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
import java.util.List;
import java.util.Map;
import java.util.Optional;
import com.grocery.dto.PurchaseItemRequest;
import com.grocery.model.OrderItem;
import java.util. ArrayList;

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


        System.out.println("Request items: " + request.getItems());

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


        for(PurchaseItemRequest itemRequest : request.getItems()){

            System.out.println("PRODUCT ID = " + itemRequest.getProductId());
            System.out.println("QUANTITY = " + itemRequest.getQuantity());


            Optional<Product> productOpt = productRepository.findById(itemRequest.getProductId());

            if (productOpt.isEmpty()) {
                return ResponseEntity.badRequest().body(Map.of("message", "Product not found"));
            }

            Product product = productOpt.get();

            if (product.getQuantity() < itemRequest.getQuantity()) {
                return ResponseEntity.badRequest().body(Map.of(
                        "message", "Not enough stock available for " + product.getName()
                ));
            }

            double itemTotal = product.getPrice() * itemRequest.getQuantity();
            orderTotal += itemTotal;

            OrderItem orderItem = new OrderItem();
            orderItem.setOrder(order);
            orderItem.setProductId(product.getId());
            orderItem.setProductName(product.getName());
            orderItem.setQuantity(itemRequest.getQuantity());
            orderItem.setPriceEach(product.getPrice());
            orderItem.setTotalPrice(itemTotal);

            order.getItems().add(orderItem);
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

        for (PurchaseItemRequest itemRequest : request.getItems()) {
            Product product = productRepository.findById(itemRequest.getProductId()).get();
            product.setQuantity(product.getQuantity() - itemRequest.getQuantity());
            productRepository.save(product);
        }

        order.setTotalPrice(orderTotal);

        Order savedOrder = orderRepository.save(order);

        for (OrderItem item : savedOrder.getItems()) {
            System.out.println("Saved item: " + item.getProductName());
        }

        return ResponseEntity.ok(Map.of(
            "message", "Purchase successful",
            "newBalance", user.getBalance(),
            "orderId", order.getId()
        ));
    }

    @GetMapping("/revenue")
    public ResponseEntity<?> getRevenue() {
        List<Order> orders = orderRepository.findAll();
        double total = orders.stream().mapToDouble(Order::getTotalPrice).sum();
        return ResponseEntity.ok(Map.of("totalRevenue", total, "totalOrders", orders.size()));
    }
}
