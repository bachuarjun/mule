/*
 * Copyright (c) MuleSoft, Inc.  All rights reserved.  http://www.mulesoft.com
 * The software in this package is published under the terms of the CPAL v1.0
 * license, a copy of which has been included with this distribution in the
 * LICENSE.txt file.
 */
package org.mule.module.socket.api.client;

import org.mule.module.socket.api.exceptions.UnresolvableHostException;
import org.mule.runtime.api.connection.ConnectionException;

public interface RequesterSocket
{
    void write(Object data) throws ConnectionException;

    void connect() throws ConnectionException, UnresolvableHostException;
}
