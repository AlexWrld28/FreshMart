package org.example.grocery.model;

import java.util.Objects;

public record StockItem(Product product, int quantity) {
    public StockItem {
        Objects.requireNonNull(product, "product");
        if (quantity < 0) {
            throw new IllegalArgumentException("quantity must not be negative");
        }
    }
}
