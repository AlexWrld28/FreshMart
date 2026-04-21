package org.example.grocery.model;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.List;
import java.util.Objects;

public record StoreSnapshot(
        List<InventorySnapshot> inventory,
        List<LaneSnapshot> checkoutLanes,
        int completedCustomers,
        int rejectedCustomers,
        BigDecimal revenue,
        boolean running
) {
    public StoreSnapshot {
        Objects.requireNonNull(inventory, "inventory");
        Objects.requireNonNull(checkoutLanes, "checkoutLanes");
        Objects.requireNonNull(revenue, "revenue");
        inventory = List.copyOf(inventory);
        checkoutLanes = List.copyOf(checkoutLanes);
        revenue = revenue.setScale(2, RoundingMode.HALF_UP);
        if (completedCustomers < 0) {
            throw new IllegalArgumentException("completedCustomers must not be negative");
        }
        if (rejectedCustomers < 0) {
            throw new IllegalArgumentException("rejectedCustomers must not be negative");
        }
    }
}
