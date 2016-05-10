/*
 * Copyright (c) MuleSoft, Inc.  All rights reserved.  http://www.mulesoft.com
 * The software in this package is published under the terms of the CPAL v1.0
 * license, a copy of which has been included with this distribution in the
 * LICENSE.txt file.
 */
package org.mule.extension.email.internal;

public class EmailFlags
{

    public EmailFlags(Boolean answered, Boolean deleted, Boolean draft, Boolean recent, Boolean seen)
    {
        this.answered = answered;
        this.deleted = deleted;
        this.draft = draft;
        this.recent = recent;
        this.seen = seen;
    }

    private Boolean answered;

    private Boolean deleted;

    private Boolean draft;

    private Boolean recent;

    private Boolean seen;

    public Boolean answered()
    {
        return answered;
    }

    public Boolean deleted()
    {
        return deleted;
    }

    public Boolean draft()
    {
        return draft;
    }

    public Boolean recent()
    {
        return recent;
    }

    public Boolean seen()
    {
        return seen;
    }

}
