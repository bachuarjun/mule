/*
 * Copyright (c) MuleSoft, Inc.  All rights reserved.  http://www.mulesoft.com
 * The software in this package is published under the terms of the CPAL v1.0
 * license, a copy of which has been included with this distribution in the
 * LICENSE.txt file.
 */
package org.mule.extension.file.internal.command;

import static java.nio.file.StandardWatchEventKinds.OVERFLOW;
import org.mule.extension.file.api.FileConnector;
import org.mule.extension.file.api.ListenerEventType;
import org.mule.extension.file.api.ListenerFileAttributes;
import org.mule.extension.file.api.LocalFileSystem;
import org.mule.runtime.api.connection.ConnectionException;

import java.nio.file.Path;
import java.nio.file.WatchEvent;
import java.nio.file.WatchKey;
import java.nio.file.WatchService;

public final class LocalListenerCommand extends LocalFileCommand
{

    private final WatchService watcher;

    LocalListenerCommand(LocalFileSystem fileSystem, FileConnector config)
    {
        super(fileSystem, config);
    }

    public void lister()
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
                WatchEvent.Kind<?> kind = event.kind();

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
