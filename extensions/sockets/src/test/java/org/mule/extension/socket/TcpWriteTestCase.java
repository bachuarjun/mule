/*
 * Copyright (c) MuleSoft, Inc.  All rights reserved.  http://www.mulesoft.com
 * The software in this package is published under the terms of the CPAL v1.0
 * license, a copy of which has been included with this distribution in the
 * LICENSE.txt file.
 */
package org.mule.extension.socket;

import static org.junit.Assert.assertEquals;
import org.mule.module.socket.api.source.SocketAttributes;
import org.mule.runtime.api.message.MuleMessage;
import org.mule.runtime.api.metadata.DataType;
import org.mule.runtime.core.DefaultMuleMessage;
import org.mule.runtime.core.el.context.MessageContext;
import org.mule.runtime.core.util.ValueHolder;
import org.mule.tck.probe.JUnitLambdaProbe;
import org.mule.tck.probe.PollingProber;

import java.util.List;
import java.util.concurrent.CopyOnWriteArrayList;

import org.junit.Test;

public class TcpWriteTestCase extends SocketExtensionTestCase
{

    private static final int TIMEOUT_MILLIS = 5000;
    private static final int POLL_DELAY_MILLIS = 100;
    public static final String TEST_STRING = "test string";
    private static String HOST = "localhost";
    private static int SOURCE_PORT = 8005;
    private static int LISTENING_PORT = 8006;
    private static int LISTENING_UDP_PORT = 8007;
    private static int UDP_SOURCE_PORT = 8008;
    private static List<MuleMessage<?, SocketAttributes>> receivedMessages;

    @Override
    protected void doSetUpBeforeMuleContextCreation() throws Exception
    {
        super.doSetUpBeforeMuleContextCreation();
        receivedMessages = new CopyOnWriteArrayList<>();
    }

    @Override
    protected String getConfigFile()
    {
        return "tcp-write-config.xml";
    }

    @Test
    public void tcpListen() throws Exception
    {
        flowRunner("tcp-write").withPayload(TEST_STRING).run();
        assertEvent(receiveConnection(), TEST_STRING);
    }

    @Test
    public void udpListen() throws Exception
    {
        flowRunner("udp-write").withPayload(TEST_STRING).run();
        assertEvent(receiveConnection(), TEST_STRING);
    }

    public static void onIncomingConnection(MessageContext messageContext)
    {
        MuleMessage message = new DefaultMuleMessage(messageContext.getPayload(), (DataType<Object>) messageContext.getDataType(), messageContext.getAttributes());
        receivedMessages.add(message);
    }

    private void assertEvent(MuleMessage<?, SocketAttributes> message, Object expectedContent) throws Exception
    {
        String payload = new String((byte[]) message.getPayload());
        assertEquals(payload, expectedContent);
    }

    private MuleMessage<?, SocketAttributes> receiveConnection()
    {
        PollingProber prober = new PollingProber(TIMEOUT_MILLIS, POLL_DELAY_MILLIS);
        ValueHolder<MuleMessage<?, SocketAttributes>> messageHolder = new ValueHolder<>();
        prober.check(new JUnitLambdaProbe(() -> {
            for (MuleMessage<?, SocketAttributes> message : receivedMessages)
            {
                SocketAttributes attributes = message.getAttributes();
                messageHolder.set(message);
                return true;
            }

            return false;
        }));

        return messageHolder.get();
    }
}
