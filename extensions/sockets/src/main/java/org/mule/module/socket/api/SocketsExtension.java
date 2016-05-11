/*
 * Copyright (c) MuleSoft, Inc.  All rights reserved.  http://www.mulesoft.com
 * The software in this package is published under the terms of the CPAL v1.0
 * license, a copy of which has been included with this distribution in the
 * LICENSE.txt file.
 */
package org.mule.module.socket.api;

import org.mule.module.socket.api.config.ListenerConfig;
import org.mule.module.socket.api.config.RequesterConfig;
import org.mule.module.socket.api.protocol.DirectProtocol;
import org.mule.module.socket.api.protocol.LengthProtocol;
import org.mule.module.socket.api.protocol.SafeProtocol;
import org.mule.module.socket.api.protocol.StreamingProtocol;
import org.mule.module.socket.api.protocol.TcpProtocol;
import org.mule.module.socket.api.provider.UdpListenerProvider;
import org.mule.module.socket.api.tcp.TcpClientSocketProperties;
import org.mule.module.socket.api.tcp.TcpServerSocketProperties;
import org.mule.module.socket.api.udp.UdpSocketProperties;
import org.mule.module.socket.internal.DefaultTcpClientSocketProperties;
import org.mule.module.socket.internal.DefaultTcpServerSocketProperties;
import org.mule.module.socket.internal.DefaultUdpSocketProperties;
import org.mule.runtime.extension.api.annotation.Configurations;
import org.mule.runtime.extension.api.annotation.Extension;
import org.mule.runtime.extension.api.annotation.SubTypeMapping;
import org.mule.runtime.extension.api.annotation.connector.Providers;

/**
 * An extension for sending and receiving connections through both TCP and UDP protocols.
 *
 * @since 4.0
 */
@Extension(name = "sockets")
@Configurations({ListenerConfig.class, RequesterConfig.class})
@SubTypeMapping(baseType = TcpClientSocketProperties.class, subTypes = {DefaultTcpClientSocketProperties.class})
@SubTypeMapping(baseType = TcpServerSocketProperties.class, subTypes = {DefaultTcpServerSocketProperties.class})
@SubTypeMapping(baseType = UdpSocketProperties.class, subTypes = {DefaultUdpSocketProperties.class})
@SubTypeMapping(baseType = TcpProtocol.class, subTypes = {SafeProtocol.class, DirectProtocol.class, LengthProtocol.class, StreamingProtocol.class})
public class SocketsExtension
{

}
