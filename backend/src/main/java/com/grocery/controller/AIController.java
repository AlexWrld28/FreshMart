package com.grocery.controller;

import com.grocery.service.AIService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/ai")
@CrossOrigin(origins = "*")
public class AIController {

    private final AIService aiService;

    public AIController(AIService aiService) {
        this.aiService = aiService;
    }

    @PostMapping("/grocery-list")
    public ResponseEntity<?> generateList(@RequestBody Map<String, Object> body) {
        try {
            String prompt = (String) body.get("prompt");
            String dietaryFilter = (String) body.getOrDefault("dietaryFilter", "None");
            Double budget = body.get("budget") != null ? Double.parseDouble(body.get("budget").toString()) : null;

            if (prompt == null || prompt.isBlank()) {
                return ResponseEntity.badRequest().body("Prompt is required");
            }

            List<Map<String, Object>> list = aiService.generateGroceryList(prompt, dietaryFilter, budget);
            return ResponseEntity.ok(list);
        } catch (Exception e) {
            return ResponseEntity.internalServerError().body(e.getMessage());
        }
    }
}
