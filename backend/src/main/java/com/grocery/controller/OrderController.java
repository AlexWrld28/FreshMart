package com.grocery.controller;

import com.grocery.dto.PurchaseRequest;
import com.grocery.model.Order;
import com.grocery.model.Product;
import com.grocery.model.User;
import com.grocery.repository.OrderRepository;
import com.grocery.repository.ProductRepository;
import com.grocery.repository.UserRepository;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import jakarta.annotation.PreDestroy;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import com.grocery.dto.PurchaseItemRequest;
import com.grocery.model.OrderItem;
import java.util.ArrayList;
import java.util.concurrent.Callable;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.concurrent.atomic.AtomicInteger;

@RestController
@RequestMapping("/api/orders")
@CrossOrigin(origins = "*")
public class OrderController {

    private static final AtomicInteger THREAD_ID = new AtomicInteger(1);

    private final OrderRepository orderRepository;
    private final ProductRepository productRepository;
    private final UserRepository userRepository;
    private final ExecutorService itemExecutor;

    public OrderController(OrderRepository orderRepository, ProductRepository productRepository, UserRepository userRepository) {
        this.orderRepository = orderRepository;
        this.productRepository = productRepository;
        this.userRepository = userRepository;
        this.itemExecutor = Executors.newFixedThreadPool(4, runnable -> {
            Thread thread = new Thread(runnable);
            thread.setName("order-item-worker-" + THREAD_ID.getAndIncrement());
            System.out.println("[thread-created] " + thread.getName());
            return thread;
        });
    }

    @PreDestroy
    public void shutdownItemExecutor() {
        itemExecutor.shutdown();
    }

    @GetMapping
    public List<Order> getAllOrders() {
        return orderRepository.findAllByOrderByCreatedAtDesc();
    }

    @GetMapping("/user/{userId}")
    public List<Order> getUserOrders(@PathVariable Long userId) {
        return orderRepository.findByUserId(userId);
    }

    @GetMapping("/{orderId}")
    public ResponseEntity<?> getOrderById(@PathVariable Long orderId) {
        Optional<Order> orderOpt = orderRepository.findById(orderId);

        if (orderOpt.isEmpty()) {
            return ResponseEntity.notFound().build();
        }

        return ResponseEntity.ok(orderOpt.get());
    }

    @PostMapping("/purchase")
    public ResponseEntity<?> purchase(@RequestBody PurchaseRequest request) {

        Optional<User> userOpt = userRepository.findById(request.getUserId());

        if (userOpt.isEmpty()) {
            return ResponseEntity.badRequest().body(Map.of("message", "User not found"));
        }

        if (request.getItems() == null || request.getItems().isEmpty()) {
            return ResponseEntity.badRequest().body(Map.of("message", "Product not found"));
        }

        User user = userOpt.get();

        double orderTotal = 0.0;

        Order order = new Order();
        order.setUserId(user.getId());
        order.setUserFullName(user.getFullName());
        order.setStatus("Confirmed");
        order.setItems(new ArrayList<>());

        List<Callable<ItemComputation>> tasks = new ArrayList<>();
        for (PurchaseItemRequest itemRequest : request.getItems()) {
            tasks.add(() -> computeItem(itemRequest));
        }

        List<ItemComputation> itemComputations = new ArrayList<>();
        List<Future<ItemComputation>> futures;
        try {
            futures = itemExecutor.invokeAll(tasks);
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            return ResponseEntity.internalServerError().body(Map.of("message", "Interrupted while processing items"));
        }

        for (Future<ItemComputation> future : futures) {
            try {
                ItemComputation computation = future.get();
                itemComputations.add(computation);
                orderTotal += computation.itemTotal;

                OrderItem orderItem = new OrderItem();
                orderItem.setOrder(order);
                orderItem.setProductId(computation.product.getId());
                orderItem.setProductName(computation.product.getName());
                orderItem.setQuantity(computation.quantity);
                orderItem.setPriceEach(computation.product.getPrice());
                orderItem.setTotalPrice(computation.itemTotal);

                order.getItems().add(orderItem);
            } catch (ExecutionException e) {
                String message = e.getCause() != null ? e.getCause().getMessage() : "Item processing failed";
                return ResponseEntity.badRequest().body(Map.of("message", message));
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
                return ResponseEntity.internalServerError().body(Map.of("message", "Interrupted while processing items"));
            }
        }

        if (user.getBalance() < orderTotal) {
            return ResponseEntity.badRequest().body(Map.of(
                    "message", "Insufficient balance. You need $" +
                            String.format("%.2f", orderTotal) +
                            " but have $" +
                            String.format("%.2f", user.getBalance())
            ));
        }

        user.setBalance(user.getBalance() - orderTotal);
        userRepository.save(user);

        for (ItemComputation computation : itemComputations) {
            Product product = computation.product;
            product.setQuantity(product.getQuantity() - computation.quantity);
            productRepository.save(product);
        }

        order.setTotalPrice(orderTotal);

        Order savedOrder = orderRepository.save(order);

        return ResponseEntity.ok(Map.of(
            "message", "Purchase successful",
            "newBalance", user.getBalance(),
            "orderId", order.getId()
        ));
    }

    private ItemComputation computeItem(PurchaseItemRequest itemRequest) {
        String threadName = Thread.currentThread().getName();
        System.out.println("[order-item-task] " + threadName + " processing productId=" + itemRequest.getProductId()
                + " requestedQty=" + itemRequest.getQuantity());

        Optional<Product> productOpt = productRepository.findById(itemRequest.getProductId());
        if (productOpt.isEmpty()) {
            throw new IllegalArgumentException("Product not found");
        }

        Product product = productOpt.get();
        if (product.getQuantity() < itemRequest.getQuantity()) {
            throw new IllegalStateException("Not enough stock available for " + product.getName());
        }

        double itemTotal = product.getPrice() * itemRequest.getQuantity();
        System.out.println("[order-item-task] " + threadName
                + " product=" + product.getName()
                + " price=" + product.getPrice()
                + " qty=" + itemRequest.getQuantity()
                + " itemTotal=" + itemTotal);
        return new ItemComputation(product, itemRequest.getQuantity(), itemTotal);
    }

    private static class ItemComputation {
        private final Product product;
        private final int quantity;
        private final double itemTotal;

        private ItemComputation(Product product, int quantity, double itemTotal) {
            this.product = product;
            this.quantity = quantity;
            this.itemTotal = itemTotal;
        }
    }

    @GetMapping("/revenue")
    public ResponseEntity<?> getRevenue() {
        List<Order> orders = orderRepository.findAll();
        double total = orders.stream().mapToDouble(Order::getTotalPrice).sum();
        return ResponseEntity.ok(Map.of("totalRevenue", total, "totalOrders", orders.size()));
    }
}
