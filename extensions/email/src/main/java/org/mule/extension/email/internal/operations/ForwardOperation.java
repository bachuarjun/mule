/*
 * Copyright (c) MuleSoft, Inc.  All rights reserved.  http://www.mulesoft.com
 * The software in this package is published under the terms of the CPAL v1.0
 * license, a copy of which has been included with this distribution in the
 * LICENSE.txt file.
 */
package org.mule.extension.email.internal.operations;

import static org.mule.extension.email.internal.util.EmailUtils.getAttributesFromMessage;
import org.mule.extension.email.internal.EmailAttributes;
import org.mule.extension.email.internal.builder.MessageBuilder;
import org.mule.extension.email.internal.exception.SenderException;
import org.mule.runtime.api.message.MuleMessage;

import java.util.HashMap;
import java.util.List;
import java.util.Optional;

import javax.mail.Message;
import javax.mail.MessagingException;
import javax.mail.Session;
import javax.mail.Transport;

/**
 * @since 4.0
 */
public class ForwardOperation
{

    public void forward(Session session,
                        MuleMessage muleMessage,
                        String subject,
                        String from,
                        List<String> toAddresses)
    {

        Optional<EmailAttributes> attributes = getAttributesFromMessage(muleMessage);

        if (subject == null)
        {
            subject = attributes.isPresent() ? attributes.get().getSubject() : "No Subject";
        }

        try
        {
            Message forward = MessageBuilder.newMessage(session)
                    .withSubject("Fwd: " + subject)
                    .fromAddresses(from)
                    .to(toAddresses)
                    .withAttachments(attributes.isPresent() ? attributes.get().getAttachments() : new HashMap<>())
                    .withContent(muleMessage.getPayload().toString())
                    .build();

            Transport.send(forward);
        }
        catch (MessagingException me)
        {
            throw new SenderException(me.getMessage(), me);
        }
    }
}
