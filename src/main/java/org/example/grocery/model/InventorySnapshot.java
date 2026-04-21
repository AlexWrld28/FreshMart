package org.example.grocery.model;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.Objects;

public record InventorySnapshot(String sku, String name, BigDecimal price, int remainingQuantity) {
    public InventorySnapshot {
        Objects.requireNonNull(sku, "sku");
        Objects.requireNonNull(name, "name");
        Objects.requireNonNull(price, "price");
        price = price.setScale(2, RoundingMode.HALF_UP);
        if (remainingQuantity < 0) {
            throw new IllegalArgumentException("remainingQuantity must not be negative");
        }
    }
}
