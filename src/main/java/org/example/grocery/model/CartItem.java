package org.example.grocery.model;

import java.util.Objects;

public record CartItem(String sku, int quantity) {
    public CartItem {
        sku = normalize("sku", sku);
        if (quantity <= 0) {
            throw new IllegalArgumentException("quantity must be greater than zero");
        }
    }

    private static String normalize(String field, String value) {
        Objects.requireNonNull(value, field + " must not be null");
        String normalized = value.trim();
        if (normalized.isEmpty()) {
            throw new IllegalArgumentException(field + " must not be blank");
        }
        return normalized;
    }
}
