/*
 * Copyright (c) MuleSoft, Inc.  All rights reserved.  http://www.mulesoft.com
 * The software in this package is published under the terms of the CPAL v1.0
 * license, a copy of which has been included with this distribution in the
 * LICENSE.txt file.
 */
package org.mule.extension.email.internal.operations;

import static org.mule.extension.email.internal.util.EmailUtils.getAttributesFromMessage;
import org.mule.extension.email.api.EmailContent;
import org.mule.extension.email.internal.EmailAttributes;
import org.mule.extension.email.internal.builder.MessageBuilder;
import org.mule.extension.email.internal.exception.SenderException;
import org.mule.runtime.api.message.MuleMessage;

import java.util.Calendar;
import java.util.List;

import javax.mail.Message;
import javax.mail.MessagingException;
import javax.mail.Session;
import javax.mail.Transport;

public final class ReplyOperation
{

    public static final String REPLY_ERROR = "No email attributes were found in the incoming message.";

    public void reply(Session session,
                      MuleMessage muleMessage,
                      EmailContent content,
                      String from,
                      Boolean replyToAll)
    {

        EmailAttributes attributes = getAttributesFromMessage(muleMessage)
                                        .orElseThrow(() -> new SenderException(REPLY_ERROR));
        try
        {
            List<String> replyTo = attributes.getReplyToAddresses();
            if (replyTo == null || replyTo.isEmpty())
            {
                replyTo = attributes.getToAddresses();
            }

            Message reply = MessageBuilder.newMessage(session)
                    .fromAddresses(from)
                    .withSubject(attributes.getSubject())
                    .replyTo(replyTo)
                    .withSentDate(Calendar.getInstance().getTime())
                    .build()
                    .reply(replyToAll);

            reply.setContent(content.getBody(), content.getContentType());
            Transport.send(reply);
        }
        catch (MessagingException e)
        {
            throw new SenderException(e.getMessage(), e);
        }
    }
}
