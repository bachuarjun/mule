/*
 * Copyright (c) MuleSoft, Inc.  All rights reserved.  http://www.mulesoft.com
 * The software in this package is published under the terms of the CPAL v1.0
 * license, a copy of which has been included with this distribution in the
 * LICENSE.txt file.
 */
package org.mule.extension.email.internal.builder;

import static javax.mail.Flags.Flag.ANSWERED;
import static javax.mail.Flags.Flag.DELETED;
import static javax.mail.Flags.Flag.DRAFT;
import static javax.mail.Flags.Flag.RECENT;
import static javax.mail.Flags.Flag.SEEN;
import static javax.mail.Message.RecipientType.BCC;
import static javax.mail.Message.RecipientType.CC;
import static javax.mail.Message.RecipientType.TO;
import static org.mule.extension.email.internal.util.EmailUtils.getAttachments;
import org.mule.extension.email.internal.EmailAttributes;
import org.mule.extension.email.internal.EmailFlags;
import org.mule.extension.email.internal.exception.EmailException;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.activation.DataHandler;
import javax.mail.Address;
import javax.mail.Flags;
import javax.mail.Message;
import javax.mail.MessagingException;

public class EmailAttributesBuilder
{

    private int id;
    private String subject;
    private List<String> from = new ArrayList<>();
    private List<String> to = new ArrayList<>();
    private List<String> bcc = new ArrayList<>();
    private List<String> cc = new ArrayList<>();
    private Map<String, String> headers = new HashMap<>();
    private Map<String, DataHandler> attachments;
    private LocalDateTime receivedDate;
    private List<String> replyTo = new ArrayList<>();
    private boolean answered;
    private boolean deleted;
    private boolean draft;
    private boolean recent;
    private boolean seen;

    private EmailAttributesBuilder()
    {
    }

    public EmailAttributesBuilder withId(int id)
    {
        this.id = id;
        return this;
    }

    public EmailAttributesBuilder withSubject(String subject)
    {
        this.subject = subject;
        return this;
    }

    public EmailAttributesBuilder fromAddresses(Address[] fromAddresses)
    {
        addArrayAddresses(fromAddresses, from);
        return this;
    }

    public EmailAttributesBuilder toAddresses(Address[] toAddresses)
    {
        addArrayAddresses(toAddresses, to);
        return this;
    }

    public EmailAttributesBuilder bccAddresses(Address[] bccAddresses)
    {
        addArrayAddresses(bccAddresses, bcc);
        return this;
    }

    public EmailAttributesBuilder ccAddresses(Address[] ccAddresses)
    {
        addArrayAddresses(ccAddresses, cc);
        return this;
    }

    public EmailAttributesBuilder setHeaders(Map<String, String> headers)
    {
        this.headers = headers;
        return this;
    }

    public EmailAttributesBuilder withAttachments(Map<String, DataHandler> attachments)
    {
        this.attachments = attachments;
        return this;
    }

    public EmailAttributesBuilder receivedDate(LocalDateTime receivedDate)
    {
        this.receivedDate = receivedDate;
        return this;
    }

    public EmailAttributesBuilder replyToAddress(Address[] replyToAddresses)
    {
        addArrayAddresses(replyToAddresses, replyTo);
        return this;
    }

    public EmailAttributesBuilder answered(boolean answered)
    {
        this.answered = answered;
        return this;
    }

    public EmailAttributesBuilder deleted(boolean deleted)
    {
        this.deleted = deleted;
        return this;
    }

    public EmailAttributesBuilder draft(boolean draft)
    {
        this.draft = draft;
        return this;
    }

    public EmailAttributesBuilder recent(boolean recent)
    {
        this.recent = recent;
        return this;
    }

    public EmailAttributesBuilder seen(boolean seen)
    {
        this.seen = seen;
        return this;
    }

    public EmailAttributes build()
    {
        return new EmailAttributes(id,
                                   subject,
                                   from,
                                   to,
                                   bcc,
                                   cc,
                                   headers,
                                   attachments,
                                   receivedDate,
                                   new EmailFlags(answered, deleted, draft, recent, seen),
                                   replyTo);
    }

    public static EmailAttributes fromMessage(Message msg, boolean withAttachments)
    {
        try
        {
            Flags flags = msg.getFlags();
            EmailAttributesBuilder builder = EmailAttributesBuilder.newAttributes()
                    .withId(msg.getMessageNumber())
                    .withSubject(msg.getSubject())
                    .fromAddresses(msg.getFrom())
                    .toAddresses(msg.getRecipients(TO))
                    .ccAddresses(msg.getRecipients(CC))
                    .bccAddresses(msg.getRecipients(BCC))
                    .replyToAddress(msg.getReplyTo())
                    .receivedDate(LocalDateTime.now())
                    .seen(flags.contains(SEEN))
                    .recent(flags.contains(RECENT))
                    .draft(flags.contains(DRAFT))
                    .answered(flags.contains(ANSWERED))
                    .deleted(flags.contains(DELETED));

            return withAttachments ? builder.withAttachments(getAttachments(msg)).build()
                                   : builder.build();

        }
        catch (MessagingException mse)
        {
            throw new EmailException(mse.getMessage(), mse);
        }
    }

    public static EmailAttributes fromMessage(Message msg)
    {
        return fromMessage(msg, true);
    }

    public static EmailAttributesBuilder newAttributes()
    {
        return new EmailAttributesBuilder();
    }

    private void addArrayAddresses(Address[] toAddresses, List<String> addresses)
    {
        if (toAddresses != null)
        {
            Arrays.stream(toAddresses).map(Object::toString).forEach(addresses::add);
        }
    }
}

