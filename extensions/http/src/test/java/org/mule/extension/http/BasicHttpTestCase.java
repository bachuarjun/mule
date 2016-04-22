/*
 * Copyright (c) MuleSoft, Inc.  All rights reserved.  http://www.mulesoft.com
 * The software in this package is published under the terms of the CPAL v1.0
 * license, a copy of which has been included with this distribution in the
 * LICENSE.txt file.
 */
package org.mule.extension.http;

import static org.hamcrest.Matchers.is;
import static org.junit.Assert.assertThat;
import org.mule.extension.http.api.HttpConnector;
import org.mule.functional.junit4.ExtensionFunctionalTestCase;
import org.mule.runtime.core.api.MuleEvent;
import org.mule.runtime.core.util.IOUtils;
import org.mule.tck.junit4.rule.DynamicPort;

import java.io.IOException;
import java.io.InputStream;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.eclipse.jetty.server.Request;
import org.eclipse.jetty.server.Server;
import org.eclipse.jetty.server.handler.AbstractHandler;
import org.junit.After;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;

public class BasicHttpTestCase extends ExtensionFunctionalTestCase
{
    @Rule
    public DynamicPort clientPort = new DynamicPort("clientPort");
    @Rule
    public DynamicPort serverPort = new DynamicPort("serverPort");

    protected Server server;

    protected String method;
    protected String uri;

    @Override
    protected Class<?>[] getAnnotatedExtensionClasses()
    {
        return new Class<?>[] {HttpConnector.class};
    }

    @Before
    public void startServer() throws Exception
    {
        server = createServer();
        server.setHandler(createHandler(server));
        server.start();
    }

    @After
    public void stopServer() throws Exception
    {
        server.stop();
    }

    protected Server createServer()
    {
        Server server = new Server(clientPort.getNumber());
        return server;
    }

    protected AbstractHandler createHandler(Server server)
    {
        return new TestHandler();
    }

    private class TestHandler extends AbstractHandler
    {

        @Override
        public void handle(String target, Request baseRequest, HttpServletRequest request, HttpServletResponse response) throws IOException, ServletException
        {

            handleRequest(baseRequest, request, response);

            baseRequest.setHandled(true);
        }
    }

    protected void handleRequest(Request baseRequest, HttpServletRequest request, HttpServletResponse response) throws IOException
    {
        extractBaseRequestParts(baseRequest);
        writeResponse(response);
    }

    protected void extractBaseRequestParts(Request baseRequest) throws IOException
    {
        method = baseRequest.getMethod();
        uri = baseRequest.getUri().getCompletePath();
    }

    protected void writeResponse(HttpServletResponse response) throws IOException
    {
        response.setContentType("text/html");
        response.setStatus(HttpServletResponse.SC_OK);
        response.getWriter().print("WOW");
    }

    @Override
    protected String getConfigFile()
    {
        return "basic-http-config.xml";
    }

    @Test
    public void sendsRequest() throws Exception
    {
        MuleEvent response = flowRunner("client").withPayload("PEPE").run();
        assertThat(IOUtils.toString((InputStream) response.getMessage().getPayload()), is("WOW"));
    }

    //@Test
    //public void receivesRequest() throws Exception
    //{
    //    CloseableHttpClient httpClient = HttpClients.createDefault();
    //    HttpGet getRequest = new HttpGet(String.format("http://localhost:%s/test", serverPort.getValue()));
    //    try
    //    {
    //        CloseableHttpResponse response = httpClient.execute(getRequest);
    //        try
    //        {
    //            assertThat(IOUtils.toString(response.getEntity().getContent()), is("HEY"));
    //        }
    //        finally
    //        {
    //            response.close();
    //        }
    //    }
    //    finally
    //    {
    //        httpClient.close();
    //    }
    //}
}
