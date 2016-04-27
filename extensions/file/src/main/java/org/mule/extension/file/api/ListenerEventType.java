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

    ListenerEventType(Kind kind)
    {
        this.kind = kind;
    }

    public Kind asEventKind()
    {
        return kind;
    }
}
