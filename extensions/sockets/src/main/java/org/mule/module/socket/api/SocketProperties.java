/*
 * Copyright (c) MuleSoft, Inc.  All rights reserved.  http://www.mulesoft.com
 * The software in this package is published under the terms of the CPAL v1.0
 * license, a copy of which has been included with this distribution in the
 * LICENSE.txt file.
 */

package org.mule.module.socket.api;

/**
 * Interface for objects that provide common configuration for both TCP and UDP sockets.
 * <p>
 * {@code null} values can be returned by any of the methods, meaning that there is no value defined for the property.
 *
 * @since 4.0
 */
public interface SocketProperties
{

    /**
     * The size of the buffer (in bytes) used when sending data, set on the socket itself.
     */
    Integer getSendBufferSize();

    /**
     * The size of the buffer (in bytes) used when receiving data, set on the socket itself.
     */
    Integer getReceiveBufferSize();

    /**
     * This sets the SO_TIMEOUT value on client sockets. Reading from the socket will block for up to this long
     * (in milliseconds) before the read fails.
     * <p>
     * A value of 0 (the default) causes the read to wait indefinitely (if no data arrives).
     */
    Integer getTimeout();

    /**
     * Will fail during sending if the remote host address cannot be resolved.
     * However, it can be set to false to allow unresolved hosts
     * (this is useful on some circumstances like connecting through a proxy).
     */
    Boolean getFailOnUnresolvedHost();
}
