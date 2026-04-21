package org.example.grocery.model;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.Objects;

public record Product(String sku, String name, BigDecimal price) {
    public Product {
        sku = normalize("sku", sku);
        name = normalize("name", name);
        Objects.requireNonNull(price, "price");
        price = price.setScale(2, RoundingMode.HALF_UP);
        if (price.signum() < 0) {
            throw new IllegalArgumentException("price must not be negative");
        }
    }

    public long priceInCents() {
        return price.movePointRight(2).longValueExact();
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
