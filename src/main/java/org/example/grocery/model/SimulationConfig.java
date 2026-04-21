package org.example.grocery.model;

public record SimulationConfig(
        int customerThreads,
        int laneCount,
        int minShoppingMillis,
        int maxShoppingMillis,
        int checkoutMillisPerItem
) {
    public SimulationConfig {
        if (customerThreads <= 0) {
            throw new IllegalArgumentException("customerThreads must be greater than zero");
        }
        if (laneCount <= 0) {
            throw new IllegalArgumentException("laneCount must be greater than zero");
        }
        if (minShoppingMillis < 0) {
            throw new IllegalArgumentException("minShoppingMillis must not be negative");
        }
        if (maxShoppingMillis < minShoppingMillis) {
            throw new IllegalArgumentException("maxShoppingMillis must be greater than or equal to minShoppingMillis");
        }
        if (checkoutMillisPerItem < 0) {
            throw new IllegalArgumentException("checkoutMillisPerItem must not be negative");
        }
    }
}
