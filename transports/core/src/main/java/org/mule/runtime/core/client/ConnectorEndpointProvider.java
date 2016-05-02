/*
 * Copyright (c) MuleSoft, Inc.  All rights reserved.  http://www.mulesoft.com
 * The software in this package is published under the terms of the CPAL v1.0
 * license, a copy of which has been included with this distribution in the
 * LICENSE.txt file.
 */
package org.mule.runtime.core.client;

import org.mule.runtime.core.DefaultMuleEvent;
import org.mule.runtime.core.api.MuleContext;
import org.mule.runtime.core.api.MuleEvent;
import org.mule.runtime.core.api.MuleException;
import org.mule.runtime.core.api.MuleMessage;
import org.mule.runtime.core.api.client.AbstractConnectorMessageProcessorProvider;
import org.mule.runtime.core.api.client.RequestCacheKey;
import org.mule.runtime.core.api.endpoint.EndpointCache;
import org.mule.runtime.core.api.endpoint.InboundEndpoint;
import org.mule.runtime.core.api.processor.MessageProcessor;
import org.mule.runtime.core.api.transport.ReceiveException;
import org.mule.runtime.core.client.DefaultLocalMuleClient.MuleClientFlowConstruct;
import org.mule.runtime.core.endpoint.SimpleEndpointCache;

public class ConnectorEndpointProvider extends AbstractConnectorMessageProcessorProvider
{

    private EndpointCache endpointCache;

    @Override
    public void setMuleContext(MuleContext context)
    {
        super.setMuleContext(context);
        this.endpointCache = new SimpleEndpointCache(muleContext);
    }

    @Override
    public boolean supportsUrl(String url)
    {
        return true;
    }

    @Override
    protected MessageProcessor buildMessageProcessor(RequestCacheKey cacheKey) throws MuleException
    {
        final Long timeout = cacheKey.getOperationOptions().getResponseTimeout();
        if (timeout == null)
        {
            return endpointCache.getOutboundEndpoint(cacheKey.getUrl(), cacheKey.getExchangePattern(), null);
        }
        else
        {
            return new MessageProcessor()
            {
                @Override
                public MuleEvent process(MuleEvent event) throws MuleException
                {
                    final InboundEndpoint inboundEndpoint = endpointCache.getInboundEndpoint(cacheKey.getUrl(), cacheKey.getExchangePattern());
                    MuleMessage message;
                    try
                    {
                        message = inboundEndpoint.request(timeout);
                    }
                    catch (Exception e)
                    {
                        throw new ReceiveException(inboundEndpoint, timeout, e);
                    }
                    return new DefaultMuleEvent(message, new MuleClientFlowConstruct(muleContext));
                }
            };
        }

    }

}
