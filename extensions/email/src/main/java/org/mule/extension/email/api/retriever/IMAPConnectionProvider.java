/*
 * Copyright (c) MuleSoft, Inc.  All rights reserved.  http://www.mulesoft.com
 * The software in this package is published under the terms of the CPAL v1.0
 * license, a copy of which has been included with this distribution in the
 * LICENSE.txt file.
 */
package org.mule.extension.email.api.retriever;

import static org.mule.extension.email.internal.util.EmailConstants.DEFAULT_FOLDER;
import static org.mule.extension.email.internal.util.EmailConstants.PORT_IMAP;
import static org.mule.extension.email.internal.util.EmailConstants.PROTOCOL_IMAP;
import org.mule.runtime.api.connection.ConnectionException;
import org.mule.runtime.api.connection.ConnectionHandlingStrategy;
import org.mule.runtime.api.connection.ConnectionHandlingStrategyFactory;
import org.mule.runtime.api.connection.ConnectionProvider;
import org.mule.runtime.api.connection.ConnectionValidationResult;
import org.mule.runtime.extension.api.annotation.Alias;
import org.mule.runtime.extension.api.annotation.Parameter;
import org.mule.runtime.extension.api.annotation.param.Optional;

@Alias("imap")
public class IMAPConnectionProvider extends AbstractRetrieverProvider implements ConnectionProvider<RetrieverConfiguration, RetrieverConnection>
{

    @Parameter
    @Optional(defaultValue = PORT_IMAP)
    private String port;

    @Parameter
    @Optional(defaultValue = DEFAULT_FOLDER)
    private String folder;

    @Override
    public RetrieverConnection connect(RetrieverConfiguration config) throws ConnectionException
    {
        return new RetrieverConnection(PROTOCOL_IMAP, getUser(), getPassword(), getHost(), getPort(), getProperties(), null, getFolder());
    }

    @Override
    public void disconnect(RetrieverConnection connection)
    {
        connection.disconnect();
    }

    @Override
    public ConnectionValidationResult validate(RetrieverConnection connection)
    {
        return ConnectionValidationResult.success();
    }

    @Override
    public ConnectionHandlingStrategy<RetrieverConnection> getHandlingStrategy(ConnectionHandlingStrategyFactory<RetrieverConfiguration, RetrieverConnection> connectionHandlingStrategyFactory)
    {
        return connectionHandlingStrategyFactory.supportsPooling();
    }

    @Override
    public String getFolder()
    {
        return folder;
    }

    @Override
    public String getPort()
    {
        return port;
    }
}
