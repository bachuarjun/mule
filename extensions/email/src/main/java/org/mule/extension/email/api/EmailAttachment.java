/*
 * Copyright (c) MuleSoft, Inc.  All rights reserved.  http://www.mulesoft.com
 * The software in this package is published under the terms of the CPAL v1.0
 * license, a copy of which has been included with this distribution in the
 * LICENSE.txt file.
 */
package org.mule.extension.email.api;

import static org.mule.runtime.extension.api.introspection.parameter.ExpressionSupport.REQUIRED;
import org.mule.runtime.extension.api.annotation.Expression;
import org.mule.runtime.extension.api.annotation.Parameter;
import org.mule.runtime.extension.api.annotation.param.Optional;

public class EmailAttachment
{

    @Parameter
    private String id;

    @Parameter
    @Expression(REQUIRED)
    private Object data;

    @Parameter
    @Optional
    private String contentType;

    public EmailAttachment()
    {

    }

    public String getId()
    {
        return id;
    }

    public Object getData()
    {
        return data;
    }

    public String getContentType()
    {
        return contentType;
    }

}
