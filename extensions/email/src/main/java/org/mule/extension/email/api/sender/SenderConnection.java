/*
 * Copyright (c) MuleSoft, Inc.  All rights reserved.  http://www.mulesoft.com
 * The software in this package is published under the terms of the CPAL v1.0
 * license, a copy of which has been included with this distribution in the
 * LICENSE.txt file.
 */
package org.mule.extension.email.api.sender;

import static org.mule.runtime.api.connection.ConnectionExceptionCode.UNKNOWN;
import static org.mule.runtime.api.connection.ConnectionValidationResult.failure;
import static org.mule.runtime.api.connection.ConnectionValidationResult.success;
import org.mule.extension.email.api.EmailConnection;
import org.mule.extension.email.internal.EmailProperties;
import org.mule.extension.email.internal.PasswordAuthenticator;
import org.mule.extension.email.internal.exception.EmailConnectionException;
import org.mule.runtime.api.connection.ConnectionValidationResult;

import java.util.Map;
import java.util.Properties;

import javax.mail.Session;

public class SenderConnection implements EmailConnection
{
    private final Session session;

    public SenderConnection(String protocol,
                            String user,
                            String password,
                            String host,
                            String port,
                            Map<String, String> properties)
    {
        Properties senderProps = EmailProperties.get(protocol, host, port, properties);

        PasswordAuthenticator authenticator = null;
        if (user != null && password != null)
        {
             authenticator = new PasswordAuthenticator(user, password);
        }

        session = Session.getInstance(senderProps, authenticator);
    }

    public void disconnect()
    {
        // No implementation
    }

    public Session getSession()
    {
        return session;
    }

    @Override
    public ConnectionValidationResult validate()
    {
        return session != null ? success() : failure("", UNKNOWN, new EmailConnectionException(""));
    }

}
