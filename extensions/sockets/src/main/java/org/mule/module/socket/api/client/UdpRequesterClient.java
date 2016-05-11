/*
 * Copyright (c) MuleSoft, Inc.  All rights reserved.  http://www.mulesoft.com
 * The software in this package is published under the terms of the CPAL v1.0
 * license, a copy of which has been included with this distribution in the
 * LICENSE.txt file.
 */
package org.mule.module.socket.api.client;

import static org.mule.module.socket.internal.NetworkUtils.getSocketAddressbyName;
import org.mule.module.socket.api.exceptions.UnresolvableHostException;
import org.mule.module.socket.api.udp.UdpSocketProperties;
import org.mule.module.socket.internal.SocketUtils;
import org.mule.runtime.api.connection.ConnectionException;

import java.io.IOException;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;
import java.net.SocketException;

public class UdpRequesterClient extends AbstractUdpClient implements RequesterSocket
{

    public UdpRequesterClient(UdpSocketProperties socketProperties, String host, Integer port) throws ConnectionException
    {
        super(socketProperties, host, port);

        try
        {
            socket = new DatagramSocket();
        }
        catch (SocketException e)
        {
            throw new ConnectionException("Could not create requester UDP socket");
        }

        this.socket = configureSocket(socket);
    }

    @Override
    public void write(Object data) throws ConnectionException
    {
        try
        {
            byte[] byteArray = SocketUtils.getByteArray(data, true);
            DatagramPacket sendPacket = createPacket(byteArray);
            socket.send(sendPacket);
        }
        catch (IOException e)
        {
            e.printStackTrace();
            throw new ConnectionException("An error ocured while trying to write into the socket");
        }
    }

    @Override
    public void connect() throws ConnectionException, UnresolvableHostException
    {
       // no op
    }

    @Override
    public boolean isValid()
    {
        return socket.isConnected();
    }

    @Override
    public void disconnect() throws ConnectionException
    {
        socket.close();
    }

    private DatagramPacket createPacket(byte[] content) throws UnresolvableHostException
    {
        DatagramPacket packet = new DatagramPacket(content, content.length);
        InetAddress address = getSocketAddressbyName(socketProperties.getFailOnUnresolvedHost(), host);
        packet.setAddress(address);
        packet.setPort(port);
        return packet;
    }
}
