package org.example.grocery.service;

import org.example.grocery.model.CartItem;
import org.example.grocery.model.InventorySnapshot;
import org.example.grocery.model.Product;
import org.example.grocery.model.StockItem;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Comparator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.locks.ReentrantLock;

public final class InventoryManager {
    private final Map<String, InventoryRecord> inventory = new ConcurrentHashMap<>();

    public InventoryManager(Collection<StockItem> initialStock) {
        Objects.requireNonNull(initialStock, "initialStock");
        for (StockItem stockItem : initialStock) {
            String sku = stockItem.product().sku();
            InventoryRecord previous = inventory.putIfAbsent(sku, new InventoryRecord(stockItem.product(), stockItem.quantity()));
            if (previous != null) {
                throw new IllegalArgumentException("Duplicate SKU in initial stock: " + sku);
            }
        }
    }

    public Optional<Reservation> tryReserve(List<CartItem> requestedItems) {
        Objects.requireNonNull(requestedItems, "requestedItems");
        Map<String, Integer> aggregatedRequest = aggregateRequest(requestedItems);
        if (aggregatedRequest.isEmpty()) {
            return Optional.empty();
        }

        List<InventoryRecord> lockedRecords = lockRecords(aggregatedRequest.keySet())
                .orElse(List.of());
        if (lockedRecords.isEmpty()) {
            return Optional.empty();
        }

        try {
            for (InventoryRecord record : lockedRecords) {
                int requestedQuantity = aggregatedRequest.get(record.product.sku());
                if (record.quantity < requestedQuantity) {
                    return Optional.empty();
                }
            }

            Map<String, Integer> reservedItems = new LinkedHashMap<>();
            long totalInCents = 0L;
            int totalItems = 0;
            for (InventoryRecord record : lockedRecords) {
                int requestedQuantity = aggregatedRequest.get(record.product.sku());
                record.quantity -= requestedQuantity;
                reservedItems.put(record.product.sku(), requestedQuantity);
                totalInCents += record.product.priceInCents() * requestedQuantity;
                totalItems += requestedQuantity;
            }

            return Optional.of(new Reservation(reservedItems, totalItems, centsToBigDecimal(totalInCents)));
        } finally {
            unlockRecords(lockedRecords);
        }
    }

    public void release(Reservation reservation) {
        Objects.requireNonNull(reservation, "reservation");
        List<InventoryRecord> lockedRecords = lockRecords(reservation.reservedItems().keySet())
                .orElseThrow(() -> new IllegalStateException("Reserved SKU disappeared from inventory"));
        try {
            for (InventoryRecord record : lockedRecords) {
                record.quantity += reservation.reservedItems().get(record.product.sku());
            }
        } finally {
            unlockRecords(lockedRecords);
        }
    }

    public void restock(String sku, int quantity) {
        if (quantity <= 0) {
            throw new IllegalArgumentException("quantity must be greater than zero");
        }

        InventoryRecord record = inventory.get(normalizeSku(sku));
        if (record == null) {
            throw new IllegalArgumentException("Unknown SKU: " + sku);
        }

        record.lock.lock();
        try {
            record.quantity += quantity;
        } finally {
            record.lock.unlock();
        }
    }

    public int remainingQuantity(String sku) {
        InventoryRecord record = inventory.get(normalizeSku(sku));
        if (record == null) {
            throw new IllegalArgumentException("Unknown SKU: " + sku);
        }

        record.lock.lock();
        try {
            return record.quantity;
        } finally {
            record.lock.unlock();
        }
    }

    public List<InventorySnapshot> snapshot() {
        List<InventoryRecord> orderedRecords = inventory.values().stream()
                .sorted(Comparator.comparing(record -> record.product.sku()))
                .toList();
        orderedRecords.forEach(record -> record.lock.lock());
        try {
            List<InventorySnapshot> snapshots = new ArrayList<>(orderedRecords.size());
            for (InventoryRecord record : orderedRecords) {
                snapshots.add(new InventorySnapshot(
                        record.product.sku(),
                        record.product.name(),
                        record.product.price(),
                        record.quantity
                ));
            }
            return List.copyOf(snapshots);
        } finally {
            unlockRecords(orderedRecords);
        }
    }

    private Optional<List<InventoryRecord>> lockRecords(Collection<String> skus) {
        List<InventoryRecord> orderedRecords = skus.stream()
                .distinct()
                .sorted()
                .map(inventory::get)
                .toList();
        if (orderedRecords.contains(null)) {
            return Optional.empty();
        }

        orderedRecords.forEach(record -> record.lock.lock());
        return Optional.of(orderedRecords);
    }

    private void unlockRecords(List<InventoryRecord> records) {
        for (int index = records.size() - 1; index >= 0; index--) {
            records.get(index).lock.unlock();
        }
    }

    private Map<String, Integer> aggregateRequest(List<CartItem> requestedItems) {
        Map<String, Integer> aggregated = new LinkedHashMap<>();
        for (CartItem item : requestedItems) {
            if (!inventory.containsKey(item.sku())) {
                return Map.of();
            }
            aggregated.merge(item.sku(), item.quantity(), Integer::sum);
        }
        return aggregated;
    }

    private String normalizeSku(String sku) {
        Objects.requireNonNull(sku, "sku");
        String normalized = sku.trim();
        if (normalized.isEmpty()) {
            throw new IllegalArgumentException("sku must not be blank");
        }
        return normalized;
    }

    private BigDecimal centsToBigDecimal(long cents) {
        return BigDecimal.valueOf(cents, 2).setScale(2, RoundingMode.HALF_UP);
    }

    public record Reservation(Map<String, Integer> reservedItems, int totalItems, BigDecimal total) {
        public Reservation {
            Objects.requireNonNull(reservedItems, "reservedItems");
            Objects.requireNonNull(total, "total");
            reservedItems = Map.copyOf(reservedItems);
            total = total.setScale(2, RoundingMode.HALF_UP);
            if (totalItems <= 0) {
                throw new IllegalArgumentException("totalItems must be greater than zero");
            }
        }
    }

    private static final class InventoryRecord {
        private final Product product;
        private final ReentrantLock lock = new ReentrantLock(true);
        private int quantity;

        private InventoryRecord(Product product, int quantity) {
            this.product = product;
            this.quantity = quantity;
        }
    }
}
