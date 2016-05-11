/*
 * Copyright (c) MuleSoft, Inc.  All rights reserved.  http://www.mulesoft.com
 * The software in this package is published under the terms of the CPAL v1.0
 * license, a copy of which has been included with this distribution in the
 * LICENSE.txt file.
 */
package org.mule.module.socket.api;

import org.mule.module.socket.api.client.RequesterSocket;
import org.mule.runtime.api.connection.ConnectionException;
import org.mule.runtime.extension.api.annotation.param.Connection;
import org.mule.runtime.extension.api.annotation.param.Optional;

public class SocketOperations
{

    public void write(@Connection RequesterSocket client,
                      @Optional(defaultValue = "#[payload]") Object data)
            throws ConnectionException
    {
        client.write(data);
    }
}
