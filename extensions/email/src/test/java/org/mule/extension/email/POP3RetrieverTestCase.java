/*
 * Copyright (c) MuleSoft, Inc.  All rights reserved.  http://www.mulesoft.com
 * The software in this package is published under the terms of the CPAL v1.0
 * license, a copy of which has been included with this distribution in the
 * LICENSE.txt file.
 */

package org.mule.extension.email;

import static org.hamcrest.Matchers.hasSize;
import static org.hamcrest.core.Is.is;
import static org.hamcrest.core.IsInstanceOf.instanceOf;
import static org.junit.Assert.assertThat;
import org.mule.runtime.api.message.MuleMessage;
import org.mule.runtime.core.api.MuleEvent;

import com.icegreen.greenmail.util.ServerSetup;

import java.util.List;

import org.junit.Test;

public class POP3RetrieverTestCase extends AbstractEmailRetrieverTestCase
{

    @Override
    protected String getConfigFile()
    {
        return "pop3.xml";
    }

    @Test
    public void retrieveAndRead() throws Exception
    {
        deliver10To(JUANI_EMAIL);
        MuleEvent event = runFlow(RETRIEVE_AND_READ);
        List<MuleMessage> messages = (List<MuleMessage>) event.getMessage().getPayload();
        assertThat(messages, hasSize(10));

        messages.forEach(m -> {
            assertThat(m.getPayload(), instanceOf(String.class));
            assertThat(m.getPayload(), is(CONTENT));
        });
    }

    @Test
    public void retrieveAndThenExpungeDelete() throws Exception
    {
        deliver10To(JUANI_EMAIL);
        assertThat(server.getReceivedMessages().length, is(10));
        runFlow(RETRIEVE_AND_THEN_EXPUNGE_DELETE);
        assertThat(server.getReceivedMessages().length, is(0));
    }

    @Override
    public ServerSetup getServerSetup()
    {
        return new ServerSetup(DEFAULT_TEST_PORT, null, "pop3");
    }
}
