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
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

@RestController
@RequestMapping("/api/orders")
@CrossOrigin(origins = "*")
public class OrderController {

    private final OrderRepository orderRepository;
    private final ProductRepository productRepository;
    private final UserRepository userRepository;

    public OrderController(OrderRepository orderRepository, ProductRepository productRepository, UserRepository userRepository) {
        this.orderRepository = orderRepository;
        this.productRepository = productRepository;
        this.userRepository = userRepository;
    }

    @GetMapping
    public List<Order> getAllOrders() {
        return orderRepository.findAllByOrderByCreatedAtDesc();
    }

    @GetMapping("/user/{userId}")
    public List<Order> getUserOrders(@PathVariable Long userId) {
        return orderRepository.findByUserId(userId);
    }

    @PostMapping("/purchase")
    public ResponseEntity<?> purchase(@RequestBody PurchaseRequest request) {
        String backendThread = Thread.currentThread().getName();
        String requestedProductName = request.getProductName() == null ? "(not supplied)" : request.getProductName();
        System.out.printf(
            "[thread-receipt] BACKEND RECEIVED checkoutId=%s purchaseTraceId=%s productId=%d requestedProduct=%s quantity=%d thread=%s%n",
            request.getCheckoutId(),
            request.getPurchaseTraceId(),
            request.getProductId(),
            requestedProductName,
            request.getQuantity(),
            backendThread
        );

        Optional<User> userOpt = userRepository.findById(request.getUserId());
        Optional<Product> productOpt = productRepository.findById(request.getProductId());

        if (userOpt.isEmpty()) return ResponseEntity.badRequest().body(Map.of("message", "User not found"));
        if (productOpt.isEmpty()) return ResponseEntity.badRequest().body(Map.of("message", "Product not found"));

        User user = userOpt.get();
        Product product = productOpt.get();

        if (product.getQuantity() < request.getQuantity()) {
            return ResponseEntity.badRequest().body(Map.of("message", "Not enough stock available"));
        }

        double total = product.getPrice() * request.getQuantity();

        if (user.getBalance() < total) {
            return ResponseEntity.badRequest().body(Map.of("message", "Insufficient balance. You need $" + String.format("%.2f", total) + " but have $" + String.format("%.2f", user.getBalance())));
        }

        user.setBalance(user.getBalance() - total);
        userRepository.save(user);

        product.setQuantity(product.getQuantity() - request.getQuantity());
        productRepository.save(product);

        Order order = new Order();
        order.setUserId(user.getId());
        order.setUserFullName(user.getFullName());
        order.setProductId(product.getId());
        order.setProductName(product.getName());
        order.setQuantity(request.getQuantity());
        order.setTotalPrice(total);
        order.setStatus("Confirmed");
        orderRepository.save(order);

        System.out.printf(
            "[thread-receipt] BACKEND SAVED    checkoutId=%s purchaseTraceId=%s orderId=%d product=%s quantity=%d total=$%.2f thread=%s%n",
            request.getCheckoutId(),
            request.getPurchaseTraceId(),
            order.getId(),
            product.getName(),
            request.getQuantity(),
            total,
            backendThread
        );

        Map<String, Object> response = new LinkedHashMap<>();
        response.put("message", "Purchase successful");
        response.put("newBalance", user.getBalance());
        response.put("orderId", order.getId());
        response.put("checkoutId", request.getCheckoutId());
        response.put("purchaseTraceId", request.getPurchaseTraceId());
        response.put("backendThread", backendThread);
        response.put("productId", product.getId());
        response.put("productName", product.getName());
        response.put("quantity", request.getQuantity());
        response.put("totalPrice", total);
        response.put("savedToBackend", true);
        return ResponseEntity.ok(response);
    }

    @GetMapping("/revenue")
    public ResponseEntity<?> getRevenue() {
        List<Order> orders = orderRepository.findAll();
        double total = orders.stream().mapToDouble(Order::getTotalPrice).sum();
        return ResponseEntity.ok(Map.of("totalRevenue", total, "totalOrders", orders.size()));
    }
}
