/*
 * Copyright (c) MuleSoft, Inc.  All rights reserved.  http://www.mulesoft.com
 * The software in this package is published under the terms of the CPAL v1.0
 * license, a copy of which has been included with this distribution in the
 * LICENSE.txt file.
 */
package org.mule.module.socket.internal;

import org.mule.runtime.api.message.MuleMessage;
import org.mule.runtime.core.util.IOUtils;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;

public class SocketUtils
{

    public static void write(OutputStream os, Object data, boolean payloadOnly, boolean streamOk, String protocolName) throws IOException
    {
        writeByteArray(os, getByteArray(data, payloadOnly));
        // todo check if protocol handles streaming

        //if (data instanceof InputStream)
        //{
        //    if (streamOk)
        //    {
        //        writeByteArray(os, getByteArray(data, payloadOnly));
        //    }
        //    else
        //    {
        //        throw new IOException(String.format("TCP protocol '%s' cannot handle streaming", protocolName));
        //    }
        //}
        //else {
        //    writeByteArray(os, getByteArray(data, payloadOnly));
        //}
    }

    protected static void writeByteArray(OutputStream os, byte[] data) throws IOException
    {
        os.write(data);
    }

    protected static void writeByteArray(OutputStream os, String data) throws IOException
    {
        writeByteArray(os, data.getBytes());
    }


    protected static void writeMuleMessage(OutputStream os, MuleMessage message, boolean payloadOnly, boolean streamOk, String protocolName) throws IOException
    {
        if (payloadOnly)
        {
            write(os, ((MuleMessage) message).getPayload(), payloadOnly, streamOk, protocolName);
        }
        else
        {
            // TODO write mule message write(os, data.by);
        }
    }

    public static byte[] getByteArray(Object data, boolean payloadOnly)
    {
        if (data instanceof InputStream)
        {
            return IOUtils.toByteArray((InputStream) data);

        }
        else if (data instanceof MuleMessage)
        {
            if (payloadOnly)
            {
                return getByteArray(((MuleMessage) data).getPayload(), payloadOnly);
            }
            else
            {
                // TODO write mule message write(os, data.by);
            }
        }
        else if (data instanceof byte[])
        {
            return (byte[]) data;
        }
        else if (data instanceof String)
        {
            return ((String) data).getBytes();
        }

        throw new IllegalArgumentException(String.format("Cannot serialize data: '%s'", data));
    }
}
