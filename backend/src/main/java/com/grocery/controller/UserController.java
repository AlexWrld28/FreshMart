package com.grocery.controller;

import com.grocery.dto.TopUpRequest;
import com.grocery.model.User;
import com.grocery.repository.UserRepository;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import java.util.List;
import java.util.Map;
import java.util.Optional;

@RestController
@RequestMapping("/api/users")
@CrossOrigin(origins = "*")
public class UserController {

    private final UserRepository userRepository;

    public UserController(UserRepository userRepository) {
        this.userRepository = userRepository;
    }

    @GetMapping
    public List<User> getAllUsers() {
        return userRepository.findAll();
    }

    @GetMapping("/customers")
    public List<User> getCustomers() {
        return userRepository.findByRole("CUSTOMER");
    }

    @GetMapping("/{id}")
    public ResponseEntity<?> getUser(@PathVariable Long id) {
        Optional<User> user = userRepository.findById(id);
        return user.map(ResponseEntity::ok).orElse(ResponseEntity.notFound().build());
    }

    @PutMapping("/{id}/toggle")
    public ResponseEntity<?> toggleUserStatus(@PathVariable Long id) {
        Optional<User> userOpt = userRepository.findById(id);
        if (userOpt.isEmpty()) return ResponseEntity.notFound().build();
        User user = userOpt.get();
        user.setEnabled(!user.isEnabled());
        userRepository.save(user);
        return ResponseEntity.ok(Map.of("enabled", user.isEnabled()));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<?> deleteUser(@PathVariable Long id) {
        if (!userRepository.existsById(id)) return ResponseEntity.notFound().build();
        userRepository.deleteById(id);
        return ResponseEntity.ok(Map.of("message", "User deleted"));
    }

    @PostMapping("/topup")
    public ResponseEntity<?> topUp(@RequestBody TopUpRequest request) {
        if (request.getAmount() <= 0) {
            return ResponseEntity.badRequest().body(Map.of("message", "Amount must be greater than zero"));
        }
        if (request.getUserId() == null) {
            return ResponseEntity.badRequest().body(Map.of("message", "User not found"));
        }

        int updatedRows = userRepository.incrementBalance(request.getUserId(), request.getAmount());
        if (updatedRows == 0) return ResponseEntity.notFound().build();

        double newBalance = userRepository.findById(request.getUserId())
                .map(User::getBalance)
                .orElseThrow(() -> new IllegalStateException("User disappeared after balance update"));
        return ResponseEntity.ok(Map.of("balance", newBalance));
    }

    @PutMapping("/{id}")
    public ResponseEntity<?> updateUser(@PathVariable Long id, @RequestBody User updated) {
        Optional<User> userOpt = userRepository.findById(id);
        if (userOpt.isEmpty()) return ResponseEntity.notFound().build();
        User user = userOpt.get();
        if (updated.getFullName() != null) user.setFullName(updated.getFullName());
        if (updated.getEmail() != null) user.setEmail(updated.getEmail());
        if (updated.getPassword() != null && !updated.getPassword().isBlank()) user.setPassword(updated.getPassword());
        userRepository.save(user);
        return ResponseEntity.ok(user);
    }
}
