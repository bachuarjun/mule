/*
 * Copyright (c) MuleSoft, Inc.  All rights reserved.  http://www.mulesoft.com
 * The software in this package is published under the terms of the CPAL v1.0
 * license, a copy of which has been included with this distribution in the
 * LICENSE.txt file.
 */
package org.mule.module.socket.internal;

import org.mule.module.socket.api.exceptions.UnresolvableHostException;

import java.net.InetAddress;
import java.net.InetSocketAddress;
import java.net.UnknownHostException;

public class NetworkUtils
{

    public static InetSocketAddress getSocketAddress(boolean failOnUnresolvedHost, String host, int port) throws UnresolvableHostException
    {

        InetSocketAddress address = new InetSocketAddress(host, port);
        if (failOnUnresolvedHost && address.isUnresolved())
        {
            throw new UnresolvableHostException(String.format(
                    "Address consisting in host '%s' and port '%d' could not be resolved", host, port));
        }
        return address;
    }

    public static InetAddress getSocketAddressbyName(boolean failOnUnresolvedHost, String host)
            throws UnresolvableHostException
    {
        InetAddress address = null;
        try
        {
            address = InetAddress.getByName(host);
            return address;
        }
        catch (UnknownHostException e)
        {
            if (failOnUnresolvedHost)
            {
                throw new UnresolvableHostException(String.format("Host name '%s' could not be resolved", host));
            }
            else
            {
                return null;
            }
        }
    }
}
