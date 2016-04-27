/*
 * Copyright (c) MuleSoft, Inc.  All rights reserved.  http://www.mulesoft.com
 * The software in this package is published under the terms of the CPAL v1.0
 * license, a copy of which has been included with this distribution in the
 * LICENSE.txt file.
 */
package org.mule.extension.file.api;

import java.nio.file.Path;

public class ListenerFileAttributes extends LocalFileAttributes
{
    private final ListenerEventType eventType;

    public ListenerFileAttributes(Path path, ListenerEventType eventType)
    {
        super(path);
        this.eventType = eventType;
    }

    public ListenerEventType getEventType()
    {
        return eventType;
    }
}
