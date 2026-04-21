package org.example.grocery;

import org.example.grocery.model.CartItem;
import org.example.grocery.model.Customer;
import org.example.grocery.model.Product;
import org.example.grocery.model.SimulationConfig;
import org.example.grocery.model.SimulationResult;
import org.example.grocery.model.StockItem;
import org.example.grocery.service.GroceryStoreSimulation;
import org.example.grocery.service.InventoryManager;

import java.math.BigDecimal;
import java.util.List;
import java.util.Objects;
import java.util.Optional;
import java.util.stream.IntStream;

public final class GroceryStoreSimulationSelfTest {
    private GroceryStoreSimulationSelfTest() {
    }

    public static void main(String[] args) throws InterruptedException {
        shouldRejectAtomicReservationWhenAnySkuIsMissing();
        shouldNeverOversellUnderConcurrentCustomers();
        System.out.println("All grocery store self-checks passed.");
    }

    private static void shouldRejectAtomicReservationWhenAnySkuIsMissing() {
        InventoryManager inventoryManager = new InventoryManager(List.of(
                new StockItem(new Product("APPLE", "Apple", new BigDecimal("0.99")), 1),
                new StockItem(new Product("BREAD", "Bread", new BigDecimal("2.99")), 0)
        ));

        Optional<InventoryManager.Reservation> reservation = inventoryManager.tryReserve(List.of(
                new CartItem("APPLE", 1),
                new CartItem("BREAD", 1)
        ));

        assertTrue(reservation.isEmpty(), "Reservation should fail when any requested SKU is unavailable");
        assertEquals(1, inventoryManager.remainingQuantity("APPLE"), "Apple stock should remain unchanged after rollback");
        assertEquals(0, inventoryManager.remainingQuantity("BREAD"), "Bread stock should remain unchanged");
    }

    private static void shouldNeverOversellUnderConcurrentCustomers() throws InterruptedException {
        GroceryStoreSimulation simulation = new GroceryStoreSimulation(
                new SimulationConfig(12, 3, 0, 5, 1),
                List.of(new StockItem(new Product("MILK", "Milk", new BigDecimal("3.49")), 50))
        );

        List<Customer> customers = IntStream.range(0, 100)
                .mapToObj(index -> new Customer(
                        "C-" + index,
                        "Customer-" + index,
                        List.of(new CartItem("MILK", 1))
                ))
                .toList();

        SimulationResult result = simulation.runSimulation(customers);

        assertEquals(50, result.finalSnapshot().completedCustomers(), "Only 50 customers should check out successfully");
        assertEquals(50, result.finalSnapshot().rejectedCustomers(), "The remaining customers should be rejected");
        assertEquals(0, simulation.remainingQuantity("MILK"), "Milk inventory should be exhausted exactly");
        assertEquals(50, result.receipts().size(), "There should be one receipt per successful customer");
    }

    private static void assertTrue(boolean condition, String message) {
        if (!condition) {
            throw new AssertionError(message);
        }
    }

    private static void assertEquals(Object expected, Object actual, String message) {
        if (!Objects.equals(expected, actual)) {
            throw new AssertionError(message + ". Expected " + expected + " but got " + actual);
        }
    }
}
