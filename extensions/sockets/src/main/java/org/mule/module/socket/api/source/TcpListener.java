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
import org.mule.module.socket.api.client.TcpListenerClient;
import org.mule.module.socket.api.config.ListenerConfig;
import org.mule.module.socket.api.protocol.SafeProtocol;
import org.mule.module.socket.api.protocol.TcpProtocol;
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
import org.mule.runtime.extension.api.annotation.Parameter;
import org.mule.runtime.extension.api.annotation.param.Connection;
import org.mule.runtime.extension.api.annotation.param.Optional;
import org.mule.runtime.extension.api.annotation.param.UseConfig;
import org.mule.runtime.extension.api.runtime.source.Source;

import java.io.IOException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.atomic.AtomicBoolean;

import javax.inject.Inject;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

@Alias("tcp-listener")
public class TcpListener extends Source<Object, SocketAttributes> implements FlowConstructAware
{

    private static final Logger LOGGER = LoggerFactory.getLogger(TcpListener.class);
    private ExecutorService executorService;
    private FlowConstruct flowConstruct;


    @UseConfig
    private ListenerConfig config;

    @Inject
    private MuleContext muleContext;

    @Parameter
    @Optional
    TcpProtocol protocol = new SafeProtocol();

    @Connection
    private TcpListenerClient client;

    private AtomicBoolean stopRequested = new AtomicBoolean(false);

    @Override
    public void start() throws Exception
    {
        executorService = newSingleThreadExecutor(r -> new Thread(r, format("%s%s.tcp.listener", getPrefix(muleContext), flowConstruct.getName())));
        executorService.execute(this::listen);
    }

    private void listen()
    {
        LOGGER.debug("Started listener");
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
                e.printStackTrace();
                sourceContext.getExceptionCallback().onException(e);
            }
            catch (IOException e)
            {
                e.printStackTrace();
            }

        }
    }

    private MuleMessage<Object, SocketAttributes> createMessage(ConnectionEvent event) throws IOException, ConnectionException
    {
        DataType dataType = getTcpMessageDataType(DataTypeFactory.create(Object.class), event.getAttributes());
        return (MuleMessage) new DefaultMuleMessage(event.getContent(), dataType, event.getAttributes(), muleContext);
    }

    private DataType<Object> getTcpMessageDataType(DataType<?> originalDataType, SocketAttributes attributes)
    {
        DataType<Object> newDataType = DataTypeFactory.create(Object.class);
        newDataType.setEncoding(originalDataType.getEncoding());
        return newDataType;
    }

    private void processNewConnection(ConnectionEvent event)
    {
        LOGGER.debug("Processing new connection");
        if (isRequestedToStop())
        {
            return;
        }

        // TODO new thread with reading and creating message
        try
        {
            sourceContext.getMessageHandler().handle(createMessage(event));
        }
        catch (IOException e)
        {
            e.printStackTrace();
        }
        catch (ConnectionException e)
        {
            e.printStackTrace();
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
}
