package org.example.grocery.model;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.Map;
import java.util.Objects;

public record Receipt(
        String customerId,
        String customerName,
        int laneId,
        int totalItems,
        BigDecimal total,
        Map<String, Integer> purchasedItems
) {
    public Receipt {
        Objects.requireNonNull(customerId, "customerId");
        Objects.requireNonNull(customerName, "customerName");
        Objects.requireNonNull(total, "total");
        Objects.requireNonNull(purchasedItems, "purchasedItems");
        total = total.setScale(2, RoundingMode.HALF_UP);
        purchasedItems = Map.copyOf(purchasedItems);
        if (laneId <= 0) {
            throw new IllegalArgumentException("laneId must be greater than zero");
        }
        if (totalItems <= 0) {
            throw new IllegalArgumentException("totalItems must be greater than zero");
        }
        if (total.signum() < 0) {
            throw new IllegalArgumentException("total must not be negative");
        }
    }
}
