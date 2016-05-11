/*
 * Copyright (c) MuleSoft, Inc.  All rights reserved.  http://www.mulesoft.com
 * The software in this package is published under the terms of the CPAL v1.0
 * license, a copy of which has been included with this distribution in the
 * LICENSE.txt file.
 */
package org.mule.module.socket.api.source;

import static java.lang.String.format;
import static java.util.concurrent.Executors.newSingleThreadExecutor;
import static java.util.concurrent.TimeUnit.MILLISECONDS;
import static org.mule.runtime.core.util.concurrent.ThreadNameHelper.getPrefix;
import org.mule.module.socket.api.client.UdpListenerClient;
import org.mule.module.socket.api.exceptions.UnresolvableHostException;
import org.mule.module.socket.internal.ConnectionEvent;
import org.mule.runtime.api.connection.ConnectionException;
import org.mule.runtime.api.message.MuleMessage;
import org.mule.runtime.api.metadata.DataType;
import org.mule.runtime.core.DefaultMuleMessage;
import org.mule.runtime.core.api.MuleContext;
import org.mule.runtime.core.api.construct.FlowConstruct;
import org.mule.runtime.core.api.construct.FlowConstructAware;
import org.mule.runtime.core.transformer.types.DataTypeFactory;
import org.mule.runtime.extension.api.annotation.Alias;
import org.mule.runtime.extension.api.annotation.param.Connection;
import org.mule.runtime.extension.api.runtime.source.Source;

import java.io.IOException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.atomic.AtomicBoolean;

import javax.inject.Inject;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

@Alias("udp-listener")
public class UdpListener extends Source<Object, SocketAttributes> implements FlowConstructAware
{

    private static final Logger LOGGER = LoggerFactory.getLogger(UdpListener.class);
    private ExecutorService executorService;
    private FlowConstruct flowConstruct;

    @Inject
    private MuleContext muleContext;

    @Connection
    private UdpListenerClient client;

    private AtomicBoolean stopRequested = new AtomicBoolean(false);

    @Override
    public void start() throws Exception
    {
        executorService = newSingleThreadExecutor(r -> new Thread(r, format("%s%s.udp.listener", getPrefix(muleContext), flowConstruct.getName())));
        executorService.execute(this::listen);
    }

    private void listen()
    {

        for (; ; )
        {

            if (isRequestedToStop())
            {
                return;
            }

            try
            {
                ConnectionEvent event = client.receive();
                if (event.getContent() != null)
                {
                    processNewConnection(event);
                }
            }
            catch (ConnectionException e)
            {
                // TODO is this the right behaviour?
                sourceContext.getExceptionCallback().onException(e);
            }
            catch (UnresolvableHostException e)
            {
                sourceContext.getExceptionCallback().onException(e);
            }
            catch (IOException e)
            {
                sourceContext.getExceptionCallback().onException(e);
            }
        }
    }


    @Override
    public void stop()
    {
        try
        {
            if (client != null)
            {
                client.disconnect();
            }
        }
        catch (ConnectionException e)
        {
            e.printStackTrace();
        }
        stopRequested.set(true);
        shutdownExecutor();
    }

    private boolean isRequestedToStop()
    {
        return stopRequested.get() || Thread.currentThread().isInterrupted();
    }


    private void shutdownExecutor()
    {
        if (executorService == null)
        {
            return;
        }

        executorService.shutdownNow();
        try
        {
            if (!executorService.awaitTermination(muleContext.getConfiguration().getShutdownTimeout(), MILLISECONDS))
            {
                if (LOGGER.isWarnEnabled())
                {
                    LOGGER.warn("Could not properly terminate pending events for directory listener on flow " + flowConstruct.getName());
                }
            }
        }
        catch (InterruptedException e)
        {
            if (LOGGER.isWarnEnabled())
            {
                LOGGER.warn("Got interrupted while trying to terminate pending events for directory listener on flow " + flowConstruct.getName());
            }
        }
    }

    @Override
    public void setFlowConstruct(FlowConstruct flowConstruct)
    {
        this.flowConstruct = flowConstruct;
    }

    private void processNewConnection(ConnectionEvent event) throws IOException, UnresolvableHostException, ConnectionException
    {
        LOGGER.debug("Processing new connection");
        if (isRequestedToStop())
        {
            return;
        }

        sourceContext.getMessageHandler().handle(createMessage(event));
    }

    private DataType<Object> getMessageDataType(DataType<?> originalDataType)
    {
        DataType<Object> newDataType = DataTypeFactory.create(Object.class);
        newDataType.setEncoding(originalDataType.getEncoding());

        //String presumedMimeType = mimetypesFileTypeMap.getContentType(attributes.getPath());
        //newDataType.setMimeType(presumedMimeType != null ? presumedMimeType : originalDataType.getMimeType());

        return newDataType;
    }

    private MuleMessage<Object, SocketAttributes> createMessage(ConnectionEvent event) throws IOException, ConnectionException, UnresolvableHostException
    {
        SocketAttributes attributes = client.getSocketAttributes();
        DataType dataType = getMessageDataType(DataTypeFactory.create(Object.class));
        return (MuleMessage) new DefaultMuleMessage(event.getContent(), dataType, event.getAttributes(), muleContext);
    }

}
