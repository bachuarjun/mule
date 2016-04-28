/*
 * Copyright (c) MuleSoft, Inc.  All rights reserved.  http://www.mulesoft.com
 * The software in this package is published under the terms of the CPAL v1.0
 * license, a copy of which has been included with this distribution in the
 * LICENSE.txt file.
 */
package org.mule.extension.file.api;

import static org.mule.extension.file.api.FileEventType.DELETE;

import java.nio.file.Path;
import java.time.Instant;
import java.time.LocalDateTime;

public class DeletedFileAttributes extends ListenerFileAttributes
{

    public DeletedFileAttributes(Path path)
    {
        super(path, DELETE);
    }

    private IllegalStateException unsupported(String property)
    {
        throw new IllegalStateException(String.format("Cannot obtain %s property for path '%s' because it has been deleted", property, getPath()));
    }

    @Override
    public LocalDateTime getLastModifiedTime()
    {
        throw unsupported("lastModifiedTime");
    }

    @Override
    public LocalDateTime getLastAccessTime()
    {
        throw unsupported("lastAccessTime");
    }

    @Override
    public LocalDateTime getCreationTime()
    {
        throw unsupported("creationTime");
    }

    @Override
    public long getSize()
    {
        throw unsupported("size");
    }

    @Override
    public boolean isRegularFile()
    {
        throw unsupported("isRegularFile");
    }

    @Override
    public boolean isDirectory()
    {
        throw unsupported("isDirectory");
    }

    @Override
    public boolean isSymbolicLink()
    {
        throw unsupported("isSymbolicLink");
    }

    @Override
    protected LocalDateTime asDateTime(Instant instant)
    {
        throw unsupported("dateTime");
    }
}
