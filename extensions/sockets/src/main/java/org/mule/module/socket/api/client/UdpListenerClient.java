/*
 * Copyright (c) MuleSoft, Inc.  All rights reserved.  http://www.mulesoft.com
 * The software in this package is published under the terms of the CPAL v1.0
 * license, a copy of which has been included with this distribution in the
 * LICENSE.txt file.
 */
package org.mule.module.socket.api.client;

import static org.mule.module.socket.internal.NetworkUtils.getSocketAddressbyName;
import org.mule.module.socket.api.exceptions.MisconfiguredSocketException;
import org.mule.module.socket.api.exceptions.UnresolvableHostException;
import org.mule.module.socket.api.source.SocketAttributes;
import org.mule.module.socket.api.udp.UdpSocketProperties;
import org.mule.module.socket.internal.ConnectionEvent;
import org.mule.runtime.api.connection.ConnectionException;

import java.io.IOException;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;
import java.net.SocketException;
import java.util.Arrays;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class UdpListenerClient extends AbstractUdpClient implements ListenerSocket
{

    private static final Logger LOGGER = LoggerFactory.getLogger(UdpListenerClient.class);


    public UdpListenerClient(UdpSocketProperties socketProperties, String host, Integer port) throws ConnectionException
    {
        super(socketProperties, host, port);

        try
        {
            socket = new DatagramSocket(port);
        }
        catch (SocketException e)
        {
            throw new ConnectionException(String.format("Could not create listener UDP socket on port '%d'", port));
        }

        this.socket = configureSocket(socket);
    }

    public ConnectionEvent receive() throws UnresolvableHostException, ConnectionException
    {

        if (!socket.isBound() || socket.isClosed())
        {
            throw new MisconfiguredSocketException("Socket must be binded before receiving a datagram");
        }

        DatagramPacket packet = createPacket();
        try
        {
            if (socketProperties.getTimeout() > 0)
            {
                socket.setSoTimeout(socketProperties.getTimeout());
            }
            socket.receive(packet);

            if (packet.getLength() > 0)
            {
                return new ConnectionEvent(Arrays.copyOf(packet.getData(), packet.getLength()), new SocketAttributes(socket));
            }
            else
            {
                LOGGER.debug(String.format("Received packet without content from host '%s' port '%d'", host, port));
                return new ConnectionEvent(new SocketAttributes(socket));
            }
        }
        catch (IOException e)
        {
            //LOGGER.error(String.format("An error occurred when receiving from UDP socket listening in host '%s' port '%d'", host, port));
            return new ConnectionEvent(new SocketAttributes(socket));
        }
    }

    protected DatagramPacket createPacket() throws UnresolvableHostException
    {
        int bufferSize = socketProperties.getReceiveBufferSize();
        DatagramPacket packet = new DatagramPacket(new byte[bufferSize], bufferSize);
        InetAddress address = getSocketAddressbyName(socketProperties.getFailOnUnresolvedHost(), host);
        packet.setAddress(address);
        return packet;
    }


    public void disconnect() throws ConnectionException
    {
        if (!socket.isConnected())
        {
            LOGGER.debug("Trying to disconnect UDP listener socket but it was not connected");
        }

        socket.close();

    }

    @Override
    public boolean isValid()
    {
        return socket.isBound() && !socket.isClosed();
    }
}
