package com.grocery.config;

import com.grocery.model.Product;
import com.grocery.model.User;
import com.grocery.repository.ProductRepository;
import com.grocery.repository.UserRepository;
import org.springframework.boot.CommandLineRunner;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class DataSeeder {

    @Bean
    public CommandLineRunner seedData(UserRepository userRepository, ProductRepository productRepository) {
        return args -> {
            if (userRepository.count() == 0) {
                User admin = new User();
                admin.setEmail("admin@freshmart.com");
                admin.setPassword("admin123");
                admin.setFullName("James Carter");
                admin.setRole("ADMIN");
                admin.setEnabled(true);
                admin.setBalance(0.0);
                userRepository.save(admin);
            }

            if (productRepository.count() == 0) {
                String[][] products = {
                        {"Whole Milk (1 Gallon)", "Dairy", "3.99", "50", "Fresh whole milk from local farms"},
                        {"Large Eggs (12 ct)", "Dairy", "4.49", "80", "Grade A large white eggs"},
                        {"Cheddar Cheese (8 oz)", "Dairy", "5.29", "40", "Sharp cheddar cheese slices"},
                        {"Butter (1 lb)", "Dairy", "4.99", "35", "Unsalted sweet cream butter"},
                        {"Greek Yogurt (32 oz)", "Dairy", "6.49", "30", "Plain whole milk Greek yogurt"},
                        {"White Bread (20 oz)", "Bakery", "3.29", "60", "Classic soft white sandwich bread"},
                        {"Sourdough Bread", "Bakery", "4.99", "25", "Artisan sourdough loaf"},
                        {"Blueberry Muffins (4 ct)", "Bakery", "4.49", "20", "Fresh baked blueberry muffins"},
                        {"Bagels (6 ct)", "Bakery", "3.99", "30", "New York style plain bagels"},
                        {"Boneless Chicken Breast (2 lb)", "Meat", "8.99", "45", "Fresh boneless skinless chicken breast"},
                        {"Ground Beef 80/20 (1 lb)", "Meat", "6.49", "50", "Fresh ground beef 80% lean"},
                        {"Bacon (16 oz)", "Meat", "7.99", "35", "Thick cut hickory smoked bacon"},
                        {"Pork Chops (2 lb)", "Meat", "9.49", "20", "Bone-in center cut pork chops"},
                        {"Atlantic Salmon (1 lb)", "Seafood", "12.99", "15", "Fresh Atlantic salmon fillet"},
                        {"Shrimp (1 lb)", "Seafood", "11.49", "20", "Peeled and deveined medium shrimp"},
                        {"Bananas (bunch)", "Produce", "1.29", "100", "Fresh yellow bananas"},
                        {"Gala Apples (3 lb bag)", "Produce", "4.99", "60", "Sweet crisp Gala apples"},
                        {"Strawberries (1 lb)", "Produce", "3.99", "40", "Fresh ripe strawberries"},
                        {"Broccoli (1 head)", "Produce", "1.99", "55", "Fresh green broccoli crown"},
                        {"Russet Potatoes (5 lb)", "Produce", "3.49", "70", "Idaho russet baking potatoes"},
                        {"Baby Spinach (5 oz)", "Produce", "3.29", "45", "Fresh pre-washed baby spinach"},
                        {"Roma Tomatoes (1 lb)", "Produce", "1.99", "65", "Firm fresh Roma tomatoes"},
                        {"Orange Juice (52 oz)", "Beverages", "4.99", "40", "100% pure squeezed orange juice"},
                        {"Coca-Cola (12 pack)", "Beverages", "7.99", "55", "Classic Coca-Cola cans"},
                        {"Spring Water (24 pack)", "Beverages", "4.49", "80", "Pure spring water bottles"},
                        {"Pasta (16 oz)", "Pantry", "1.79", "90", "Barilla spaghetti pasta"},
                        {"Marinara Sauce (24 oz)", "Pantry", "3.49", "75", "Classic tomato marinara sauce"},
                        {"Peanut Butter (16 oz)", "Pantry", "3.99", "60", "Creamy peanut butter"},
                        {"Cheerios (18 oz)", "Pantry", "4.99", "50", "Original whole grain Cheerios cereal"},
                        {"Frozen Pizza (12 inch)", "Frozen", "6.99", "30", "DiGiorno pepperoni frozen pizza"}
                };

                for (String[] p : products) {
                    Product product = new Product();
                    product.setName(p[0]);
                    product.setCategory(p[1]);
                    product.setPrice(Double.parseDouble(p[2]));
                    product.setQuantity(Integer.parseInt(p[3]));
                    product.setDescription(p[4]);
                    productRepository.save(product);
                }
            }
        };
    }
}