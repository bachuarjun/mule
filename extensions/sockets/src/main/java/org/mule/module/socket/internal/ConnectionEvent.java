/*
 * Copyright (c) MuleSoft, Inc.  All rights reserved.  http://www.mulesoft.com
 * The software in this package is published under the terms of the CPAL v1.0
 * license, a copy of which has been included with this distribution in the
 * LICENSE.txt file.
 */
package org.mule.module.socket.internal;

import org.mule.module.socket.api.source.SocketAttributes;

public class ConnectionEvent
{

    private Object content;

    public SocketAttributes getAttributes()
    {
        return attributes;
    }

    private SocketAttributes attributes;

    public Object getContent()
    {
        return content;
    }

    public ConnectionEvent(Object content, SocketAttributes attributes)
    {
        this.content = content;
        this.attributes = attributes;
    }

    public ConnectionEvent()
    {
    }

    public ConnectionEvent(SocketAttributes attributes)
    {
        this.attributes = attributes;
    }
}
