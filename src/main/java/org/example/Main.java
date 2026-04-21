package org.example;

import org.example.grocery.model.CartItem;
import org.example.grocery.model.Customer;
import org.example.grocery.model.InventorySnapshot;
import org.example.grocery.model.Product;
import org.example.grocery.model.Receipt;
import org.example.grocery.model.SimulationConfig;
import org.example.grocery.model.SimulationResult;
import org.example.grocery.model.StockItem;
import org.example.grocery.model.StoreEventType;
import org.example.grocery.service.GroceryStoreSimulation;

import java.math.BigDecimal;
import java.text.NumberFormat;
import java.util.List;
import java.util.Locale;

public final class Main {
    private Main() {
    }

    public static void main(String[] args) throws InterruptedException {
        GroceryStoreSimulation simulation = new GroceryStoreSimulation(
                new SimulationConfig(5, 3, 100, 350, 70),
                List.of(
                        new StockItem(new Product("MILK", "Milk", new BigDecimal("3.49")), 6),
                        new StockItem(new Product("BREAD", "Bread", new BigDecimal("2.99")), 5),
                        new StockItem(new Product("EGGS", "Eggs", new BigDecimal("4.79")), 4),
                        new StockItem(new Product("APPLE", "Apple", new BigDecimal("0.89")), 15),
                        new StockItem(new Product("RICE", "Rice", new BigDecimal("7.29")), 3)
                )
        );

        simulation.addListener(event -> {
            if (event.type() == StoreEventType.SIMULATION_STARTED
                    || event.type() == StoreEventType.OUT_OF_STOCK
                    || event.type() == StoreEventType.CHECKOUT_COMPLETED
                    || event.type() == StoreEventType.SIMULATION_FINISHED) {
                System.out.println(event.timestamp() + " [" + event.type() + "] " + event.message());
            }
        });

        List<Customer> customers = List.of(
                new Customer("C-100", "Avery", List.of(new CartItem("MILK", 1), new CartItem("BREAD", 1))),
                new Customer("C-101", "Blair", List.of(new CartItem("EGGS", 1), new CartItem("APPLE", 4))),
                new Customer("C-102", "Casey", List.of(new CartItem("RICE", 1), new CartItem("MILK", 2))),
                new Customer("C-103", "Devon", List.of(new CartItem("APPLE", 6), new CartItem("BREAD", 1))),
                new Customer("C-104", "Emerson", List.of(new CartItem("RICE", 2), new CartItem("EGGS", 1))),
                new Customer("C-105", "Finley", List.of(new CartItem("MILK", 3), new CartItem("BREAD", 2))),
                new Customer("C-106", "Gray", List.of(new CartItem("APPLE", 5), new CartItem("EGGS", 1))),
                new Customer("C-107", "Harper", List.of(new CartItem("RICE", 1), new CartItem("APPLE", 2)))
        );

        SimulationResult result = simulation.runSimulation(customers);
        NumberFormat currency = NumberFormat.getCurrencyInstance(Locale.US);

        System.out.println();
        System.out.println("Receipts");
        for (Receipt receipt : result.receipts()) {
            System.out.printf(
                    Locale.US,
                    "Customer %s used lane %d for %d items: %s%n",
                    receipt.customerName(),
                    receipt.laneId(),
                    receipt.totalItems(),
                    currency.format(receipt.total())
            );
        }

        System.out.println();
        System.out.println("Remaining inventory");
        for (InventorySnapshot item : result.finalSnapshot().inventory()) {
            System.out.printf(
                    Locale.US,
                    "%s (%s): %d left%n",
                    item.name(),
                    item.sku(),
                    item.remainingQuantity()
            );
        }

        System.out.println();
        System.out.println("Revenue: " + currency.format(result.finalSnapshot().revenue()));
        System.out.println("Completed customers: " + result.finalSnapshot().completedCustomers());
        System.out.println("Rejected customers: " + result.finalSnapshot().rejectedCustomers());
    }
}
