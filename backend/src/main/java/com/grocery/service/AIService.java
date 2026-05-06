package com.grocery.service;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.grocery.model.Product;
import com.grocery.repository.ProductRepository;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.util.*;

@Service
public class AIService {

    @Value("${gemini.api.key}")
    private String apiKey;

    @Value("${gemini.api.url}")
    private String apiUrl;

    private final HttpClient httpClient = HttpClient.newHttpClient();
    private final ObjectMapper objectMapper;
    private final ProductRepository productRepository;

    public AIService(ObjectMapper objectMapper, ProductRepository productRepository) {
        this.objectMapper = objectMapper;
        this.productRepository = productRepository;
    }

    public List<Map<String, Object>> generateGroceryList(String userPrompt, String dietaryFilter, Double budget) throws Exception {

        String dietaryNote = (dietaryFilter != null && !dietaryFilter.equals("None"))
                ? "IMPORTANT: This is a strict " + dietaryFilter + " grocery list. Do NOT include any " +
                switch (dietaryFilter) {
                    case "Vegan" -> "meat, poultry, seafood, dairy, or eggs.";
                    case "Vegetarian" -> "meat, poultry, or seafood.";
                    case "Gluten-Free" -> "wheat, barley, rye, or gluten-containing products.";
                    case "Dairy-Free" -> "milk, cheese, butter, yogurt, or any dairy products.";
                    case "Halal" -> "pork or non-halal meat products.";
                    case "Kosher" -> "pork, shellfish, or mixing of meat and dairy.";
                    default -> "non-" + dietaryFilter + " items.";
                }
                : "";

        String budgetNote = (budget != null && budget > 0)
                ? "The total estimated cost must fit within a $" + budget + " budget. Adjust quantities accordingly."
                : "";

        String fullPrompt = """
            You are a grocery list assistant for a store called FreshMart.
            When the user describes a meal or need, respond ONLY with a JSON array.
            No markdown, no extra text, just the raw JSON array like this:
            [
              {"name": "ground beef", "quantity": "1 lb", "category": "Meat"},
              {"name": "tortillas", "quantity": "1 pack", "category": "Pantry"}
            ]
            Use only these categories: Produce, Dairy, Meat, Seafood, Bakery, Frozen, Beverages, Pantry.
            """ + dietaryNote + "\n" + budgetNote + """
            \nUser request: """ + userPrompt;

        Map<String, Object> requestBody = Map.of(
                "contents", List.of(
                        Map.of("parts", List.of(Map.of("text", fullPrompt)))
                )
        );

        String requestJson = objectMapper.writeValueAsString(requestBody);

        HttpRequest request = HttpRequest.newBuilder()
                .uri(URI.create(apiUrl))
                .header("Content-Type", "application/json")
                .header("x-goog-api-key", apiKey)
                .POST(HttpRequest.BodyPublishers.ofString(requestJson))
                .build();

        HttpResponse<String> httpResponse = httpClient.send(request, HttpResponse.BodyHandlers.ofString());

        if (httpResponse.statusCode() != 200) {
            throw new RuntimeException(httpResponse.statusCode() + " " + httpResponse.body());
        }

        JsonNode root = objectMapper.readTree(httpResponse.body());
        String content = root
                .path("candidates").get(0)
                .path("content").path("parts").get(0)
                .path("text").asText();

        content = content.replaceAll("(?s)```json\\s*", "").replaceAll("```", "").trim();
        JsonNode items = objectMapper.readTree(content);

        List<Map<String, Object>> result = new ArrayList<>();
        double runningTotal = 0;

        for (JsonNode item : items) {
            String name = item.path("name").asText();
            String quantity = item.path("quantity").asText();
            String category = item.path("category").asText();

            List<Product> matches = productRepository.findByNameContainingIgnoreCase(name);
            if (matches.isEmpty()) {
                matches = productRepository.findByCategory(category);
            }

            Map<String, Object> entry = new LinkedHashMap<>();
            entry.put("suggestedName", name);
            entry.put("suggestedQuantity", quantity);
            entry.put("category", category);

            if (!matches.isEmpty()) {
                Product cheapest = matches.stream()
                        .filter(p -> p.getQuantity() > 0)
                        .min(Comparator.comparingDouble(Product::getPrice))
                        .orElse(matches.get(0));

                if (budget != null && budget > 0 && runningTotal + cheapest.getPrice() > budget) {
                    continue;
                }

                runningTotal += cheapest.getPrice();
                entry.put("matchedProductId", cheapest.getId());
                entry.put("matchedProductName", cheapest.getName());
                entry.put("price", cheapest.getPrice());
                entry.put("inStock", cheapest.getQuantity() > 0);
            } else {
                entry.put("matchedProductId", null);
                entry.put("matchedProductName", "Not available");
                entry.put("price", 0.0);
                entry.put("inStock", false);
            }

            result.add(entry);
        }

        return result;
    }
}
