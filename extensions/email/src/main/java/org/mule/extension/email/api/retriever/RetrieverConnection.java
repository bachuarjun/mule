/*
 * Copyright (c) MuleSoft, Inc.  All rights reserved.  http://www.mulesoft.com
 * The software in this package is published under the terms of the CPAL v1.0
 * license, a copy of which has been included with this distribution in the
 * LICENSE.txt file.
 */
package org.mule.extension.email.api.retriever;

import static java.lang.String.format;
import static org.mule.runtime.api.connection.ConnectionExceptionCode.UNKNOWN;
import static org.mule.runtime.api.connection.ConnectionValidationResult.failure;
import static org.mule.runtime.api.connection.ConnectionValidationResult.success;
import org.mule.extension.email.api.EmailConnection;
import org.mule.extension.email.internal.EmailProperties;
import org.mule.extension.email.internal.exception.EmailConnectionException;
import org.mule.runtime.api.connection.ConnectionValidationResult;

import java.util.Map;
import java.util.Properties;

import javax.mail.Authenticator;
import javax.mail.Folder;
import javax.mail.MessagingException;
import javax.mail.Session;
import javax.mail.Store;

public class RetrieverConnection implements EmailConnection
{
    private final Session session;
    private final Store store;
    private final String folder;

    public RetrieverConnection(String protocol,
                               String username,
                               String password,
                               String host,
                               String port,
                               Map<String, String> properties,
                               Authenticator authenticator,
                               String folder)
    {
        Properties sessionProperties = EmailProperties.get(protocol, host, port, properties);
        this.session = Session.getInstance(sessionProperties, authenticator);
        this.folder = folder;
        try
        {
            this.store = session.getStore(protocol);
            this.store.connect(username, password);
        }
        catch (MessagingException e)
        {
            throw new EmailConnectionException("Error while acquiring connection with the " + protocol + "store", e);
        }
    }

    @Override
    public void disconnect()
    {
        try
        {
            store.close();
        }
        catch (MessagingException e)
        {
            throw new EmailConnectionException("Error while disconnecting", e);
        }
    }

    @Override
    public Session getSession()
    {
        return session;
    }

    @Override
    public ConnectionValidationResult validate()
    {
        String errorMessage = "";
        return session != null && store.isConnected() ? success() : failure(errorMessage, UNKNOWN, new EmailConnectionException(errorMessage));
    }

    public Store getStore()
    {
        return store;
    }

    public Folder getOpenFolder(int mode)
    {
        try
        {
            Folder folder = store.getFolder(this.folder);
            folder.open(mode);
            return folder;
        }
        catch (MessagingException e)
        {
            throw new EmailConnectionException(format("Error while opening folder [%s]", folder), e);
        }
    }

}
