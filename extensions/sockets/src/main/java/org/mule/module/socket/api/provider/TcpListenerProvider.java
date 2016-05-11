/*
 * Copyright (c) MuleSoft, Inc.  All rights reserved.  http://www.mulesoft.com
 * The software in this package is published under the terms of the CPAL v1.0
 * license, a copy of which has been included with this distribution in the
 * LICENSE.txt file.
 */
package org.mule.module.socket.api.provider;

import org.mule.module.socket.api.client.TcpListenerClient;
import org.mule.module.socket.api.config.ListenerConfig;
import org.mule.module.socket.api.protocol.SafeProtocol;
import org.mule.module.socket.api.protocol.TcpProtocol;
import org.mule.module.socket.api.tcp.TcpServerSocketProperties;
import org.mule.module.socket.internal.ConnectionSettings;
import org.mule.module.socket.internal.DefaultTcpServerSocketProperties;
import org.mule.runtime.api.connection.ConnectionException;
import org.mule.runtime.api.connection.ConnectionExceptionCode;
import org.mule.runtime.api.connection.ConnectionHandlingStrategy;
import org.mule.runtime.api.connection.ConnectionHandlingStrategyFactory;
import org.mule.runtime.api.connection.ConnectionProvider;
import org.mule.runtime.api.connection.ConnectionValidationResult;
import org.mule.runtime.extension.api.annotation.Alias;
import org.mule.runtime.extension.api.annotation.Parameter;
import org.mule.runtime.extension.api.annotation.ParameterGroup;
import org.mule.runtime.extension.api.annotation.param.Optional;

@Alias("tcp-listener")
public class TcpListenerProvider implements ConnectionProvider<ListenerConfig, TcpListenerClient>
{

    @ParameterGroup
    ConnectionSettings settings;

    @Parameter
    @Optional
    TcpServerSocketProperties tcpServerSocketProperties = new DefaultTcpServerSocketProperties();

    @Parameter
    @Optional
    TcpProtocol protocol = new SafeProtocol();

    @Override
    public TcpListenerClient connect(ListenerConfig listenerConfig) throws ConnectionException
    {
        return new TcpListenerClient(tcpServerSocketProperties, protocol, settings.getHost(), settings.getPort());
    }

    @Override
    public void disconnect(TcpListenerClient tcpConnection)
    {
        try
        {
            tcpConnection.disconnect();
        }
        catch (ConnectionException e)
        {
            e.printStackTrace();
        }
    }

    @Override
    public ConnectionValidationResult validate(TcpListenerClient tcpListenerClient)
    {
        if (tcpListenerClient.isValid())
        {
            return ConnectionValidationResult.success();
        }
        else
        {
            String msg = "Socket is not connected";
            return ConnectionValidationResult.failure(msg, ConnectionExceptionCode.UNKNOWN, new ConnectionException(msg));
        }
    }

    @Override
    public ConnectionHandlingStrategy<TcpListenerClient> getHandlingStrategy(ConnectionHandlingStrategyFactory<ListenerConfig, TcpListenerClient> handlingStrategyFactory)
    {
        return handlingStrategyFactory.cached();
    }
}
