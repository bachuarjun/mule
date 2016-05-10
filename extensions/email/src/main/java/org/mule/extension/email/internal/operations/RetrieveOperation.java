/*
 * Copyright (c) MuleSoft, Inc.  All rights reserved.  http://www.mulesoft.com
 * The software in this package is published under the terms of the CPAL v1.0
 * license, a copy of which has been included with this distribution in the
 * LICENSE.txt file.
 */
package org.mule.extension.email.internal.operations;

import static java.lang.String.format;
import static org.mule.extension.email.internal.builder.EmailAttributesBuilder.fromMessage;
import static org.mule.extension.email.internal.util.EmailUtils.getBody;
import org.mule.extension.email.internal.EmailAttributes;
import org.mule.extension.email.internal.exception.RetrieverException;
import org.mule.runtime.api.message.MuleMessage;
import org.mule.runtime.core.DefaultMuleMessage;
import org.mule.runtime.core.api.MuleContext;

import java.util.ArrayList;
import java.util.List;

import javax.mail.Folder;
import javax.mail.Message;
import javax.mail.MessagingException;

public class RetrieveOperation
{

    public List<MuleMessage<String, EmailAttributes>> retrieve(Folder folder, MuleContext context, boolean readContent)
    {
        try
        {
            List<MuleMessage<String, EmailAttributes>> list = new ArrayList<>();
            for (Message m : folder.getMessages())
            {
                MuleMessage muleMessage = readContent ? new DefaultMuleMessage(getBody(m), null, fromMessage(m), context)
                                                      : new DefaultMuleMessage("", null, fromMessage(m, false), context);

                list.add(muleMessage);
            }
            folder.close(false);
            return list;
        }
        catch (MessagingException me)
        {
            throw new RetrieverException(format("could not get messages from the folder %s", folder), me);
        }
    }
}
