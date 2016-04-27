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
