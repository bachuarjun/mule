/*
 * Copyright (c) MuleSoft, Inc.  All rights reserved.  http://www.mulesoft.com
 * The software in this package is published under the terms of the CPAL v1.0
 * license, a copy of which has been included with this distribution in the
 * LICENSE.txt file.
 */
package org.mule.module.socket.api.config;

import org.mule.module.socket.api.provider.TcpListenerProvider;
import org.mule.module.socket.api.provider.UdpListenerProvider;
import org.mule.module.socket.api.source.TcpListener;
import org.mule.module.socket.api.source.UdpListener;
import org.mule.runtime.extension.api.annotation.Configuration;
import org.mule.runtime.extension.api.annotation.Sources;
import org.mule.runtime.extension.api.annotation.connector.Providers;

@Configuration(name = "listener-config")
@Providers({TcpListenerProvider.class, UdpListenerProvider.class})
@Sources({TcpListener.class, UdpListener.class})
public class ListenerConfig extends AbstractSocketConfig
{
}
