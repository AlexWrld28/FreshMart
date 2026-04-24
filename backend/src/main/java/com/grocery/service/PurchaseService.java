package com.grocery.service;

import com.grocery.dto.PurchaseRequest;
import com.grocery.model.Order;
import com.grocery.model.Product;
import com.grocery.model.User;
import com.grocery.repository.OrderRepository;
import com.grocery.repository.ProductRepository;
import com.grocery.repository.UserRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Locale;
import java.util.Objects;

@Service
public class PurchaseService {

    private final OrderRepository orderRepository;
    private final ProductRepository productRepository;
    private final UserRepository userRepository;

    public PurchaseService(OrderRepository orderRepository, ProductRepository productRepository, UserRepository userRepository) {
        this.orderRepository = orderRepository;
        this.productRepository = productRepository;
        this.userRepository = userRepository;
    }

    @Transactional
    public PurchaseResult purchase(PurchaseRequest request) {
        Objects.requireNonNull(request, "request must not be null");
        if (request.getUserId() == null) {
            throw new IllegalArgumentException("User not found");
        }
        if (request.getProductId() == null) {
            throw new IllegalArgumentException("Product not found");
        }
        if (request.getQuantity() <= 0) {
            throw new IllegalArgumentException("Quantity must be greater than zero");
        }

        User user = userRepository.findById(request.getUserId())
                .orElseThrow(() -> new IllegalArgumentException("User not found"));
        Product product = productRepository.findById(request.getProductId())
                .orElseThrow(() -> new IllegalArgumentException("Product not found"));

        double total = product.getPrice() * request.getQuantity();

        if (productRepository.decrementQuantityIfAvailable(product.getId(), request.getQuantity()) == 0) {
            throw new IllegalArgumentException("Not enough stock available");
        }

        if (userRepository.deductBalanceIfSufficient(user.getId(), total) == 0) {
            double currentBalance = userRepository.findById(user.getId())
                    .map(User::getBalance)
                    .orElse(0.0);
            throw new IllegalArgumentException(
                    "Insufficient balance. You need $"
                            + formatAmount(total)
                            + " but have $"
                            + formatAmount(currentBalance)
            );
        }

        double newBalance = userRepository.findById(user.getId())
                .map(User::getBalance)
                .orElseThrow(() -> new IllegalStateException("User disappeared during purchase"));

        Order order = new Order();
        order.setUserId(user.getId());
        order.setUserFullName(user.getFullName());
        order.setProductId(product.getId());
        order.setProductName(product.getName());
        order.setQuantity(request.getQuantity());
        order.setTotalPrice(total);
        order.setStatus("Confirmed");
        orderRepository.save(order);

        return new PurchaseResult(order.getId(), newBalance);
    }

    private String formatAmount(double amount) {
        return String.format(Locale.US, "%.2f", amount);
    }

    public record PurchaseResult(Long orderId, double newBalance) {
    }
}
