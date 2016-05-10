/*
 * Copyright (c) MuleSoft, Inc.  All rights reserved.  http://www.mulesoft.com
 * The software in this package is published under the terms of the CPAL v1.0
 * license, a copy of which has been included with this distribution in the
 * LICENSE.txt file.
 */
package org.mule.extension.email.api.retriever;

import org.mule.extension.email.api.AbstractEmailProvider;
import org.mule.runtime.extension.api.annotation.Parameter;

public abstract class AbstractRetrieverProvider extends AbstractEmailProvider
{

    @Parameter
    protected String user;

    @Parameter
    protected String password;


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

    public abstract String getFolder();
}
