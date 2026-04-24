package com.grocery.service;

import com.grocery.dto.PurchaseRequest;
import com.grocery.model.Product;
import com.grocery.model.User;
import com.grocery.repository.OrderRepository;
import com.grocery.repository.ProductRepository;
import com.grocery.repository.UserRepository;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.TestPropertySource;

import java.util.List;
import java.util.concurrent.Callable;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

@SpringBootTest
@TestPropertySource(properties = {
        "spring.datasource.url=jdbc:h2:mem:grocery-test;DB_CLOSE_DELAY=-1;LOCK_TIMEOUT=10000",
        "spring.datasource.driver-class-name=org.h2.Driver",
        "spring.jpa.database-platform=org.hibernate.dialect.H2Dialect",
        "spring.jpa.hibernate.ddl-auto=create-drop",
        "spring.jpa.show-sql=false"
})
class PurchaseServiceConcurrencyTest {

    @Autowired
    private PurchaseService purchaseService;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private ProductRepository productRepository;

    @Autowired
    private OrderRepository orderRepository;

    @BeforeEach
    void setUp() {
        orderRepository.deleteAll();
        userRepository.deleteAll();
        productRepository.deleteAll();
    }

    @AfterEach
    void tearDown() {
        orderRepository.deleteAll();
        userRepository.deleteAll();
        productRepository.deleteAll();
    }

    @Test
    void shouldOnlyAllowOneConcurrentPurchaseOfTheLastItem() throws Exception {
        Product product = new Product();
        product.setName("Milk");
        product.setCategory("Dairy");
        product.setPrice(3.49);
        product.setQuantity(1);
        product = productRepository.save(product);

        User firstUser = createCustomer("first@example.com");
        User secondUser = createCustomer("second@example.com");

        ExecutorService executorService = Executors.newFixedThreadPool(2);
        CountDownLatch ready = new CountDownLatch(2);
        CountDownLatch start = new CountDownLatch(1);
        try {
            Future<Boolean> firstAttempt = executorService.submit(purchaseTask(firstUser.getId(), product.getId(), ready, start));
            Future<Boolean> secondAttempt = executorService.submit(purchaseTask(secondUser.getId(), product.getId(), ready, start));

            ready.await();
            start.countDown();

            int successes = 0;
            successes += firstAttempt.get() ? 1 : 0;
            successes += secondAttempt.get() ? 1 : 0;

            assertEquals(1, successes);
            assertEquals(1, orderRepository.count());
            assertEquals(0, productRepository.findById(product.getId()).orElseThrow().getQuantity());

            double firstBalance = userRepository.findById(firstUser.getId()).orElseThrow().getBalance();
            double secondBalance = userRepository.findById(secondUser.getId()).orElseThrow().getBalance();
            assertEquals(196.51, firstBalance + secondBalance, 0.001);
            assertTrue(Math.abs(firstBalance - 96.51) < 0.001 || Math.abs(secondBalance - 96.51) < 0.001);
            assertTrue(Math.abs(firstBalance - 100.0) < 0.001 || Math.abs(secondBalance - 100.0) < 0.001);
        } finally {
            executorService.shutdownNow();
        }
    }

    @Test
    void shouldAccumulateConcurrentBalanceUpdates() throws Exception {
        User user = createCustomer("wallet@example.com");

        ExecutorService executorService = Executors.newFixedThreadPool(4);
        CountDownLatch ready = new CountDownLatch(4);
        CountDownLatch start = new CountDownLatch(1);
        try {
            List<Future<Integer>> futures = List.of(
                    executorService.submit(topUpTask(user.getId(), ready, start)),
                    executorService.submit(topUpTask(user.getId(), ready, start)),
                    executorService.submit(topUpTask(user.getId(), ready, start)),
                    executorService.submit(topUpTask(user.getId(), ready, start))
            );

            ready.await();
            start.countDown();

            for (Future<Integer> future : futures) {
                assertEquals(1, future.get());
            }

            double updatedBalance = userRepository.findById(user.getId()).orElseThrow().getBalance();
            assertEquals(200.0, updatedBalance, 0.001);
        } finally {
            executorService.shutdownNow();
        }
    }

    private Callable<Boolean> purchaseTask(Long userId, Long productId, CountDownLatch ready, CountDownLatch start) {
        return () -> {
            PurchaseRequest request = new PurchaseRequest();
            request.setUserId(userId);
            request.setProductId(productId);
            request.setQuantity(1);
            ready.countDown();
            start.await();
            try {
                purchaseService.purchase(request);
                return true;
            } catch (IllegalArgumentException illegalArgumentException) {
                return false;
            }
        };
    }

    private Callable<Integer> topUpTask(Long userId, CountDownLatch ready, CountDownLatch start) {
        return () -> {
            ready.countDown();
            start.await();
            return userRepository.incrementBalance(userId, 25.0);
        };
    }

    private User createCustomer(String email) {
        User user = new User();
        user.setEmail(email);
        user.setPassword("password");
        user.setFullName("Thread Test");
        user.setRole("CUSTOMER");
        user.setEnabled(true);
        user.setBalance(100.0);
        return userRepository.save(user);
    }
}
