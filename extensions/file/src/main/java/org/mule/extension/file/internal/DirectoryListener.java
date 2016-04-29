/*
 * Copyright (c) MuleSoft, Inc.  All rights reserved.  http://www.mulesoft.com
 * The software in this package is published under the terms of the CPAL v1.0
 * license, a copy of which has been included with this distribution in the
 * LICENSE.txt file.
 */
package org.mule.extension.file.internal;

import static com.sun.nio.file.SensitivityWatchEventModifier.HIGH;
import static java.lang.String.format;
import static java.nio.file.FileVisitResult.CONTINUE;
import static java.nio.file.Files.walkFileTree;
import static java.nio.file.StandardWatchEventKinds.OVERFLOW;
import static java.util.concurrent.Executors.newSingleThreadExecutor;
import static java.util.concurrent.TimeUnit.MILLISECONDS;
import static java.util.stream.Collectors.toSet;
import static org.mule.extension.file.api.FileEventType.CREATE;
import static org.mule.extension.file.api.FileEventType.DELETE;
import static org.mule.extension.file.api.FileEventType.UPDATE;
import static org.mule.runtime.core.config.i18n.MessageFactory.createStaticMessage;
import static org.mule.runtime.core.util.concurrent.ThreadNameHelper.getPrefix;
import org.mule.extension.file.api.DeletedFileAttributes;
import org.mule.extension.file.api.FileConnector;
import org.mule.extension.file.api.FileEventType;
import org.mule.extension.file.api.FileInputStream;
import org.mule.extension.file.api.ListenerFileAttributes;
import org.mule.runtime.api.message.NullPayload;
import org.mule.runtime.api.metadata.DataType;
import org.mule.runtime.api.temporary.MuleMessage;
import org.mule.runtime.core.DefaultMuleMessage;
import org.mule.runtime.core.api.MuleContext;
import org.mule.runtime.core.api.MuleRuntimeException;
import org.mule.runtime.core.api.config.ConfigurationException;
import org.mule.runtime.core.api.construct.FlowConstruct;
import org.mule.runtime.core.api.construct.FlowConstructAware;
import org.mule.runtime.core.transformer.types.DataTypeFactory;
import org.mule.runtime.extension.api.annotation.Alias;
import org.mule.runtime.extension.api.annotation.Parameter;
import org.mule.runtime.extension.api.annotation.param.Connection;
import org.mule.runtime.extension.api.annotation.param.Optional;
import org.mule.runtime.extension.api.annotation.param.UseConfig;
import org.mule.runtime.extension.api.runtime.source.Source;
import org.mule.runtime.module.extension.file.api.FileAttributes;
import org.mule.runtime.module.extension.file.api.FilePredicateBuilder;
import org.mule.runtime.module.extension.file.api.FileSystem;
import org.mule.runtime.module.extension.file.api.TreeNode;
import org.mule.runtime.module.extension.file.api.lock.NullPathLock;
import org.mule.runtime.module.extension.file.api.matcher.NullFilePayloadPredicate;

import com.google.common.base.Supplier;
import com.google.common.collect.ImmutableSet;

import java.io.IOException;
import java.io.InputStream;
import java.nio.file.ClosedWatchServiceException;
import java.nio.file.FileSystems;
import java.nio.file.FileVisitResult;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.SimpleFileVisitor;
import java.nio.file.WatchEvent;
import java.nio.file.WatchEvent.Kind;
import java.nio.file.WatchKey;
import java.nio.file.WatchService;
import java.nio.file.attribute.BasicFileAttributes;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.function.Predicate;

import javax.inject.Inject;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

@Alias("directory-listener")
public class DirectoryListener extends Source<InputStream, ListenerFileAttributes> implements FlowConstructAware
{

    private static final Logger LOGGER = LoggerFactory.getLogger(DirectoryListener.class);

    @UseConfig
    private FileConnector config;

    @Parameter
    @Optional
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

    @Connection
    private FileSystem fileSystem;

    private Path directoryPath;
    private FlowConstruct flowConstruct;
    private WatchService watcher;
    private Predicate<FileAttributes> matcher;
    private Set<FileEventType> enabledEventTypes = new HashSet<>();
    private ExecutorService executorService;

    private final Map<WatchKey, Path> keyPaths = new HashMap<>();
    private final AtomicBoolean stopRequested = new AtomicBoolean(false);

    @Override
    public void start() throws Exception
    {
        calculateEnabledEventTypes();
        directoryPath = resolveDirectory();

        createWatcherService();

        matcher = predicateBuilder != null ? predicateBuilder.build() : new NullFilePayloadPredicate();
        executorService = newSingleThreadExecutor(r -> new Thread(r, format("%s%s.file.listener", getPrefix(muleContext), flowConstruct.getName())));
        executorService.execute(this::listen);
    }

    private void listen()
    {
        try
        {
            for (; ; )
            {
                if (isRequestedToStop())
                {
                    return;
                }

                WatchKey key;
                try
                {
                    key = watcher.take();
                }
                catch (InterruptedException | ClosedWatchServiceException e)
                {
                    return;
                }

                try
                {
                    key.pollEvents().forEach(this::processEvent);
                }
                finally
                {
                    resetWatchKey(key);
                }
            }
        }
        catch (Exception e)
        {
            sourceContext.getExceptionCallback().onException(e);
        }
    }

    private void processEvent(WatchEvent<?> event)
    {
        final Path path = directoryPath.resolve(((WatchEvent<Path>) event).context()).toAbsolutePath();
        final Kind<?> kind = event.kind();

        if (kind == OVERFLOW)
        {
            if (LOGGER.isWarnEnabled())
            {
                LOGGER.warn(format("Too many changes occurred concurrently on file '%s'. Events might have been lost or discarded"));
            }
            return;
        }

        ListenerFileAttributes attributes = new ListenerFileAttributes(path, FileEventType.of(kind));
        if (!matcher.test(attributes))
        {
            if (LOGGER.isDebugEnabled())
            {
                LOGGER.debug(format("Detected a '%s' event on path '%s' but it will be skipped because it does not meet the matcher's criteria",
                                    FileEventType.of(kind), path.toString()));
            }
            return;
        }

        if (isRequestedToStop())
        {
            return;
        }

        sourceContext.getMessageHandler().handle(createMessage(path, attributes));
    }


    private boolean isRequestedToStop()
    {
        return stopRequested.get() || Thread.currentThread().isInterrupted();
    }

    private MuleMessage<InputStream, ListenerFileAttributes> createMessage(Path path, ListenerFileAttributes attributes)
    {
        Object payload = NullPayload.getInstance();
        DataType dataType = DataTypeFactory.create(NullPayload.class);
        MuleMessage<InputStream, ListenerFileAttributes> message;

        if (attributes.getEventType() == FileEventType.DELETE)
        {
            attributes = new DeletedFileAttributes(path);
        }
        else if (!attributes.isDirectory())
        {
            dataType = fileSystem.getFileMessageDataType(DataTypeFactory.create(InputStream.class), attributes);
            payload = new FileInputStream(path, new NullPathLock());
        }

        message = (MuleMessage) new DefaultMuleMessage(payload, dataType, attributes, muleContext);
        return message;
    }

    @Override
    public void stop()
    {
        stopRequested.set(true);
        closeWatcherService();
        shutdownExecutor();
    }

    private void shutdownExecutor()
    {
        if (executorService == null)
        {
            return;
        }

        executorService.shutdownNow();
        try
        {
            if (!executorService.awaitTermination(muleContext.getConfiguration().getShutdownTimeout(), MILLISECONDS))
            {
                if (LOGGER.isWarnEnabled())
                {
                    LOGGER.warn("Could not properly terminate pending events for directory listener on flow " + flowConstruct.getName());
                }
            }
        }
        catch (InterruptedException e)
        {
            if (LOGGER.isWarnEnabled())
            {
                LOGGER.warn("Got interrupted while trying to terminate pending events for directory listener on flow " + flowConstruct.getName());
            }
        }
    }

    private void closeWatcherService()
    {
        if (watcher == null)
        {
            return;
        }

        try
        {
            watcher.close();
        }
        catch (IOException e)
        {
            if (LOGGER.isWarnEnabled())
            {
                LOGGER.warn("Found exception trying to close watcher service for directory listener on flow " + flowConstruct.getName(), e);
            }
        }

        keyPaths.clear();
    }

    private void resetWatchKey(WatchKey key)
    {
        if (key.reset())
        {
            return;
        }

        Path path = keyPaths.remove(key);
        if (path != null)
        {
            try
            {
                registerPath(path);
            }
            catch (IOException e)
            {
                if (LOGGER.isWarnEnabled())
                {
                    LOGGER.warn(format("Directory '%s' became unavailable and a new listener could not be established on it",
                                       path.toString()));
                }
            }
        }
    }

    private void createWatcherService() throws IOException
    {
        try
        {
            watcher = FileSystems.getDefault().newWatchService();
        }
        catch (Exception e)
        {
            throw new MuleRuntimeException(createStaticMessage("Could not create watcher service"), e);
        }

        TreeNode directoryNode = fileSystem.list(directoryPath.toString(), false, new DefaultMuleMessage(""), new NullFilePayloadPredicate());
        final Path rootPath = Paths.get(directoryNode.getAttributes().getPath());
        registerPath(rootPath);

        if (recursive)
        {
            walkFileTree(rootPath, new SimpleFileVisitor<Path>()
            {
                @Override
                public FileVisitResult preVisitDirectory(Path dir, BasicFileAttributes attrs) throws IOException
                {
                    if (!dir.equals(rootPath))
                    {
                        registerPath(dir);
                    }
                    return CONTINUE;
                }
            });
        }
    }

    private void registerPath(Path path) throws IOException
    {
        WatchKey key = path.register(watcher, getEnabledEventKinds(), HIGH);
        keyPaths.put(key, path);
    }

    private void calculateEnabledEventTypes() throws ConfigurationException
    {
        ImmutableSet.Builder<FileEventType> types = ImmutableSet.builder();
        addEventType(types, notifyOnCreate, () -> CREATE);
        addEventType(types, notifyOnUpdate, () -> UPDATE);
        addEventType(types, notifyOnDelete, () -> DELETE);

        enabledEventTypes = types.build();

        if (enabledEventTypes.isEmpty())
        {
            throw new ConfigurationException(createStaticMessage(format(
                    "File listener in flow '%s' has disabled all notification types. At least one should be enabled", flowConstruct.getName())));
        }
    }

    private Kind<?>[] getEnabledEventKinds()
    {
        Set<Kind> kindSet = enabledEventTypes.stream().map(FileEventType::asEventKind).collect(toSet());
        Kind[] kinds = new Kind[kindSet.size()];
        return kindSet.toArray(kinds);
    }

    private void addEventType(ImmutableSet.Builder<FileEventType> types, boolean condition, Supplier<FileEventType> supplier)
    {
        if (condition)
        {
            types.add(supplier.get());
        }
    }

    private Path resolveDirectory()
    {
        return directory == null
               ? Paths.get(config.getBaseDir())
               : Paths.get(config.getBaseDir()).resolve(directory).toAbsolutePath();
    }


    @Override
    public void setFlowConstruct(FlowConstruct flowConstruct)
    {
        this.flowConstruct = flowConstruct;
    }
}
