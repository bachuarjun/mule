/*
 * Copyright (c) MuleSoft, Inc.  All rights reserved.  http://www.mulesoft.com
 * The software in this package is published under the terms of the CPAL v1.0
 * license, a copy of which has been included with this distribution in the
 * LICENSE.txt file.
 */
package org.mule.extension.file;

import static org.hamcrest.CoreMatchers.equalTo;
import static org.hamcrest.CoreMatchers.is;
import static org.junit.Assert.assertThat;
import org.mule.extension.file.api.ListenerEventType;
import org.mule.extension.file.api.ListenerFileAttributes;
import org.mule.runtime.api.metadata.DataType;
import org.mule.runtime.api.temporary.MuleMessage;
import org.mule.runtime.core.DefaultMuleMessage;
import org.mule.runtime.core.el.context.MessageContext;
import org.mule.runtime.core.util.FileUtils;
import org.mule.runtime.core.util.ValueHolder;
import org.mule.tck.probe.JUnitLambdaProbe;
import org.mule.tck.probe.PollingProber;

import java.io.File;
import java.io.InputStream;
import java.nio.file.Paths;

import org.apache.commons.io.IOUtils;
import org.junit.Test;

public class DirectoryListenerTestCase extends FileConnectorTestCase
{

    public static ValueHolder<MuleMessage<InputStream, ListenerFileAttributes>> receivedMessage;
    private static final String WATCH_FILE = "watchme.txt";
    private static final String WATCH_CONTENT = "who watches the watchmen?";

    private String listenerFolder;

    @Override
    protected String getConfigFile()
    {
        return "directory-listener-config.xml";
    }

    @Override
    protected void doSetUpBeforeMuleContextCreation() throws Exception
    {
        super.doSetUpBeforeMuleContextCreation();
        temporaryFolder.newFolder("listener");
        listenerFolder = Paths.get(temporaryFolder.getRoot().getAbsolutePath(), "listener").toString();
        receivedMessage = new ValueHolder<>();
    }

    @Override
    protected void doTearDown() throws Exception
    {
        receivedMessage = null;
    }

    @Test
    public void listenFileCreation() throws Exception
    {
        FileUtils.write(new File(listenerFolder, WATCH_FILE), WATCH_CONTENT);
        assertEvent(listen(), ListenerEventType.CREATE);
    }

    private void assertEvent(MuleMessage<InputStream, ListenerFileAttributes> message, ListenerEventType type) throws Exception
    {
        assertThat(IOUtils.toString(message.getPayload()), equalTo(WATCH_CONTENT));
        ListenerFileAttributes attributes = message.getAttributes();

        assertThat(attributes.getPath().endsWith("/" + WATCH_FILE), is(true));
        assertThat(attributes.getEventType(), is(type));
    }

    private MuleMessage<InputStream, ListenerFileAttributes> listen()
    {
        PollingProber prober = new PollingProber(5000, 100);
        prober.check(new JUnitLambdaProbe(() -> receivedMessage.get() != null, "Event was not received"));

        return receivedMessage.get();
    }

    public static void onMessage(MessageContext messageContext)
    {
        MuleMessage message = new DefaultMuleMessage(messageContext.getPayload(), (DataType<Object>) messageContext.getDataType(), messageContext.getAttributes());
        receivedMessage.set(message);
    }
}
