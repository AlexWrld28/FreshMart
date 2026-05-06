package com.grocery.controller;

import com.grocery.dto.LoginRequest;
import com.grocery.model.User;
import com.grocery.repository.UserRepository;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import java.util.HashMap;
import java.util.Map;
import java.util.Optional;

@RestController
@RequestMapping("/api/auth")
@CrossOrigin(origins = "*")
public class AuthController {

    private final UserRepository userRepository;

    public AuthController(UserRepository userRepository) {
        this.userRepository = userRepository;
    }

    @PostMapping("/login")
    public ResponseEntity<?> login(@RequestBody LoginRequest request) {
        Optional<User> userOpt = userRepository.findByEmail(request.getEmail());
        if (userOpt.isEmpty()) {
            return ResponseEntity.status(401).body(Map.of("message", "Invalid email or password"));
        }
        User user = userOpt.get();
        if (!user.getPassword().equals(request.getPassword())) {
            return ResponseEntity.status(401).body(Map.of("message", "Invalid email or password"));
        }
        if (!user.isEnabled()) {
            return ResponseEntity.status(403).body(Map.of("message", "Your account has been disabled"));
        }
        Map<String, Object> response = new HashMap<>();
        response.put("id", user.getId());
        response.put("email", user.getEmail());
        response.put("fullName", user.getFullName());
        response.put("role", user.getRole());
        response.put("balance", user.getBalance());
        return ResponseEntity.ok(response);
    }

    @PostMapping("/register")
    public ResponseEntity<?> register(@RequestBody User newUser) {
        if (newUser.getConfirmPassword() != null && !newUser.getPassword().equals(newUser.getConfirmPassword())) {
            return ResponseEntity.badRequest().body(Map.of("message", "Passwords do not match"));
        }
        if (userRepository.findByEmail(newUser.getEmail()).isPresent()) {
            return ResponseEntity.badRequest().body(Map.of("message", "Email already in use"));
        }
        newUser.setRole("CUSTOMER");
        newUser.setEnabled(true);
        newUser.setBalance(0.0);
        User saved = userRepository.save(newUser);
        Map<String, Object> response = new HashMap<>();
        response.put("id", saved.getId());
        response.put("email", saved.getEmail());
        response.put("fullName", saved.getFullName());
        response.put("role", saved.getRole());
        response.put("balance", saved.getBalance());
        return ResponseEntity.ok(response);
    }
}