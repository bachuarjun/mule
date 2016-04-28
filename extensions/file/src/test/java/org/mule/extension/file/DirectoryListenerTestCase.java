/*
 * Copyright (c) MuleSoft, Inc.  All rights reserved.  http://www.mulesoft.com
 * The software in this package is published under the terms of the CPAL v1.0
 * license, a copy of which has been included with this distribution in the
 * LICENSE.txt file.
 */
package org.mule.extension.file;

import static org.apache.commons.io.FileUtils.write;
import static org.hamcrest.CoreMatchers.equalTo;
import static org.hamcrest.CoreMatchers.is;
import static org.junit.Assert.assertThat;
import static org.mule.extension.file.api.FileEventType.CREATE;
import static org.mule.extension.file.api.FileEventType.DELETE;
import static org.mule.extension.file.api.FileEventType.UPDATE;
import org.mule.extension.file.api.FileEventType;
import org.mule.extension.file.api.ListenerFileAttributes;
import org.mule.runtime.api.message.NullPayload;
import org.mule.runtime.api.metadata.DataType;
import org.mule.runtime.api.temporary.MuleMessage;
import org.mule.runtime.core.DefaultMuleMessage;
import org.mule.runtime.core.el.context.MessageContext;
import org.mule.runtime.core.util.FileUtils;
import org.mule.tck.probe.JUnitLambdaProbe;
import org.mule.tck.probe.PollingProber;

import java.io.File;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.Stack;

import org.apache.commons.io.IOUtils;
import org.junit.Test;

public class DirectoryListenerTestCase extends FileConnectorTestCase
{

    private static final String SUBFOLDER_CHILD_FILE = "child.txt";
    private static final String LISTENER_FOLDER_NAME = "listener";
    private static final String CREATED_FOLDER_NAME = "createdFolder";
    private static final String WATCH_FILE = "watchme.txt";
    private static final String WATCH_CONTENT = "who watches the watchmen?";

    private static Stack<MuleMessage<?, ListenerFileAttributes>> receivedMessage;

    private File createdFolder;
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
        temporaryFolder.newFolder(LISTENER_FOLDER_NAME);
        listenerFolder = Paths.get(temporaryFolder.getRoot().getAbsolutePath(), LISTENER_FOLDER_NAME).toString();
        createdFolder = new File(listenerFolder, CREATED_FOLDER_NAME);
        receivedMessage = new Stack<>();
    }

    @Override
    protected void doTearDown() throws Exception
    {
        receivedMessage = null;
    }

    @Test
    public void onFileCreated() throws Exception
    {
        write(new File(listenerFolder, WATCH_FILE), WATCH_CONTENT);
        assertEvent(listen(), CREATE, WATCH_CONTENT, WATCH_FILE);
    }

    @Test
    public void onFileUpdated() throws Exception
    {
        final String appendedContent = "\nNOBODY";

        onFileCreated();

        write(new File(listenerFolder, WATCH_FILE), appendedContent, true);
        assertEvent(listen(), UPDATE, WATCH_CONTENT + appendedContent, WATCH_FILE);
    }

    @Test
    public void onFileDeleted() throws Exception
    {
        onFileCreated();

        new File(listenerFolder, WATCH_FILE).delete();
        assertEvent(listen(), DELETE, NullPayload.getInstance(), WATCH_FILE);
    }

    @Test
    public void onDirectoryCreated() throws Exception
    {
        createdFolder.mkdir();
        assertEvent(listen(), CREATE, NullPayload.getInstance(), CREATED_FOLDER_NAME);
    }

    @Test
    public void onDirectoryDeleted() throws Exception
    {
        onDirectoryCreated();
        FileUtils.deleteTree(createdFolder);
        assertEvent(listen(), DELETE, NullPayload.getInstance(), CREATED_FOLDER_NAME);
    }

    @Test
    public void onDirectoryRenamed() throws Exception
    {
        onDirectoryCreated();
        final String updatedName = CREATED_FOLDER_NAME + "twist";

        Files.move(createdFolder.toPath(), new File(listenerFolder, updatedName).toPath());
        assertEvent(listen(), DELETE, NullPayload.getInstance(), CREATED_FOLDER_NAME);
        assertEvent(listen(), CREATE, NullPayload.getInstance(), updatedName);
    }

    @Test
    public void onCreateFileAtSubfolder() throws Exception
    {
        onDirectoryCreated();
        write(new File(createdFolder, SUBFOLDER_CHILD_FILE), WATCH_CONTENT);
        assertEvent(listen(), UPDATE, NullPayload.getInstance(), CREATED_FOLDER_NAME);
    }

    @Test
    public void onDeleteFileAtSubfolder() throws Exception
    {
        onDirectoryCreated();
        FileUtils.deleteTree(createdFolder);
        assertEvent(listen(), DELETE, NullPayload.getInstance(), CREATED_FOLDER_NAME);
    }

    private void assertEvent(MuleMessage<?, ListenerFileAttributes> message, FileEventType type, Object expectedContent, String fileName) throws Exception
    {
        Object payload = message.getPayload();
        if (payload instanceof InputStream)
        {
            payload = IOUtils.toString((InputStream) payload);
        }

        assertThat(payload, equalTo(expectedContent));
        ListenerFileAttributes attributes = message.getAttributes();

        assertThat(attributes.getPath().endsWith("/" + fileName), is(true));
        assertThat(attributes.getEventType(), is(type));
    }

    private MuleMessage<?, ListenerFileAttributes> listen()
    {
        PollingProber prober = new PollingProber(5000, 100);
        prober.check(new JUnitLambdaProbe(() -> receivedMessage.peek() != null, "Event was not received"));

        return receivedMessage.pop();
    }

    public static void onMessage(MessageContext messageContext)
    {
        MuleMessage message = new DefaultMuleMessage(messageContext.getPayload(), (DataType<Object>) messageContext.getDataType(), messageContext.getAttributes());
        receivedMessage.push(message);
    }
}
