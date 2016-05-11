/*
 * Copyright (c) MuleSoft, Inc.  All rights reserved.  http://www.mulesoft.com
 * The software in this package is published under the terms of the CPAL v1.0
 * license, a copy of which has been included with this distribution in the
 * LICENSE.txt file.
 */
package org.mule.module.socket.api.provider;

import org.mule.module.socket.api.client.SocketClient;
import org.mule.module.socket.api.client.UdpRequesterClient;
import org.mule.module.socket.api.config.RequesterConfig;
import org.mule.module.socket.api.exceptions.UnresolvableHostException;
import org.mule.module.socket.api.udp.UdpSocketProperties;
import org.mule.module.socket.internal.ConnectionSettings;
import org.mule.module.socket.internal.DefaultUdpSocketProperties;
import org.mule.runtime.api.connection.ConnectionException;
import org.mule.runtime.api.connection.ConnectionHandlingStrategy;
import org.mule.runtime.api.connection.ConnectionHandlingStrategyFactory;
import org.mule.runtime.api.connection.ConnectionProvider;
import org.mule.runtime.api.connection.ConnectionValidationResult;
import org.mule.runtime.extension.api.annotation.Alias;
import org.mule.runtime.extension.api.annotation.Parameter;
import org.mule.runtime.extension.api.annotation.ParameterGroup;
import org.mule.runtime.extension.api.annotation.param.Optional;

@Alias("udp-requester")
public class UdpRequesterProvider implements ConnectionProvider<RequesterConfig, SocketClient>
{

    @ParameterGroup
    ConnectionSettings settings;

    @Parameter
    @Optional
    UdpSocketProperties udpSocketProperties = new DefaultUdpSocketProperties();

    @Override
    public SocketClient connect(RequesterConfig udpConfig) throws ConnectionException, UnresolvableHostException
    {
        UdpRequesterClient client = new UdpRequesterClient(udpSocketProperties, settings.getHost(), settings.getPort());
        return client;
    }

    @Override
    public void disconnect(org.mule.module.socket.api.client.SocketClient client)
    {
        try
        {
            client.disconnect();
        }
        catch (ConnectionException e)
        {

        }
    }

    @Override
    public ConnectionValidationResult validate(SocketClient udpClient)
    {
        return udpClient.validate();
    }

    @Override
    public ConnectionHandlingStrategy<SocketClient> getHandlingStrategy(ConnectionHandlingStrategyFactory<RequesterConfig, SocketClient> handlingStrategyFactory)
    {
        return handlingStrategyFactory.cached();
    }
}

