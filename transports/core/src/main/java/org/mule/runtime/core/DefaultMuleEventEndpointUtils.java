/*
 * Copyright (c) MuleSoft, Inc.  All rights reserved.  http://www.mulesoft.com
 * The software in this package is published under the terms of the CPAL v1.0
 * license, a copy of which has been included with this distribution in the
 * LICENSE.txt file.
 */
package org.mule.runtime.core;

import org.mule.runtime.core.api.endpoint.InboundEndpoint;
import org.mule.runtime.core.api.security.Credentials;
import org.mule.runtime.core.security.MuleCredentials;

import java.util.Iterator;

public class DefaultMuleEventEndpointUtils
{

    /**
     * @deprecated Transport infrastructure is deprecated.
     */
    @Deprecated
    public static void populateFieldsFromInboundEndpoint(DefaultMuleEvent event, InboundEndpoint endpoint)
    {
        event.credentials = extractCredentials(endpoint);
        event.encoding = endpoint.getEncoding();
        event.exchangePattern = endpoint.getExchangePattern();
        event.messageSourceName = endpoint.getName();
        event.messageSourceURI = endpoint.getEndpointURI().getUri();
        event.timeout = endpoint.getResponseTimeout();
        event.transacted = endpoint.getTransactionConfig().isTransacted();
        fillProperties(event, endpoint);

        event.synchronous = event.resolveEventSynchronicity();
        event.nonBlocking = event.isFlowConstructNonBlockingProcessingStrategy();
    }

    /**
     * @deprecated Transport infrastructure is deprecated.
     */
    @Deprecated
    protected static void fillProperties(DefaultMuleEvent event, InboundEndpoint endpoint)
    {
        if (endpoint != null && endpoint.getProperties() != null)
        {
            for (Iterator<?> iterator = endpoint.getProperties().keySet().iterator(); iterator.hasNext();)
            {
                String prop = (String) iterator.next();

                // don't overwrite property on the message
                if (!event.ignoreProperty(prop))
                {
                    // inbound endpoint flowVariables are in the invocation scope
                    Object value = endpoint.getProperties().get(prop);
                    event.setFlowVariable(prop, value);
                }
            }
        }
    }

    /**
     * @deprecated Transport infrastructure is deprecated.
     */
    @Deprecated
    protected static Credentials extractCredentials(InboundEndpoint endpoint)
    {
        if (null != endpoint && null != endpoint.getEndpointURI()
            && null != endpoint.getEndpointURI().getUserInfo())
        {
            final String userName = endpoint.getEndpointURI().getUser();
            final String password = endpoint.getEndpointURI().getPassword();
            if (password != null && userName != null)
            {
                return new MuleCredentials(userName, password.toCharArray());
            }
        }
        return null;
    }


}
