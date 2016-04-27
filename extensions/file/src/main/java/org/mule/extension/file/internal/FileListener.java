/*
 * Copyright (c) MuleSoft, Inc.  All rights reserved.  http://www.mulesoft.com
 * The software in this package is published under the terms of the CPAL v1.0
 * license, a copy of which has been included with this distribution in the
 * LICENSE.txt file.
 */
package org.mule.extension.file.internal;

import static java.lang.String.format;
import static java.nio.file.StandardWatchEventKinds.OVERFLOW;
import static java.util.concurrent.TimeUnit.MILLISECONDS;
import static java.util.stream.Collectors.toSet;
import static org.mule.extension.file.api.ListenerEventType.CREATE;
import static org.mule.extension.file.api.ListenerEventType.DELETE;
import static org.mule.extension.file.api.ListenerEventType.UPDATE;
import static org.mule.runtime.core.config.i18n.MessageFactory.createStaticMessage;
import static org.mule.runtime.core.util.concurrent.ThreadNameHelper.getPrefix;
import org.mule.extension.file.api.FileConnector;
import org.mule.extension.file.api.FileInputStream;
import org.mule.extension.file.api.ListenerEventType;
import org.mule.extension.file.api.ListenerFileAttributes;
import org.mule.runtime.api.connection.ConnectionException;
import org.mule.runtime.api.temporary.MuleMessage;
import org.mule.runtime.core.DefaultMuleMessage;
import org.mule.runtime.core.api.MuleContext;
import org.mule.runtime.core.api.MuleRuntimeException;
import org.mule.runtime.core.api.config.ConfigurationException;
import org.mule.runtime.core.api.construct.FlowConstruct;
import org.mule.runtime.core.api.construct.FlowConstructAware;
import org.mule.runtime.extension.api.annotation.Alias;
import org.mule.runtime.extension.api.annotation.Parameter;
import org.mule.runtime.extension.api.annotation.param.ConfigName;
import org.mule.runtime.extension.api.annotation.param.Optional;
import org.mule.runtime.extension.api.annotation.param.UseConfig;
import org.mule.runtime.extension.api.runtime.source.Source;
import org.mule.runtime.module.extension.file.api.FileAttributes;
import org.mule.runtime.module.extension.file.api.FilePredicateBuilder;
import org.mule.runtime.module.extension.file.api.lock.NullPathLock;
import org.mule.runtime.module.extension.file.api.matcher.NullFilePayloadPredicate;

import com.google.common.base.Supplier;
import com.google.common.collect.ImmutableSet;

import java.io.InputStream;
import java.nio.file.FileSystems;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.WatchEvent;
import java.nio.file.WatchEvent.Kind;
import java.nio.file.WatchKey;
import java.nio.file.WatchService;
import java.util.Set;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.function.Predicate;

import javax.inject.Inject;

public class FileListener extends Source<InputStream, ListenerFileAttributes> implements FlowConstructAware
{

    @UseConfig
    private FileConnector config;

    @Parameter
    private String directory;

    @Parameter
    @Optional(defaultValue = "true")
    private boolean notifyOnCreate = true;

    @Parameter
    @Optional(defaultValue = "true")
    private boolean notifyOnUpdate = true;

    @Parameter
    @Optional(defaultValue = "true")
    private boolean notifyOnDelete = true;

    @Parameter
    @Optional(defaultValue = "false")
    private boolean recursive = false;

    @Parameter
    @Optional
    @Alias("matcher")
    private FilePredicateBuilder<FilePredicateBuilder, FileAttributes> predicateBuilder;

    @Inject
    private MuleContext muleContext;

    @ConfigName
    private String configName;

    private FlowConstruct flowConstruct;

    private WatchService watcher;
    private Predicate<FileAttributes> matcher;
    private Set<ListenerEventType> enabledEventTypes = null;
    private ExecutorService executorService;

    @Override
    public void start() throws Exception
    {
        createWatcherService();

        //TODO resolve properly
        Path directoryPath = Paths.get(directory);
        matcher = predicateBuilder != null ? predicateBuilder.build() : new NullFilePayloadPredicate();
        enabledEventTypes = getEnabledEventTypes();

        directoryPath.register(watcher, getEnabledEventKinds());
        executorService = Executors.newSingleThreadExecutor(r -> new Thread(format("%s%s.file.listener", getPrefix(muleContext), flowConstruct.getName(), r)));
        executorService.execute(this::listen);
    }

    private void listen()
    {
        for (; ; )
        {
            WatchKey key;
            try
            {
                key = watcher.take();
            }
            catch (InterruptedException e)
            {
                stop();
                return;
            }

            try
            {
                for (WatchEvent<?> event : key.pollEvents())
                {
                    Kind<?> kind = event.kind();

                    if (kind == OVERFLOW)
                    {
                        //log
                        continue;
                    }

                    WatchEvent<Path> ev = (WatchEvent<Path>) event;
                    Path path = ev.context();

                    ListenerFileAttributes attributes = new ListenerFileAttributes(path, ListenerEventType.of(kind));
                    if (!matcher.test(attributes))
                    {
                        //log
                        continue;
                    }

                    sourceContext.getMessageHandler().handle(createMessage(path, attributes));
                }
            }
            finally
            {
                if (!key.reset())
                {
                    //log
                    sourceContext.getExceptionCallback().onException(new ConnectionException("fuck!"));
                }
            }
        }
    }

    private MuleMessage<InputStream, ListenerFileAttributes> createMessage(Path path, ListenerFileAttributes attributes)
    {
        MuleMessage<InputStream, ListenerFileAttributes> message = (MuleMessage) new DefaultMuleMessage(new FileInputStream(path, new NullPathLock()), attributes);
        return message;
    }

    @Override
    public void stop()
    {
        executorService.shutdownNow();
        try
        {
            if (!executorService.awaitTermination(muleContext.getConfiguration().getShutdownTimeout(), MILLISECONDS))
            {
                //log
            }
        }
        catch (InterruptedException e)
        {
            //log
        }
    }

    private void createWatcherService()
    {
        try
        {
            watcher = FileSystems.getDefault().newWatchService();
        }
        catch (Exception e)
        {
            throw new MuleRuntimeException(createStaticMessage("Could not create watcher service"), e);
        }
    }

    private Set<ListenerEventType> getEnabledEventTypes() throws ConfigurationException
    {
        if (enabledEventTypes == null)
        {
            ImmutableSet.Builder<ListenerEventType> types = ImmutableSet.builder();
            addEventType(types, notifyOnCreate, () -> CREATE);
            addEventType(types, notifyOnUpdate, () -> UPDATE);
            addEventType(types, notifyOnDelete, () -> DELETE);

            enabledEventTypes = types.build();
        }

        if (enabledEventTypes.isEmpty())
        {
            throw new ConfigurationException(createStaticMessage(format(
                    "File listener in flow '%s' has disabled all notification types. At least one should be enabled", flowConstruct.getName())));
        }
        return enabledEventTypes;
    }

    private Kind[] getEnabledEventKinds() throws ConfigurationException
    {
        Set<Kind> kindSet = getEnabledEventTypes().stream().map(ListenerEventType::asEventKind).collect(toSet());
        Kind[] kinds = new Kind[kindSet.size()];
        return kindSet.toArray(kinds);
    }

    private void addEventType(ImmutableSet.Builder<ListenerEventType> types, boolean condition, Supplier<ListenerEventType> supplier)
    {
        if (condition)
        {
            types.add(supplier.get());
        }
    }

    @Override
    public void setFlowConstruct(FlowConstruct flowConstruct)
    {
        this.flowConstruct = flowConstruct;
    }
}
