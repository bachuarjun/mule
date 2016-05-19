/*
 * Copyright (c) MuleSoft, Inc.  All rights reserved.  http://www.mulesoft.com
 * The software in this package is published under the terms of the CPAL v1.0
 * license, a copy of which has been included with this distribution in the
 * LICENSE.txt file.
 */
package org.mule.module.socket.internal;

import org.mule.module.socket.api.tcp.TcpClientSocketProperties;
import org.mule.runtime.extension.api.annotation.Parameter;
import org.mule.runtime.extension.api.annotation.param.Optional;

/**
 * Default mutable implementation of the {@code TcpClientSocketProperties} interface.
 *
 * @since 4.0
 */
public class DefaultTcpClientSocketProperties extends AbstractTcpSocketProperties implements TcpClientSocketProperties
{
    /**
     * Number of milliseconds to wait until an outbound connection to a remote server is successfully created.
     * Defaults to 30 seconds.
     */
    @Parameter
    @Optional(defaultValue = "30000")
    private Integer connectionTimeout = 30000;

    public Integer getConnectionTimeout()
    {
        return connectionTimeout;
    }

    public void setConnectionTimeout(Integer connectionTimeout)
    {
        this.connectionTimeout = connectionTimeout;
    }

}
