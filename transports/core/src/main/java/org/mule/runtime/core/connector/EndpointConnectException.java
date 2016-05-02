/*
 * Copyright (c) MuleSoft, Inc.  All rights reserved.  http://www.mulesoft.com
 * The software in this package is published under the terms of the CPAL v1.0
 * license, a copy of which has been included with this distribution in the
 * LICENSE.txt file.
 */
package org.mule.runtime.core.connector;

import org.mule.runtime.core.api.connector.Connectable;
import org.mule.runtime.core.config.i18n.Message;
import org.mule.runtime.core.transport.AbstractTransportMessageHandler;

/** 
 * When this exception is thrown it will trigger a retry (reconnection) policy to go into effect if one is configured.
 */
public class EndpointConnectException extends ConnectException
{
    /** Serial version */
    private static final long serialVersionUID = -7802483584780922653L;

    /** Resource which has disconnected */
    private transient Connectable failed;
    
    public EndpointConnectException(Message message, Connectable failed)
    {
        super(message, resolveFailed(failed));
    }

    public EndpointConnectException(Message message, Throwable cause, Connectable failed)
    {
        super(message, cause, resolveFailed(failed));
    }

    public EndpointConnectException(Throwable cause, Connectable failed)
    {
        super(cause, resolveFailed(failed));
    }
    
    protected static Connectable resolveFailed(Connectable failed)
    {
        return failed instanceof AbstractTransportMessageHandler ? ((AbstractTransportMessageHandler) failed).getConnector() : failed;
    }
}
