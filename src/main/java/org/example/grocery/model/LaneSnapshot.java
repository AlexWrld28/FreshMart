package org.example.grocery.model;

public record LaneSnapshot(int laneId, int queuedCustomers) {
    public LaneSnapshot {
        if (laneId <= 0) {
            throw new IllegalArgumentException("laneId must be greater than zero");
        }
        if (queuedCustomers < 0) {
            throw new IllegalArgumentException("queuedCustomers must not be negative");
        }
    }
}
