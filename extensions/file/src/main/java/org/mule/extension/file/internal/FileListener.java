/*
 * Copyright (c) MuleSoft, Inc.  All rights reserved.  http://www.mulesoft.com
 * The software in this package is published under the terms of the CPAL v1.0
 * license, a copy of which has been included with this distribution in the
 * LICENSE.txt file.
 */
package org.mule.extension.file.internal;

import static java.util.stream.Collectors.toSet;
import static org.mule.extension.file.api.ListenerEventType.CREATE;
import static org.mule.extension.file.api.ListenerEventType.DELETE;
import static org.mule.extension.file.api.ListenerEventType.UPDATE;
import static org.mule.runtime.core.config.i18n.MessageFactory.createStaticMessage;
import org.mule.extension.file.api.FileConnector;
import org.mule.extension.file.api.ListenerEventType;
import org.mule.extension.file.api.ListenerFileAttributes;
import org.mule.runtime.core.api.MuleRuntimeException;
import org.mule.runtime.extension.api.annotation.Parameter;
import org.mule.runtime.extension.api.annotation.param.Optional;
import org.mule.runtime.extension.api.annotation.param.UseConfig;
import org.mule.runtime.extension.api.runtime.source.Source;
import org.mule.runtime.module.extension.file.api.FilePredicateBuilder;

import com.google.common.base.Supplier;
import com.google.common.collect.ImmutableSet;

import java.io.InputStream;
import java.nio.file.FileSystems;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.WatchEvent.Kind;
import java.nio.file.WatchService;
import java.util.Set;

public class FileListener extends Source<InputStream, ListenerFileAttributes>
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
    private FilePredicateBuilder matcher;

    private WatchService watcher;

    private Set<ListenerEventType> enabledEventTypes = null;

    @Override
    public void start() throws Exception
    {
        //TODO resolve properly
        Path directoryPath = Paths.get(directory);

        enabledEventTypes = getEnabledEventTypes();


        createWatcherService();

        directoryPath.register(watcher, )

    }

    @Override
    public void stop()
    {

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

    private Set<ListenerEventType> getEnabledEventTypes()
    {
        if (enabledEventTypes == null)
        {
            ImmutableSet.Builder<ListenerEventType> types = ImmutableSet.builder();
            addEventType(types, notifyOnCreate, () -> CREATE);
            addEventType(types, notifyOnUpdate, () -> UPDATE);
            addEventType(types, notifyOnDelete, () -> DELETE);

            enabledEventTypes = types.build();
        }

        if (enabledEventTypes.isEmpty()) {

        }
        return enabledEventTypes;
    }

    private Kind[] getEnabledEventKinds()
    {
        Set<Kind> kindSet = getEnabledEventTypes().stream().map(ListenerEventType::asEventKind).collect(toSet());
        Kind[] kinds = new Kind[kindSet.size()];
        return kindSet.toArray(kinds);
    }

    private Kind<?> toEventKinds()

    private void addEventType(ImmutableSet.Builder<ListenerEventType> types, boolean condition, Supplier<ListenerEventType> supplier)
    {
        if (condition)
        {
            types.add(supplier.get());
        }
    }

}
