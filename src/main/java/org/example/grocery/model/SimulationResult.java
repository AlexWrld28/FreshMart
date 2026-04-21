package org.example.grocery.model;

import java.util.List;
import java.util.Objects;

public record SimulationResult(List<Receipt> receipts, StoreSnapshot finalSnapshot) {
    public SimulationResult {
        Objects.requireNonNull(receipts, "receipts");
        Objects.requireNonNull(finalSnapshot, "finalSnapshot");
        receipts = List.copyOf(receipts);
    }
}
