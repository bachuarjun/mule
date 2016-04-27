/*
 * Copyright (c) MuleSoft, Inc.  All rights reserved.  http://www.mulesoft.com
 * The software in this package is published under the terms of the CPAL v1.0
 * license, a copy of which has been included with this distribution in the
 * LICENSE.txt file.
 */
package org.mule.extension.file.api;

import static java.nio.file.StandardWatchEventKinds.ENTRY_CREATE;
import static java.nio.file.StandardWatchEventKinds.ENTRY_DELETE;
import static java.nio.file.StandardWatchEventKinds.ENTRY_MODIFY;

import java.nio.file.WatchEvent.Kind;

public enum ListenerEventType
{

    CREATE(ENTRY_CREATE),
    UPDATE(ENTRY_MODIFY),
    DELETE(ENTRY_DELETE);

    private final Kind kind;

    public static ListenerEventType of(Kind kind) {
        if (kind == ENTRY_CREATE) {
            return CREATE;
        } else if (kind == ENTRY_MODIFY) {
            return UPDATE;
        } else if (kind == ENTRY_DELETE) {
            return DELETE;
        }

        throw new IllegalArgumentException("Invalid Event Kind: " + kind.name());
    }

    ListenerEventType(Kind kind)
    {
        this.kind = kind;
    }

    public Kind asEventKind()
    {
        return kind;
    }
}
