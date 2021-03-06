/*
 * Copyright (c) MuleSoft, Inc.  All rights reserved.  http://www.mulesoft.com
 * The software in this package is published under the terms of the CPAL v1.0
 * license, a copy of which has been included with this distribution in the
 * LICENSE.txt file.
 */
package org.mule.tck.testmodels.mule;

import org.mule.api.config.MuleConfiguration;
import org.mule.transaction.lookup.GenericTransactionManagerLookupFactory;

import java.lang.reflect.InvocationHandler;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.lang.reflect.Proxy;

import javax.transaction.TransactionManager;

/**
 * <code>TestTransactionManagerFactory</code> TODO
 */
public class TestTransactionManagerFactory extends GenericTransactionManagerLookupFactory
{
    @Override
    public TransactionManager create(MuleConfiguration config) throws Exception
    {
        return (TransactionManager) Proxy.newProxyInstance(getClass().getClassLoader(),
                                                           new Class[] {TransactionManager.class},
                                                           new InternalInvocationHandler());
    }

    @Override
    public void initialise()
    {
        // shortcut super's implementation
    }

    public class InternalInvocationHandler implements InvocationHandler
    {
        public TestTransactionManagerFactory getParent()
        {
            return TestTransactionManagerFactory.this;
        }

        @Override
        public Object invoke (Object proxy, Method method, Object[] args) throws Throwable
        {
            if (Object.class.equals(method.getDeclaringClass()))
            {
                try
                {
                    return method.invoke(this, args);
                }
                catch (InvocationTargetException e)
                {
                    throw e.getCause();
                }
            }
            else
            {
                return null;
            }
        }

    }
}
