/*
 * Copyright (c) MuleSoft, Inc.  All rights reserved.  http://www.mulesoft.com
 * The software in this package is published under the terms of the CPAL v1.0
 * license, a copy of which has been included with this distribution in the
 * LICENSE.txt file.
 */
package org.mule.module.socket.api.client;

import static org.mule.module.socket.internal.NetworkUtils.getSocketAddress;
import org.mule.module.socket.api.exceptions.UnresolvableHostException;
import org.mule.module.socket.api.protocol.TcpProtocol;
import org.mule.module.socket.api.tcp.TcpClientSocketProperties;
import org.mule.runtime.api.connection.ConnectionException;

import java.io.BufferedOutputStream;
import java.io.IOException;
import java.net.InetSocketAddress;
import java.net.Socket;
import java.net.SocketException;

public class TcpRequesterClient extends AbstractSocketClient implements RequesterSocket
{

    private Socket socket;
    private TcpClientSocketProperties socketProperties;
    private TcpProtocol protocol;

    public TcpRequesterClient(TcpClientSocketProperties socketProperties, TcpProtocol protocol, String host, Integer port) throws ConnectionException
    {
        super(host, port);
        this.socket = new Socket();
        this.protocol = protocol;
        this.socketProperties = socketProperties;
        configureSocket();
    }

    private void configureSocket() throws ConnectionException
    {
        try
        {
            if (socketProperties.getKeepAlive() != null)
            {
                socket.setKeepAlive(socketProperties.getKeepAlive());
            }

            if (socketProperties.getSendBufferSize() != null)
            {
                socket.setSendBufferSize(socketProperties.getSendBufferSize());
            }

            if (socketProperties.getReceiveBufferSize() != null)
            {
                socket.setReceiveBufferSize(socketProperties.getReceiveBufferSize());
            }

            if (socketProperties.getLinger() != null)
            {
                socket.setSoLinger(true, socketProperties.getLinger());
            }


            socket.setTcpNoDelay(socketProperties.getSendTcpNoDelay());
            socket.setSoTimeout(socketProperties.getTimeout());
            socket.bind(null);
        }
        catch (SocketException e)
        {
            e.printStackTrace();
            throw new ConnectionException("Could not create socket due to a SocketException");
        }

        catch (IOException e)
        {
            e.printStackTrace();
            throw new ConnectionException("Could not create socket due to a IOException");
        }

    }

    public void connect() throws ConnectionException, UnresolvableHostException
    {

        try
        {
            InetSocketAddress address = getSocketAddress(socketProperties.getFailOnUnresolvedHost(), host, port);
            // TODO validate address InetAddressValidator from Apache commons
            socket.connect(address);
        }
        catch (IOException e)
        {
            e.printStackTrace();
            throw new ConnectionException(String.format("Could not connect to host '%s' on port '%d'", host, port));
        }
    }

    public void write(Object data) throws ConnectionException
    {
        BufferedOutputStream bos = null;
        try
        {
            bos = new BufferedOutputStream(socket.getOutputStream());
            protocol.write(bos, data);
            bos.flush();
        }
        catch (IOException e)
        {
            e.printStackTrace();
            throw new ConnectionException("An error ocured while trying to write into the socket");
        }
    }

    public void disconnect() throws ConnectionException
    {
        // todo same code as udpclient disconnect
        if (!socket.isConnected())
        {
            throw new ConnectionException("Trying to disconnect socket but it was not connected");
        }

        try
        {
            socket.close();
        }
        catch (IOException e)
        {
            throw new ConnectionException("An error ocurred when trying to close the socket");
        }
    }

    @Override
    public boolean isValid()
    {
        return socket.isConnected();
    }
}
