/*
 * Copyright (c) MuleSoft, Inc.  All rights reserved.  http://www.mulesoft.com
 * The software in this package is published under the terms of the CPAL v1.0
 * license, a copy of which has been included with this distribution in the
 * LICENSE.txt file.
 */
package org.mule.extension.email.api;

import org.mule.runtime.extension.api.annotation.Parameter;
import org.mule.runtime.extension.api.annotation.param.Optional;

import java.util.Map;

public abstract class AbstractEmailProvider
{
    @Parameter
    protected String host;

    @Parameter
    @Optional
    protected Map<String, String> properties;

    public String getHost()
    {
        return host;
    }

    public Map<String, String> getProperties()
    {
        return properties;
    }

    public abstract String getUser();

    public abstract String getPassword();

    public abstract String getPort();
}
