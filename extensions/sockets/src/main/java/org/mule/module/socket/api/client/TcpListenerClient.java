/*
 * Copyright (c) MuleSoft, Inc.  All rights reserved.  http://www.mulesoft.com
 * The software in this package is published under the terms of the CPAL v1.0
 * license, a copy of which has been included with this distribution in the
 * LICENSE.txt file.
 */
package org.mule.module.socket.api.client;

import org.mule.module.socket.api.protocol.TcpProtocol;
import org.mule.module.socket.api.source.SocketAttributes;
import org.mule.module.socket.api.tcp.TcpServerSocketProperties;
import org.mule.module.socket.internal.ConnectionEvent;
import org.mule.module.socket.internal.TcpInputStream;
import org.mule.runtime.api.connection.ConnectionException;

import java.io.BufferedInputStream;
import java.io.DataInputStream;
import java.io.IOException;
import java.net.InetSocketAddress;
import java.net.ServerSocket;
import java.net.Socket;
import java.net.SocketException;
import java.net.SocketTimeoutException;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class TcpListenerClient extends AbstractTcpClient implements ListenerSocket
{

    private static final Logger LOGGER = LoggerFactory.getLogger(TcpListenerClient.class);

    private ServerSocket socket;
    private TcpServerSocketProperties socketProperties;


    public TcpListenerClient(TcpServerSocketProperties socketProperties, TcpProtocol protocol, String host, Integer port) throws ConnectionException
    {
        super(protocol, host, port);
        this.socketProperties = socketProperties;
        configureSocket();
    }

    public void configureSocket() throws ConnectionException
    {
        try
        {
            this.socket = new ServerSocket();

            if (socketProperties.getTimeout() != null)
            {
                socket.setSoTimeout(socketProperties.getTimeout());
            }

            if (socketProperties.getReceiveBufferSize() != null)
            {

                socket.setReceiveBufferSize(socketProperties.getReceiveBufferSize());
            }

            if (socketProperties.getReuseAddress() != null)
            {

                socket.setReuseAddress(socketProperties.getReuseAddress());
            }
        }
        catch (SocketException e)
        {
            throw new ConnectionException("Could not configure socket");
        }
        catch (IOException e)
        {
            throw new ConnectionException("Could not create socket");
        }

        try
        {
            socket.bind(new InetSocketAddress(host, port), socketProperties.getReceiveBacklog());
        }
        catch (IOException e)
        {
            throw new ConnectionException(String.format("Could not bind socket to host '%s' and port '%d'", host, port));
        }
    }

    private Socket listen() throws ConnectionException
    {
        try
        {
            // blocking
            Socket newConnection = socket.accept();
            configureIncomingConnection(newConnection);
            return newConnection;
        }
        catch (SocketTimeoutException e)
        {
            // do nothing because timeout is configurable
            LOGGER.debug("Socket timed out while listening for a new connection");
            return null;
        }
        catch (IOException e)
        {
            // this is the expected behaviour while closing the socket
            return null;
        }
        catch (ConnectionException e)
        {
            throw e;
        }
    }

    private void configureIncomingConnection(Socket newConnection) throws ConnectionException
    {
        try
        {
            if (socketProperties.getTimeout() != null)
            {
                newConnection.setSoTimeout(socketProperties.getTimeout());
            }
        }
        catch (SocketException e)
        {
            throw new ConnectionException("Could not configure incoming connection");
        }
    }

    @Override
    public ConnectionEvent receive() throws ConnectionException, IOException
    {

        Socket socket = listen();

        if (socket == null)
        {
            return new ConnectionEvent();
        }
        DataInputStream underlyingIs = new DataInputStream(new BufferedInputStream(socket.getInputStream()));
        TcpInputStream tis = new TcpInputStream(underlyingIs);

        try
        {
            return new ConnectionEvent(protocol.read(tis), new SocketAttributes(socket));
        }
        catch (IOException e)
        {
            if (protocol.getRethrowExceptionOnRead())
            {
                throw e;
            }

            return new ConnectionEvent(new SocketAttributes(socket));
        }
        finally
        {
            if (!tis.isStreaming())
            {
                tis.close();
            }
        }
    }

    public void disconnect() throws ConnectionException
    {
        try
        {
            if (!socket.isClosed())
            {
                socket.close();
            }
        }
        catch (IOException e)
        {
            throw new ConnectionException("An error occurred when closing the listener socket");
        }
    }

    @Override
    public boolean isValid()
    {
        return !socket.isClosed() && socket.isBound();
    }
}
