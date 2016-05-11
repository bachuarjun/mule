/*
 * Copyright (c) MuleSoft, Inc.  All rights reserved.  http://www.mulesoft.com
 * The software in this package is published under the terms of the CPAL v1.0
 * license, a copy of which has been included with this distribution in the
 * LICENSE.txt file.
 */
package org.mule.module.socket.api.client;

import org.mule.module.socket.api.source.SocketAttributes;
import org.mule.module.socket.api.udp.UdpSocketProperties;
import org.mule.runtime.api.connection.ConnectionException;

import java.io.IOException;
import java.net.DatagramSocket;
import java.net.SocketException;

public abstract class AbstractUdpClient extends AbstractSocketClient
{

    protected DatagramSocket socket;
    protected UdpSocketProperties socketProperties;

    public AbstractUdpClient(UdpSocketProperties socketProperties, String host, Integer port) throws ConnectionException
    {
        super(host, port);
        this.socketProperties = socketProperties;
    }

    protected DatagramSocket configureSocket(DatagramSocket socket) throws ConnectionException
    {
        try
        {
            if (socket == null)
            {
                throw new IllegalStateException("Socket must be created before being configured");
            }

            if (socketProperties.getSendBufferSize() != null)
            {
                socket.setSendBufferSize(socketProperties.getSendBufferSize());
            }

            if (socketProperties.getReceiveBufferSize() != null)
            {
                socket.setReceiveBufferSize(socketProperties.getReceiveBufferSize());
            }

            socket.setBroadcast(socketProperties.getBroadcast());
            socket.setSoTimeout(socketProperties.getTimeout());
            return socket;
        }
        catch (SocketException e)
        {
            throw new ConnectionException("Could not create socket due to a SocketException");
        }
        catch (IOException e)
        {
            throw new ConnectionException("Could not create socket due to a IOException");
        }

    }

    public SocketAttributes getSocketAttributes()
    {
        return new SocketAttributes(socket);
    }
}
