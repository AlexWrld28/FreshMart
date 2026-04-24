package com.grocery.controller;

import com.grocery.dto.PurchaseRequest;
import com.grocery.model.Order;
import com.grocery.repository.OrderRepository;
import com.grocery.service.PurchaseService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/orders")
@CrossOrigin(origins = "*")
public class OrderController {

    private final OrderRepository orderRepository;
    private final PurchaseService purchaseService;

    public OrderController(OrderRepository orderRepository, PurchaseService purchaseService) {
        this.orderRepository = orderRepository;
        this.purchaseService = purchaseService;
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
        try {
            PurchaseService.PurchaseResult result = purchaseService.purchase(request);
            return ResponseEntity.ok(Map.of(
                    "message", "Purchase successful",
                    "newBalance", result.newBalance(),
                    "orderId", result.orderId()
            ));
        } catch (IllegalArgumentException illegalArgumentException) {
            return ResponseEntity.badRequest().body(Map.of("message", illegalArgumentException.getMessage()));
        }
    }

    @GetMapping("/revenue")
    public ResponseEntity<?> getRevenue() {
        List<Order> orders = orderRepository.findAll();
        double total = orders.stream().mapToDouble(Order::getTotalPrice).sum();
        return ResponseEntity.ok(Map.of("totalRevenue", total, "totalOrders", orders.size()));
    }
}
