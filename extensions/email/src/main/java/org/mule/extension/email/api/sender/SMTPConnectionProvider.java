/*
 * Copyright (c) MuleSoft, Inc.  All rights reserved.  http://www.mulesoft.com
 * The software in this package is published under the terms of the CPAL v1.0
 * license, a copy of which has been included with this distribution in the
 * LICENSE.txt file.
 */
package org.mule.extension.email.api.sender;

import static org.mule.extension.email.internal.util.EmailConstants.PORT_SMTP;
import static org.mule.extension.email.internal.util.EmailConstants.PROTOCOL_SMTP;
import org.mule.extension.email.api.AbstractEmailProvider;
import org.mule.runtime.api.connection.ConnectionException;
import org.mule.runtime.api.connection.ConnectionHandlingStrategy;
import org.mule.runtime.api.connection.ConnectionHandlingStrategyFactory;
import org.mule.runtime.api.connection.ConnectionProvider;
import org.mule.runtime.api.connection.ConnectionValidationResult;
import org.mule.runtime.extension.api.annotation.Alias;
import org.mule.runtime.extension.api.annotation.Parameter;
import org.mule.runtime.extension.api.annotation.param.Optional;

@Alias("smtp")
public class SMTPConnectionProvider extends AbstractEmailProvider implements ConnectionProvider<SenderConfiguration, SenderConnection>
{

    @Parameter
    @Optional(defaultValue = PORT_SMTP)
    protected String port;

    @Parameter
    @Optional
    protected String user;

    @Parameter
    @Optional
    protected String password;

    @Override
    public SenderConnection connect(SenderConfiguration config) throws ConnectionException
    {
        return new SenderConnection(PROTOCOL_SMTP, getUser(), getPassword(), getHost(), getPort(), getProperties());
    }

    @Override
    public void disconnect(SenderConnection connection)
    {
        connection.disconnect();
    }

    @Override
    public ConnectionValidationResult validate(SenderConnection connection)
    {
        return connection.validate();
    }

    @Override
    public ConnectionHandlingStrategy<SenderConnection> getHandlingStrategy(ConnectionHandlingStrategyFactory<SenderConfiguration, SenderConnection> handlingStrategyFactory)
    {
        return handlingStrategyFactory.supportsPooling();
    }

    @Override
    public String getUser()
    {
        return user;
    }

    @Override
    public String getPassword()
    {
        return password;
    }

    @Override
    public String getPort()
    {
        return port;
    }
}
