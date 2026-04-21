package org.example.grocery.model;

import java.time.Instant;
import java.util.Objects;

public record StoreEvent(Instant timestamp, StoreEventType type, String message, StoreSnapshot snapshot) {
    public StoreEvent {
        Objects.requireNonNull(timestamp, "timestamp");
        Objects.requireNonNull(type, "type");
        Objects.requireNonNull(message, "message");
        Objects.requireNonNull(snapshot, "snapshot");
    }
}
