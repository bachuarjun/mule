/*
 * Copyright (c) MuleSoft, Inc.  All rights reserved.  http://www.mulesoft.com
 * The software in this package is published under the terms of the CPAL v1.0
 * license, a copy of which has been included with this distribution in the
 * LICENSE.txt file.
 */
package org.mule.extension.email.internal.builder;

import static javax.mail.Message.RecipientType.BCC;
import static javax.mail.Message.RecipientType.CC;
import static javax.mail.Message.RecipientType.TO;
import static javax.mail.Part.ATTACHMENT;
import static javax.mail.Part.INLINE;
import static org.mule.extension.email.internal.util.EmailConstants.MULTIPART;
import static org.mule.extension.email.internal.util.EmailUtils.toAddressArray;
import static org.mule.runtime.core.util.IOUtils.toDataHandler;
import org.mule.extension.email.api.EmailAttachment;
import org.mule.extension.email.internal.exception.EmailException;
import org.mule.extension.email.internal.util.EmailUtils;
import org.mule.runtime.core.transformer.types.MimeTypes;

import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.activation.DataHandler;
import javax.mail.MessagingException;
import javax.mail.Session;
import javax.mail.internet.MimeBodyPart;
import javax.mail.internet.MimeMessage;
import javax.mail.internet.MimeMultipart;

public class MessageBuilder
{
    private static final String ERROR = "Error while creating Message";

    private final MimeMessage message;

    private Map<String, DataHandler> attachments;
    private String content = "";
    private String contentType = MimeTypes.TEXT;

    private MessageBuilder(Session s) throws MessagingException
    {
        this.message = new MimeMessage(s);
    }

    private MessageBuilder(MimeMessage message) throws MessagingException
    {
        this.message = message;
    }

    public static MessageBuilder newMessage(Session session)
    {
        try
        {
            return new MessageBuilder(session);
        }
        catch (MessagingException e)
        {
            throw new EmailException(ERROR, e);
        }
    }

    public MessageBuilder withSubject(String subject) throws MessagingException
    {
        this.message.setSubject(subject);
        return this;
    }

    public MessageBuilder fromAddresses(List<String> fromAddresses) throws MessagingException
    {
        this.message.addFrom(toAddressArray(fromAddresses));
        return this;
    }

    public MessageBuilder fromAddresses(String from) throws MessagingException
    {
        if (from != null)
        {
            this.message.setFrom(EmailUtils.toAddress(from));
        }
        else
        {
            this.message.setFrom();
        }
        return this;
    }

    public MessageBuilder to(List<String> toAddresses) throws MessagingException
    {
        this.message.setRecipients(TO, toAddressArray(toAddresses));
        return this;
    }

    public MessageBuilder bcc(List<String> bccAddresses) throws MessagingException
    {
        this.message.setRecipients(BCC, toAddressArray(bccAddresses));
        return this;
    }

    public MessageBuilder cc(List<String> ccAddresses) throws MessagingException
    {
        this.message.setRecipients(CC, toAddressArray(ccAddresses));
        return this;
    }

    public MessageBuilder withHeaders(Map<String, String> headers) throws MessagingException
    {
        for (String h : headers.keySet())
        {
            this.message.addHeader(h, headers.get(h));
        }
        return this;
    }

    public MessageBuilder withAttachments(List<EmailAttachment> attachments)
    {
        Map<String, DataHandler> attachmentsMap = new HashMap<>();
        attachments.forEach(a -> {
            try
            {
                DataHandler dataHandler = toDataHandler(a.getId(), a.getData(), a.getContentType());
                attachmentsMap.put(a.getId(), dataHandler);
            }
            catch (Exception e)
            {
                throw new EmailException(ERROR + " could not add attachments", e);
            }
        });
        this.attachments = attachmentsMap;
        return this;
    }

    public MessageBuilder withAttachments(Map<String, DataHandler> attachments)
    {
        this.attachments = attachments;
        return this;
    }

    public MessageBuilder withSentDate(Date date) throws MessagingException
    {
        this.message.setSentDate(date);
        return this;
    }

    public MessageBuilder withContent(String content, String contentType) throws MessagingException
    {
        this.content = content;
        this.contentType = contentType;
        return this;
    }

    public MessageBuilder withContent(String content) throws MessagingException
    {
        this.content = content;
        return this;
    }

    public MessageBuilder replyTo(List<String> replyAddresses) throws MessagingException
    {
        this.message.setReplyTo(toAddressArray(replyAddresses));
        return this;
    }

    public MimeMessage build() throws MessagingException
    {
        DataHandler contentDataHandler = new DataHandler(content, contentType);
        if (attachments != null && !attachments.isEmpty())
        {
            // first part of the message is the content
            MimeMultipart multipart = new MimeMultipart();
            MimeBodyPart mimeBodyPart = new MimeBodyPart();

            mimeBodyPart.setDisposition(INLINE);
            mimeBodyPart.setDataHandler(contentDataHandler);
            multipart.addBodyPart(mimeBodyPart);

            for (String attachment : attachments.keySet())
            {
                try
                {
                    mimeBodyPart = new MimeBodyPart();
                    mimeBodyPart.setDisposition(ATTACHMENT);
                    mimeBodyPart.setFileName(attachment);
                    mimeBodyPart.setDataHandler(attachments.get(attachment));
                    multipart.addBodyPart(mimeBodyPart);
                }
                catch (Exception e)
                {
                    throw new EmailException("Error while adding attachment: " + attachment, e);
                }
            }

            this.message.setContent(multipart, MULTIPART);
        }
        else
        {
            this.message.setDataHandler(contentDataHandler);
        }

        return message;
    }

}
