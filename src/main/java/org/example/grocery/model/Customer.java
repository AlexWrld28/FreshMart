package org.example.grocery.model;

import java.util.List;
import java.util.Objects;

public record Customer(String id, String name, List<CartItem> shoppingList) {
    public Customer {
        id = normalize("id", id);
        name = normalize("name", name);
        Objects.requireNonNull(shoppingList, "shoppingList must not be null");
        shoppingList = List.copyOf(shoppingList);
        if (shoppingList.isEmpty()) {
            throw new IllegalArgumentException("shoppingList must not be empty");
        }
    }

    public int totalItems() {
        return shoppingList.stream().mapToInt(CartItem::quantity).sum();
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
