/*
 * $Id$
 * --------------------------------------------------------------------------------------
 * Copyright (c) MuleSoft, Inc.  All rights reserved.  http://www.mulesoft.com
 *
 * The software in this package is published under the terms of the CPAL v1.0
 * license, a copy of which has been included with this distribution in the
 * LICENSE.txt file.
 */

package org.mule.transport.jersey;

public class SimpleServiceRootServletTestCase extends AbstractServletTestCase
{
    public SimpleServiceRootServletTestCase()
    {
        super("/*");
    }

    public void testBasic() throws Exception
    {
        testBasic("http://localhost:63088/base");
    }

    @Override
    protected String getConfigResources()
    {
        return "simple-service-servlet-conf.xml";
    }
}
