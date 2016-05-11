/*
 * Copyright (c) MuleSoft, Inc.  All rights reserved.  http://www.mulesoft.com
 * The software in this package is published under the terms of the CPAL v1.0
 * license, a copy of which has been included with this distribution in the
 * LICENSE.txt file.
 */
package org.mule.module.socket.api.provider;

import org.mule.module.socket.api.client.SocketClient;
import org.mule.module.socket.api.client.TcpRequesterClient;
import org.mule.module.socket.api.config.RequesterConfig;
import org.mule.module.socket.api.exceptions.UnresolvableHostException;
import org.mule.module.socket.api.protocol.SafeProtocol;
import org.mule.module.socket.api.protocol.TcpProtocol;
import org.mule.module.socket.api.tcp.TcpClientSocketProperties;
import org.mule.module.socket.internal.ConnectionSettings;
import org.mule.module.socket.internal.DefaultTcpClientSocketProperties;
import org.mule.runtime.api.connection.ConnectionException;
import org.mule.runtime.api.connection.ConnectionHandlingStrategy;
import org.mule.runtime.api.connection.ConnectionHandlingStrategyFactory;
import org.mule.runtime.api.connection.ConnectionProvider;
import org.mule.runtime.api.connection.ConnectionValidationResult;
import org.mule.runtime.extension.api.annotation.Alias;
import org.mule.runtime.extension.api.annotation.Parameter;
import org.mule.runtime.extension.api.annotation.ParameterGroup;
import org.mule.runtime.extension.api.annotation.param.Optional;

@Alias("tcp-requester")
public class TcpRequesterProvider implements ConnectionProvider<RequesterConfig, SocketClient>
{

    @ParameterGroup
    ConnectionSettings settings;

    @Parameter
    @Optional
    TcpClientSocketProperties tcpClientSocketProperties = new DefaultTcpClientSocketProperties();

    @Parameter
    @Optional
    TcpProtocol protocol = new SafeProtocol();


    @Override
    public SocketClient connect(RequesterConfig requesterConfig) throws ConnectionException
    {
        TcpRequesterClient client = new TcpRequesterClient(tcpClientSocketProperties, protocol, settings.getHost(), settings.getPort());
        try
        {
            client.connect();
        }
        catch (UnresolvableHostException e)
        {
            // todo check
        }
        return client;
    }

    @Override
    public void disconnect(SocketClient tcpRequesterClient)
    {
        try
        {
            tcpRequesterClient.disconnect();
        }
        catch (ConnectionException e)
        {
            // todo
            e.printStackTrace();
        }
    }

    @Override
    public ConnectionValidationResult validate(SocketClient tcpRequesterClient)
    {
        return tcpRequesterClient.validate();
    }

    @Override
    public ConnectionHandlingStrategy<SocketClient> getHandlingStrategy(ConnectionHandlingStrategyFactory<RequesterConfig, SocketClient> handlingStrategyFactory)
    {
        return handlingStrategyFactory.supportsPooling();
    }
}
