/*
 * Copyright (c) MuleSoft, Inc.  All rights reserved.  http://www.mulesoft.com
 * The software in this package is published under the terms of the CPAL v1.0
 * license, a copy of which has been included with this distribution in the
 * LICENSE.txt file.
 */
package org.mule.extension.email.api.retriever;

import static javax.mail.Flags.Flag.DELETED;
import static javax.mail.Flags.Flag.SEEN;
import static javax.mail.Folder.READ_ONLY;
import static javax.mail.Folder.READ_WRITE;
import org.mule.extension.email.internal.EmailAttributes;
import org.mule.extension.email.internal.operations.RetrieveOperation;
import org.mule.extension.email.internal.operations.SetFlagOperation;
import org.mule.runtime.api.message.MuleMessage;
import org.mule.runtime.core.api.MuleContext;
import org.mule.runtime.extension.api.annotation.param.Connection;
import org.mule.runtime.extension.api.annotation.param.Optional;

import java.util.List;

import javax.inject.Inject;

public class RetrieverOperations
{

    @Inject
    private MuleContext context;

    /**
     * List all the emails and opens its content
     * @param connection
     * @return
     */
    public List<MuleMessage<String, EmailAttributes>> retrieve(@Connection RetrieverConnection connection, @Optional(defaultValue = "true") boolean readContent)
    {
        return new RetrieveOperation().retrieve(connection.getOpenFolder(READ_ONLY), context, readContent);
    }

    /**
     * Marks an incoming email as READ
     * @param message
     * @param connection
     * @param emailId
     * @return
     */
    public void read(MuleMessage message, @Connection RetrieverConnection connection, @Optional Integer emailId)
    {
        new SetFlagOperation().set(message, connection.getOpenFolder(READ_WRITE), emailId, SEEN, false);
    }

    /**
     * Marks an incoming email as DELETED
     * @param message
     * @param connection
     * @param emailId
     * @return
     */
    public void delete(MuleMessage message, @Connection RetrieverConnection connection, @Optional Integer emailId, @Optional(defaultValue = "false") boolean expunge)
    {
        new SetFlagOperation().set(message, connection.getOpenFolder(READ_WRITE), emailId, DELETED, expunge);
    }
}
