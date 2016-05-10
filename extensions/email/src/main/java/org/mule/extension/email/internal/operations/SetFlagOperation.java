/*
 * Copyright (c) MuleSoft, Inc.  All rights reserved.  http://www.mulesoft.com
 * The software in this package is published under the terms of the CPAL v1.0
 * license, a copy of which has been included with this distribution in the
 * LICENSE.txt file.
 */
package org.mule.extension.email.internal.operations;

import static java.lang.String.format;
import static org.mule.extension.email.internal.util.EmailUtils.getAttributesFromMessage;
import org.mule.extension.email.internal.EmailAttributes;
import org.mule.extension.email.internal.exception.EmailException;
import org.mule.runtime.api.message.MuleMessage;

import java.util.List;
import java.util.Optional;

import javax.mail.Flags.Flag;
import javax.mail.Folder;
import javax.mail.Message;
import javax.mail.MessagingException;

public class SetFlagOperation
{

    public void set(MuleMessage muleMessage, Folder folder, Integer emailId, Flag flag, boolean expunge)
    {
        Object payload = muleMessage.getPayload();
        if (payload instanceof List)
        {
            for (Object o : (List) payload)
            {
                if (o instanceof MuleMessage)
                {
                    setFlag((MuleMessage) o, folder, emailId, flag, expunge);
                }
                else
                {
                    throw new EmailException("Cannot perform operation for the incoming payload");
                }
            }
        }
        else
        {
            setFlag(muleMessage, folder, emailId, flag, expunge);
        }

        try
        {
            folder.close(expunge);
        }
        catch (MessagingException e)
        {
            throw new EmailException("Error while closing folder " + folder.getName());
        }

    }

    private void setFlag(MuleMessage muleMessage, Folder folder, Integer emailId, Flag flag, boolean expunge)
    {
        Optional<EmailAttributes> attributes = getAttributesFromMessage(muleMessage);

        if (attributes.isPresent())
        {
            emailId = attributes.get().getId();
        }
        else
        {
            if (emailId == null)
            {
                throw new EmailException("No emailId specified for the operation. Expecting email attributes in the incoming mule message or an explicit emailId value");
            }
        }

        try
        {
            Message message = folder.getMessage(emailId);
            message.setFlag(flag, true);
        }
        catch (MessagingException e)
        {
            throw new EmailException(format("Error while fetching email id [%s] ", emailId), e);
        }
    }
}
