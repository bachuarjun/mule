/*
 * Copyright (c) MuleSoft, Inc.  All rights reserved.  http://www.mulesoft.com
 * The software in this package is published under the terms of the CPAL v1.0
 * license, a copy of which has been included with this distribution in the
 * LICENSE.txt file.
 */
package org.mule.module.socket.internal;

import org.mule.module.socket.api.SocketProperties;
import org.mule.runtime.extension.api.annotation.Parameter;
import org.mule.runtime.extension.api.annotation.param.ConfigName;
import org.mule.runtime.extension.api.annotation.param.Optional;

public abstract class AbstractSocketProperties implements SocketProperties
{

    public static final int DEFAULT_BUFFER_SIZE = 16384; // 1024 * 16
    /**
     * The name of this config object, so that it can be referenced by config elements.
     */
    @ConfigName
    protected String name;

    /**
     * The size of the buffer (in bytes) used when sending data, set on the socket itself.
     */
    @Parameter
    @Optional(defaultValue = "16384")
    protected Integer sendBufferSize = DEFAULT_BUFFER_SIZE;

    /**
     * The size of the buffer (in bytes) used when receiving data, set on the socket itself.
     */
    @Parameter
    @Optional(defaultValue = "16384")
    protected Integer receiveBufferSize = DEFAULT_BUFFER_SIZE;

    /**
     * This sets the SO_TIMEOUT value on client sockets. Reading from the socket will block for up to this long
     * (in milliseconds) before the read fails.
     * <p>
     * A value of 0 (the default) causes the read to wait indefinitely (if no data arrives).
     */
    @Parameter
    @Optional(defaultValue = "0")
    protected Integer timeout = 0;


    /**
     * Whether the socket should fail during its creation if the host set on the endpoint cannot be resolved.
     * However, it can be set to false to allow unresolved hosts (useful when connecting through a proxy).
     */
    @Parameter
    @Optional(defaultValue = "true")
    protected Boolean failOnUnresolvedHost = true;

    public String getName()
    {
        return name;
    }

    public void setName(String name)
    {
        this.name = name;
    }

    public Integer getSendBufferSize()
    {
        return sendBufferSize;
    }

    public void setSendBufferSize(Integer sendBufferSize)
    {
        this.sendBufferSize = sendBufferSize;
    }

    public Integer getReceiveBufferSize()
    {
        return receiveBufferSize;
    }

    public void setReceiveBufferSize(Integer receiveBufferSize)
    {
        this.receiveBufferSize = receiveBufferSize;
    }

    public Integer getTimeout()
    {
        return timeout;
    }

    public void setTimeout(Integer timeout)
    {
        this.timeout = timeout;
    }

    public Boolean getFailOnUnresolvedHost()
    {
        return failOnUnresolvedHost;
    }

    public void setFailOnUnresolvedHost(Boolean failOnUnresolvedHost)
    {
        this.failOnUnresolvedHost = failOnUnresolvedHost;
    }
}
