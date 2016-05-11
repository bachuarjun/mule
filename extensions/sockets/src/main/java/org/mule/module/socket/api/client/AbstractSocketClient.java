/*
 * Copyright (c) MuleSoft, Inc.  All rights reserved.  http://www.mulesoft.com
 * The software in this package is published under the terms of the CPAL v1.0
 * license, a copy of which has been included with this distribution in the
 * LICENSE.txt file.
 */
package org.mule.module.socket.api.client;

import org.mule.runtime.api.connection.ConnectionException;
import org.mule.runtime.api.connection.ConnectionExceptionCode;
import org.mule.runtime.api.connection.ConnectionValidationResult;

public abstract class AbstractSocketClient implements SocketClient
{

    protected String host;
    protected int port;

    public AbstractSocketClient(String host, int port)
    {
        this.host = host;
        this.port = port;
    }

    public ConnectionValidationResult validate()
    {
        if (this.isValid())
        {
            return ConnectionValidationResult.success();
        }
        else
        {
            String msg = "Socket is not connected";
            return ConnectionValidationResult.failure(msg, ConnectionExceptionCode.UNKNOWN, new ConnectionException(msg));
        }
    }
}
