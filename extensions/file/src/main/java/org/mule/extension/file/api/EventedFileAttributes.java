package org.mule.extension.file.api;

import org.mule.runtime.module.extension.file.api.FileAttributes;

public interface EventedFileAttributes extends FileAttributes
{
    FileEventType getEventType();
}
