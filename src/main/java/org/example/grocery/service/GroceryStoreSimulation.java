package org.example.grocery.service;

import org.example.grocery.model.Customer;
import org.example.grocery.model.LaneSnapshot;
import org.example.grocery.model.Receipt;
import org.example.grocery.model.SimulationConfig;
import org.example.grocery.model.SimulationResult;
import org.example.grocery.model.StockItem;
import org.example.grocery.model.StoreEvent;
import org.example.grocery.model.StoreEventType;
import org.example.grocery.model.StoreSnapshot;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.Instant;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Queue;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.CompletionException;
import java.util.concurrent.ConcurrentLinkedQueue;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.ThreadLocalRandom;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.atomic.AtomicLong;
import java.util.stream.IntStream;

public final class GroceryStoreSimulation {
    private final SimulationConfig config;
    private final InventoryManager inventoryManager;
    private final CopyOnWriteArrayList<StoreListener> listeners = new CopyOnWriteArrayList<>();
    private final Queue<Receipt> receipts = new ConcurrentLinkedQueue<>();
    private final AtomicInteger completedCustomers = new AtomicInteger();
    private final AtomicInteger rejectedCustomers = new AtomicInteger();
    private final AtomicLong revenueInCents = new AtomicLong();
    private final AtomicBoolean running = new AtomicBoolean();

    private volatile List<CheckoutLane> checkoutLanes;

    public GroceryStoreSimulation(SimulationConfig config, Collection<StockItem> initialStock) {
        this.config = Objects.requireNonNull(config, "config");
        this.inventoryManager = new InventoryManager(initialStock);
        this.checkoutLanes = createCheckoutLanes();
    }

    public void addListener(StoreListener listener) {
        listeners.add(Objects.requireNonNull(listener, "listener"));
    }

    public void removeListener(StoreListener listener) {
        listeners.remove(listener);
    }

    public CompletableFuture<SimulationResult> runSimulationAsync(List<Customer> customers) {
        return CompletableFuture.supplyAsync(() -> {
            try {
                return runSimulation(customers);
            } catch (InterruptedException interruptedException) {
                Thread.currentThread().interrupt();
                throw new CompletionException(interruptedException);
            }
        });
    }

    public SimulationResult runSimulation(List<Customer> customers) throws InterruptedException {
        Objects.requireNonNull(customers, "customers");
        List<Customer> customerList = List.copyOf(customers);
        if (!running.compareAndSet(false, true)) {
            throw new IllegalStateException("A simulation is already running");
        }

        resetState();
        checkoutLanes = createCheckoutLanes();
        publishEvent(
                StoreEventType.SIMULATION_STARTED,
                "Simulation started for " + customerList.size() + " customers.",
                snapshot()
        );

        ExecutorService customerExecutor = Executors.newFixedThreadPool(config.customerThreads());
        ExecutorService laneExecutor = Executors.newFixedThreadPool(config.laneCount());
        List<Future<?>> laneFutures = startLaneWorkers(laneExecutor, checkoutLanes);

        try {
            List<Future<?>> customerFutures = new ArrayList<>(customerList.size());
            for (Customer customer : customerList) {
                customerFutures.add(customerExecutor.submit(() -> runCustomerTask(customer)));
            }

            customerExecutor.shutdown();
            waitFor(customerFutures);

            for (CheckoutLane lane : checkoutLanes) {
                lane.submit(CustomerOrder.poison());
            }

            laneExecutor.shutdown();
            waitFor(laneFutures);

            running.set(false);
            StoreSnapshot finalSnapshot = snapshot();
            publishEvent(
                    StoreEventType.SIMULATION_FINISHED,
                    "Simulation finished. Completed "
                            + finalSnapshot.completedCustomers()
                            + " customers and rejected "
                            + finalSnapshot.rejectedCustomers()
                            + ".",
                    finalSnapshot
            );
            return new SimulationResult(List.copyOf(receipts), finalSnapshot);
        } catch (ExecutionException executionException) {
            throw new IllegalStateException("A simulation worker failed", executionException.getCause());
        } finally {
            customerExecutor.shutdownNow();
            laneExecutor.shutdownNow();
            running.set(false);
        }
    }

    public StoreSnapshot snapshot() {
        List<LaneSnapshot> laneSnapshots = checkoutLanes.stream()
                .map(lane -> new LaneSnapshot(lane.laneId(), lane.queueSize()))
                .sorted(Comparator.comparingInt(LaneSnapshot::laneId))
                .toList();
        return new StoreSnapshot(
                inventoryManager.snapshot(),
                laneSnapshots,
                completedCustomers.get(),
                rejectedCustomers.get(),
                centsToBigDecimal(revenueInCents.get()),
                running.get()
        );
    }

    public void restock(String sku, int quantity) {
        inventoryManager.restock(sku, quantity);
    }

    public int remainingQuantity(String sku) {
        return inventoryManager.remainingQuantity(sku);
    }

    private void runCustomerTask(Customer customer) {
        try {
            processCustomer(customer);
        } catch (InterruptedException interruptedException) {
            Thread.currentThread().interrupt();
            throw new IllegalStateException("Customer worker interrupted", interruptedException);
        }
    }

    private void runLaneTask(CheckoutLane lane) {
        try {
            processLane(lane);
        } catch (InterruptedException interruptedException) {
            Thread.currentThread().interrupt();
            throw new IllegalStateException("Lane worker interrupted", interruptedException);
        }
    }

    private void processCustomer(Customer customer) throws InterruptedException {
        publishEvent(StoreEventType.CUSTOMER_ARRIVED, customer.name() + " entered the store.");
        simulateShoppingDelay();

        InventoryManager.Reservation reservation = inventoryManager.tryReserve(customer.shoppingList())
                .orElse(null);
        if (reservation == null) {
            rejectedCustomers.incrementAndGet();
            publishEvent(
                    StoreEventType.OUT_OF_STOCK,
                    customer.name() + " left because at least one requested item was unavailable."
            );
            return;
        }

        publishEvent(
                StoreEventType.BASKET_RESERVED,
                customer.name() + " reserved " + reservation.totalItems() + " items."
        );

        CheckoutLane lane = selectShortestLane();
        try {
            lane.submit(new CustomerOrder(customer, reservation.reservedItems(), reservation.totalItems(), reservation.total()));
        } catch (InterruptedException interruptedException) {
            inventoryManager.release(reservation);
            throw interruptedException;
        }

        publishEvent(
                StoreEventType.QUEUED_FOR_CHECKOUT,
                customer.name() + " joined checkout lane " + lane.laneId() + "."
        );
    }

    private void processLane(CheckoutLane lane) throws InterruptedException {
        while (true) {
            CustomerOrder order = lane.take();
            if (order.poisonPill()) {
                return;
            }

            publishEvent(
                    StoreEventType.CHECKOUT_STARTED,
                    order.customer().name() + " started checkout on lane " + lane.laneId() + "."
            );

            long checkoutTime = (long) order.totalItems() * config.checkoutMillisPerItem();
            if (checkoutTime > 0) {
                TimeUnit.MILLISECONDS.sleep(checkoutTime);
            }

            completedCustomers.incrementAndGet();
            long totalInCents = order.total().movePointRight(2).longValueExact();
            revenueInCents.addAndGet(totalInCents);
            receipts.add(new Receipt(
                    order.customer().id(),
                    order.customer().name(),
                    lane.laneId(),
                    order.totalItems(),
                    order.total(),
                    order.reservedItems()
            ));

            publishEvent(
                    StoreEventType.CHECKOUT_COMPLETED,
                    order.customer().name()
                            + " checked out on lane "
                            + lane.laneId()
                            + " for "
                            + order.total()
                            + "."
            );
        }
    }

    private List<Future<?>> startLaneWorkers(ExecutorService laneExecutor, List<CheckoutLane> lanes) {
        List<Future<?>> futures = new ArrayList<>(lanes.size());
        for (CheckoutLane lane : lanes) {
            futures.add(laneExecutor.submit(() -> runLaneTask(lane)));
        }
        return futures;
    }

    private CheckoutLane selectShortestLane() {
        return checkoutLanes.stream()
                .min(Comparator.comparingInt(CheckoutLane::queueSize).thenComparingInt(CheckoutLane::laneId))
                .orElseThrow(() -> new IllegalStateException("No checkout lanes are configured"));
    }

    private void simulateShoppingDelay() throws InterruptedException {
        if (config.maxShoppingMillis() == 0) {
            return;
        }

        int delay = config.minShoppingMillis();
        if (config.maxShoppingMillis() > config.minShoppingMillis()) {
            delay = ThreadLocalRandom.current().nextInt(config.minShoppingMillis(), config.maxShoppingMillis() + 1);
        }
        if (delay > 0) {
            TimeUnit.MILLISECONDS.sleep(delay);
        }
    }

    private void waitFor(List<Future<?>> futures) throws InterruptedException, ExecutionException {
        for (Future<?> future : futures) {
            future.get();
        }
    }

    private void resetState() {
        receipts.clear();
        completedCustomers.set(0);
        rejectedCustomers.set(0);
        revenueInCents.set(0);
    }

    private void publishEvent(StoreEventType type, String message) {
        publishEvent(type, message, snapshot());
    }

    private void publishEvent(StoreEventType type, String message, StoreSnapshot snapshot) {
        StoreEvent event = new StoreEvent(Instant.now(), type, message, snapshot);
        for (StoreListener listener : listeners) {
            try {
                listener.onEvent(event);
            } catch (RuntimeException ignored) {
                // Listener failures should not stop the simulation engine.
            }
        }
    }

    private List<CheckoutLane> createCheckoutLanes() {
        return IntStream.rangeClosed(1, config.laneCount())
                .mapToObj(CheckoutLane::new)
                .toList();
    }

    private BigDecimal centsToBigDecimal(long cents) {
        return BigDecimal.valueOf(cents, 2).setScale(2, RoundingMode.HALF_UP);
    }

    private record CustomerOrder(
            Customer customer,
            Map<String, Integer> reservedItems,
            int totalItems,
            BigDecimal total,
            boolean poisonPill
    ) {
        private CustomerOrder {
            if (!poisonPill) {
                Objects.requireNonNull(customer, "customer");
                Objects.requireNonNull(reservedItems, "reservedItems");
                Objects.requireNonNull(total, "total");
                reservedItems = Map.copyOf(reservedItems);
                if (totalItems <= 0) {
                    throw new IllegalArgumentException("totalItems must be greater than zero");
                }
            }
        }

        private CustomerOrder(Customer customer, Map<String, Integer> reservedItems, int totalItems, BigDecimal total) {
            this(customer, reservedItems, totalItems, total, false);
        }

        private static CustomerOrder poison() {
            return new CustomerOrder(null, Map.of(), 0, BigDecimal.ZERO, true);
        }
    }

    private static final class CheckoutLane {
        private final int laneId;
        private final BlockingQueue<CustomerOrder> queue = new LinkedBlockingQueue<>();

        private CheckoutLane(int laneId) {
            this.laneId = laneId;
        }

        private int laneId() {
            return laneId;
        }

        private void submit(CustomerOrder order) throws InterruptedException {
            queue.put(order);
        }

        private CustomerOrder take() throws InterruptedException {
            return queue.take();
        }

        private int queueSize() {
            return queue.size();
        }
    }
}
