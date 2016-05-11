/*
 * Copyright (c) MuleSoft, Inc.  All rights reserved.  http://www.mulesoft.com
 * The software in this package is published under the terms of the CPAL v1.0
 * license, a copy of which has been included with this distribution in the
 * LICENSE.txt file.
 */
package org.mule.module.socket.api.config;

import org.mule.module.socket.api.SocketOperations;
import org.mule.module.socket.api.provider.TcpRequesterProvider;
import org.mule.module.socket.api.provider.UdpListenerProvider;
import org.mule.module.socket.api.provider.UdpRequesterProvider;
import org.mule.runtime.extension.api.annotation.Configuration;
import org.mule.runtime.extension.api.annotation.Operations;
import org.mule.runtime.extension.api.annotation.connector.Providers;

@Configuration(name = "requester-config")
@Operations({SocketOperations.class})
@Providers({TcpRequesterProvider.class, UdpRequesterProvider.class})
public class RequesterConfig extends AbstractSocketConfig
{

}
