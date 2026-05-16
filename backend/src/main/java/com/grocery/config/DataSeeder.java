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

    private static final String U = "https://images.unsplash.com/photo-";
    private static final String Q = "?w=300&h=200&fit=crop";

    // Dairy
    private static final String MILK     = U + "BYlHH_1j2GA" + Q;
    private static final String EGGS     = U + "ECjY3Ypyl5g" + Q;
    private static final String CHEESE   = U + "1486297678162-eb2a19b0a32d" + Q;
    private static final String BUTTER   = U + "1589985270826-4b7bb135bc9d" + Q;
    private static final String YOGURT   = U + "LzrMzmVWhJw" + Q;
    private static final String CREAM    = U + "bBCRrplhhZ4" + Q;

    // Bakery
    private static final String BREAD    = U + "1549931319-a545dcf3bc73" + Q;
    private static final String SOURDOUGH= U + "1586444248902-0b9c0cf15f8a" + Q;
    private static final String MUFFIN   = U + "1607958996333-41aef7caefaa" + Q;
    private static final String BAGEL    = U + "1558961363-fa8fdf82db35" + Q;
    private static final String CROISSANT= U + "1555507036-ab1f4038808a" + Q;
    private static final String ROLLS    = U + "1509440159596-0249088772ff" + Q;
    private static final String TORTILLA = U + "1565117526622-9d8e7e04ffc9" + Q;

    // Meat
    private static final String CHICKEN  = U + "1604503468506-a8da13d11d36" + Q;
    private static final String BEEF     = U + "1551028719-00167b16eac5" + Q;
    private static final String STEAK    = U + "1529692236671-f1f6cf9683ba" + Q;
    private static final String BACON    = U + "1544025162-d76538d4adf4" + Q;
    private static final String PORK     = U + "1603048588240-499e3b23b4e8" + Q;
    private static final String SAUSAGE  = U + "1558030006-35700c30e4dc" + Q;
    private static final String HOTDOG   = U + "1567620905978-b08ed08de8a9" + Q;
    private static final String DELI     = U + "1607198179219-cd40c72e6b49" + Q;
    private static final String LAMB     = U + "1574181143987-64f3b0c5f448" + Q;

    // Seafood
    private static final String SALMON   = U + "1467003909585-2f8a72700288" + Q;
    private static final String SHRIMP   = U + "1510130387422-82bed34b37e9" + Q;
    private static final String FISH     = U + "1498654200218-4a3a4c477b1b" + Q;
    private static final String CRAB     = U + "1559742485-e9414f4b6e94" + Q;
    private static final String LOBSTER  = U + "1559942110-d35e32f7e5c4" + Q;
    private static final String SCALLOPS = U + "1599084993522-c8fe0e9c0a7d" + Q;

    // Produce
    private static final String BANANA   = U + "1571771894821-ce9b6c11b08e" + Q;
    private static final String APPLE    = U + "1570913149827-d2ac84ab3f9a" + Q;
    private static final String BERRY    = U + "1464965911861-746a04b4bca6" + Q;
    private static final String GRAPE    = U + "1537640538966-79f369143f8f" + Q;
    private static final String CITRUS   = U + "1582979512210-7b90dd90e5aa" + Q;
    private static final String AVOCADO  = U + "1523049673857-eb18f1d7b578" + Q;
    private static final String BROCCOLI = U + "1459411621453-7b03977f4bfc" + Q;
    private static final String POTATO   = U + "1518977676601-b53f82aba655" + Q;
    private static final String GREENS   = U + "1576045057995-568f588f82fb" + Q;
    private static final String TOMATO   = U + "1546094096-0df4bcaaa337" + Q;
    private static final String ONION    = U + "1618512496248-a4f8a9d69b37" + Q;
    private static final String PEPPER   = U + "1526346698789-22fd84314424" + Q;
    private static final String CARROT   = U + "1598170845058-32b4da92d908" + Q;
    private static final String MUSHROOM = U + "1504545102780-26774c1bb073" + Q;
    private static final String CORN     = U + "1601648764658-cf79f817e98c" + Q;
    private static final String MANGO    = U + "1550258987-190a2d41a8ba" + Q;

    // Beverages
    private static final String JUICE    = U + "1621506289937-a8e4df240d0b" + Q;
    private static final String SODA     = U + "1527960171744-2d73949d5b97" + Q;
    private static final String WATER    = U + "1548839140-29a749e1cf4d" + Q;
    private static final String COFFEE   = U + "1495474472287-4d71bcdd2085" + Q;
    private static final String TEA      = U + "1556679343-c7306c1976bc" + Q;
    private static final String SPORTS   = U + "1544947950-fa07a98d237f" + Q;

    // Pantry
    private static final String PASTA       = U + "1473093226584-3f46966d7af1" + Q;
    private static final String SAUCE       = U + "1536304993881-ff86d42d7bcc" + Q;
    private static final String PEANUTBUTTER= U + "1559181567-c3190e20edd5" + Q;
    private static final String JAM         = U + "1597528380849-7fcf7cc48e93" + Q;
    private static final String HONEY       = U + "1587049352846-4a222e784d38" + Q;
    private static final String CEREAL      = U + "1556909114-f6e7ad7d3136" + Q;
    private static final String OATS        = U + "1525351484163-7529414f2acd" + Q;
    private static final String RICE        = U + "1536304929831-ee1ca9d44906" + Q;
    private static final String BROTH       = U + "1547592166-23ac45744acd" + Q;
    private static final String BEANS       = U + "1512621776951-a57ef5c6a107" + Q;
    private static final String OIL         = U + "1474979723174-9be8823e07e5" + Q;
    private static final String FLOUR       = U + "1558618666-fcd25c85cd64" + Q;
    private static final String SUGAR       = U + "1558618666-fcd25c85cd64" + Q;
    private static final String SPICE       = U + "1532336544626-46e43c429b99" + Q;
    private static final String CHIPS       = U + "1621939182070-34d43ada28ea" + Q;
    private static final String CHOCOLATE   = U + "1481391319464-3c1c2e78f029" + Q;
    private static final String CANNED      = U + "1512621776951-a57ef5c6a107" + Q;
    private static final String KETCHUP     = U + "1607198179219-cd40c72e6b49" + Q;

    // Frozen
    private static final String FROZENPIZZA  = U + "1565299624946-b28f40a0ae38" + Q;
    private static final String NUGGETS      = U + "1562802378-063ec186a863" + Q;
    private static final String FRIES        = U + "1541592106381-b31e9677c0e5" + Q;
    private static final String WAFFLES      = U + "1607301405390-d831c242f59b" + Q;
    private static final String FROZENVEG    = U + "1490645935967-10de6ba17061" + Q;
    private static final String ICECREAM     = U + "1560008581-09ba4e2b5b3e" + Q;
    private static final String FISHSTICKS   = U + "1504674900247-0877df9cc836" + Q;
    private static final String LASAGNA      = U + "1574894709920-11b28f7367e3" + Q;
    private static final String BURRITO      = U + "1628871862-5e0e4b24f89a" + Q;

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

                User[] customers = {
                        createUser("john.hughes@email.com", "john123", "John Hughes"),
                        createUser("mary.brumly@email.com", "mary123", "Mary Brumly"),
                        createUser("catie.diane@email.com", "catie123", "Catie Diane"),
                        createUser("kelsi.texas@email.com", "kelsi123", "Kelsi Texas"),
                        createUser("lela.moon@email.com", "lela123", "Lela Moon"),
                };
                for (User c : customers) userRepository.save(c);
            }

            if (productRepository.count() == 0) {
                Object[][] products = {
                        // Dairy
                        {"Whole Milk (1 Gallon)", "Dairy", 3.99, 50, "Fresh whole milk from local farms", "images/wholeMilk.png"},
                        {"2% Reduced Fat Milk (1 Gallon)", "Dairy", 3.79, 45, "Smooth reduced fat milk", "images/2% fat reduced milk.jpg"},
                        {"Skim Milk (1 Gallon)", "Dairy", 3.59, 30, "Fat-free skim milk", "images/Skim milk.jpg"},
                        {"Large Eggs (12 ct)", "Dairy", 4.49, 80, "Grade A large white eggs", "images/dozen white eggs in open cardboard carton, white background, clean food photography.jpg"},
                        {"Large Eggs (18 ct)", "Dairy", 6.29, 50, "Grade A large white eggs family pack", "images/dozen white eggs in open cardboard carton, white background, clean food photography (1).jpg"},
                        {"Cheddar Cheese (8 oz)", "Dairy", 5.29, 40, "Sharp cheddar cheese slices", "images/Cheddar.jpg"},
                        {"Mozzarella Cheese (16 oz)", "Dairy", 6.49, 35, "Whole milk mozzarella cheese", "images/Moza.jpg"},
                        {"Parmesan Cheese (8 oz)", "Dairy", 7.99, 25, "Freshly grated parmesan", CHEESE},
                        {"Butter (1 lb)", "Dairy", 4.99, 35, "Unsalted sweet cream butter", BUTTER},
                        {"Salted Butter (1 lb)", "Dairy", 4.99, 35, "Salted sweet cream butter", "images/Salted Butter.jpg"},
                        {"Greek Yogurt (32 oz)", "Dairy", 6.49, 30, "Plain whole milk Greek yogurt", "images/plain white greek yogurt in glass bowl with spoon, clean background, food photography.jpg"},
                        {"Strawberry Yogurt (6 oz)", "Dairy", 1.29, 60, "Creamy strawberry flavored yogurt", "images/strawberry yogurt cup with fresh strawberries, food photography, white background.jpg"},
                        {"Vanilla Yogurt (6 oz)", "Dairy", 1.29, 60, "Smooth vanilla flavored yogurt", "images/vanilla yogurt in white cup with spoon, clean food photography.jpg"},
                        {"Cream Cheese (8 oz)", "Dairy", 3.49, 40, "Philadelphia original cream cheese", "images/block of cream cheese on white plate, food photography, clean background.jpg"},
                        {"Sour Cream (16 oz)", "Dairy", 2.99, 35, "Full fat sour cream", "images/sour cream in white bowl with spoon, food photography, clean background.jpg"},
                        {"Heavy Whipping Cream (1 pt)", "Dairy", 4.29, 25, "Fresh heavy whipping cream", "images/Heavy whipping cream.jpg"},
                        {"Almond Milk (64 oz)", "Dairy", 4.99, 30, "Unsweetened original almond milk", "images/Almond milk.jpg"},
                        {"Oat Milk (64 oz)", "Dairy", 5.49, 28, "Original oat milk plant based", "images/Oatmilk.jpg"},
                        {"Cottage Cheese (16 oz)", "Dairy", 3.79, 25, "Small curd cottage cheese", "images/cottage cheese.jpg"},
                        {"American Cheese Slices (16 ct)", "Dairy", 4.49, 30, "Individually wrapped cheese slices", "images/American cheese.jpg"},

                        // Bakery
                        {"White Bread (20 oz)", "Bakery", 3.29, 60, "Classic soft white sandwich bread", "images/White bread.jpg"},
                        {"Whole Wheat Bread (20 oz)", "Bakery", 3.79, 55, "100% whole wheat sandwich bread", "images/Whole wheat.jpg"},
                        {"Sourdough Bread", "Bakery", 4.99, 25, "Artisan sourdough loaf", "images/Sourdough.jpg"},
                        {"Multigrain Bread (24 oz)", "Bakery", 4.49, 30, "Hearty multigrain sandwich bread", "images/Multigrain.jpg"},
                        {"Blueberry Muffins (4 ct)", "Bakery", 4.49, 20, "Fresh baked blueberry muffins", "images/Blueberry muffins.jpg"},
                        {"Chocolate Chip Muffins (4 ct)", "Bakery", 4.49, 20, "Fresh baked chocolate chip muffins", "images/Chocolat chip muffons.jpg"},
                        {"Bagels (6 ct)", "Bakery", 3.99, 30, "New York style plain bagels", "images/Bagels.jpg"},
                        {"Everything Bagels (6 ct)", "Bakery", 4.29, 25, "Everything seasoned bagels", "images/Everything bagel.jpg"},
                        {"Croissants (4 ct)", "Bakery", 5.49, 20, "Buttery flaky croissants", "images/Croissant.jpg"},
                        {"Dinner Rolls (12 ct)", "Bakery", 3.99, 35, "Soft golden dinner rolls", ROLLS},
                        {"Cinnamon Raisin Bread (16 oz)", "Bakery", 4.29, 20, "Sweet cinnamon raisin swirl bread", "images/Cinnamon raisan bread.jpg"},
                        {"Pita Bread (6 ct)", "Bakery", 2.99, 25, "Soft pita pockets", "images/pita.jpg"},
                        {"Tortilla Wraps (10 ct)", "Bakery", 3.49, 40, "Large flour tortilla wraps", "images/Tortilla wraps.jpg"},
                        {"Corn Tortillas (30 ct)", "Bakery", 3.29, 35, "Authentic corn tortillas", "images/Corn tortilla.jpg"},
                        {"English Muffins (6 ct)", "Bakery", 3.49, 30, "Classic split English muffins", MUFFIN},

                        // Meat
                        {"Boneless Chicken Breast (2 lb)", "Meat", 8.99, 45, "Fresh boneless skinless chicken breast", "images/Boneless chicken breast.jpg"},
                        {"Chicken Thighs (2 lb)", "Meat", 6.49, 40, "Bone-in chicken thighs", "images/chicken thighs.jpg"},
                        {"Whole Chicken (4 lb)", "Meat", 9.99, 20, "Fresh whole fryer chicken", "images/Whole chicken.jpg"},
                        {"Ground Beef 80/20 (1 lb)", "Meat", 6.49, 50, "Fresh ground beef 80% lean", "images/ground beef.jpg"},
                        {"Ground Beef 93/7 (1 lb)", "Meat", 7.99, 35, "Lean ground beef 93% lean", "images/ground beef.jpg"},
                        {"Beef Sirloin Steak (1 lb)", "Meat", 14.99, 15, "USDA choice beef sirloin", STEAK},
                        {"Ribeye Steak (1 lb)", "Meat", 18.99, 10, "USDA choice bone-in ribeye", "images/ribeye.jpg"},
                        {"Bacon (16 oz)", "Meat", 7.99, 35, "Thick cut hickory smoked bacon", "images/bacon.jpg"},
                        {"Turkey Bacon (12 oz)", "Meat", 5.49, 30, "Lower sodium turkey bacon", "images/turkey bacon.jpg"},
                        {"Pork Chops (2 lb)", "Meat", 9.49, 20, "Bone-in center cut pork chops", "images/pork cho[s.jpg"},
                        {"Pork Tenderloin (1.5 lb)", "Meat", 10.99, 15, "Fresh pork tenderloin", "images/Pork tenderloin.jpg"},
                        {"Italian Sausage (19 oz)", "Meat", 6.99, 25, "Sweet Italian sausage links", "images/italian sausage.jpg"},
                        {"Hot Dogs (16 oz)", "Meat", 4.99, 40, "Classic beef hot dogs", "images/hotdogs.jpg"},
                        {"Deli Turkey (8 oz)", "Meat", 5.99, 30, "Sliced oven roasted turkey breast", "images/deli turkey.jpg"},
                        {"Deli Ham (8 oz)", "Meat", 5.49, 30, "Sliced honey ham", "images/deli ham.jpg"},
                        {"Lamb Chops (1 lb)", "Meat", 16.99, 10, "Fresh bone-in lamb chops", "images/lab chops.jpg"},

                        // Seafood
                        {"Atlantic Salmon (1 lb)", "Seafood", 12.99, 15, "Fresh Atlantic salmon fillet", SALMON},
                        {"Tilapia Fillet (1 lb)", "Seafood", 8.99, 20, "Fresh skinless tilapia fillet", "images/tiliapia filet.jpg"},
                        {"Cod Fillet (1 lb)", "Seafood", 10.99, 15, "Fresh Atlantic cod fillet", "images/cod filet.jpg"},
                        {"Shrimp (1 lb)", "Seafood", 11.49, 20, "Peeled and deveined medium shrimp", "images/shrimp.jpg"},
                        {"Jumbo Shrimp (1 lb)", "Seafood", 14.99, 15, "Peeled and deveined jumbo shrimp", "images/jumbo shrimp.jpg"},
                        {"Tuna Steak (1 lb)", "Seafood", 15.99, 10, "Fresh yellowfin tuna steak", "images/tuna steak.jpg"},
                        {"Crab Legs (1 lb)", "Seafood", 19.99, 8, "Snow crab legs", "images/crab legs.jpg"},
                        {"Lobster Tail (6 oz)", "Seafood", 24.99, 5, "Cold water lobster tail", "images/lobster tail.jpg"},
                        {"Scallops (1 lb)", "Seafood", 18.99, 10, "Fresh sea scallops", "images/Scallops.jpg"},
                        {"Clams (1 lb)", "Seafood", 9.99, 12, "Fresh littleneck clams", "images/clams.jpg"},

                        // Produce
                        {"Bananas (bunch)", "Produce", 1.29, 100, "Fresh yellow bananas", BANANA},
                        {"Gala Apples (3 lb bag)", "Produce", 4.99, 60, "Sweet crisp Gala apples", APPLE},
                        {"Granny Smith Apples (3 lb)", "Produce", 4.79, 50, "Tart green Granny Smith apples", "images/granny smith apples.jpg"},
                        {"Strawberries (1 lb)", "Produce", 3.99, 40, "Fresh ripe strawberries", BERRY},
                        {"Blueberries (6 oz)", "Produce", 3.49, 35, "Fresh sweet blueberries", "images/blue berries.jpg"},
                        {"Raspberries (6 oz)", "Produce", 3.99, 30, "Fresh red raspberries", "images/raspberries.jpg"},
                        {"Grapes (2 lb)", "Produce", 4.49, 40, "Seedless red grapes", GRAPE},
                        {"Watermelon (whole)", "Produce", 7.99, 15, "Sweet seedless watermelon", "images/watermelon.jpg"},
                        {"Oranges (4 lb bag)", "Produce", 5.49, 40, "Navel oranges", "images/oranges.jpg"},
                        {"Lemons (2 lb bag)", "Produce", 3.99, 35, "Fresh bright lemons", "images/lemons.jpg"},
                        {"Avocados (each)", "Produce", 1.49, 60, "Ripe Hass avocados", AVOCADO},
                        {"Broccoli (1 head)", "Produce", 1.99, 55, "Fresh green broccoli crown", BROCCOLI},
                        {"Cauliflower (1 head)", "Produce", 2.99, 30, "Fresh white cauliflower", "images/cauliflower.jpg"},
                        {"Russet Potatoes (5 lb)", "Produce", 3.49, 70, "Idaho russet baking potatoes", POTATO},
                        {"Sweet Potatoes (3 lb)", "Produce", 3.99, 50, "Fresh orange sweet potatoes", "images/sweet potatoes.jpg"},
                        {"Baby Spinach (5 oz)", "Produce", 3.29, 45, "Fresh pre-washed baby spinach", "images/baby spinach.jpg"},
                        {"Romaine Lettuce (3 ct)", "Produce", 4.49, 35, "Crisp romaine lettuce hearts", GREENS},
                        {"Iceberg Lettuce (1 head)", "Produce", 1.99, 40, "Fresh iceberg lettuce", "images/iceberg lettuce.jpg"},
                        {"Roma Tomatoes (1 lb)", "Produce", 1.99, 65, "Firm fresh Roma tomatoes", TOMATO},
                        {"Cherry Tomatoes (10 oz)", "Produce", 3.29, 40, "Sweet cherry tomatoes", "images/cherry tomatoes.jpg"},
                        {"Yellow Onions (3 lb)", "Produce", 2.49, 60, "Fresh yellow cooking onions", "images/yellow onion.jpg"},
                        {"Red Onions (2 lb)", "Produce", 2.29, 50, "Fresh red onions", "images/red onion.jpg"},
                        {"Garlic (3 ct)", "Produce", 1.49, 70, "Fresh garlic bulbs", "images/garlic.jpg"},
                        {"Bell Peppers (3 ct)", "Produce", 3.99, 45, "Mixed red yellow orange peppers", "images/bell peppers.jpg"},
                        {"Jalapeños (8 oz)", "Produce", 1.99, 40, "Fresh spicy jalapeño peppers", PEPPER},
                        {"Cucumbers (each)", "Produce", 1.29, 50, "Fresh English cucumbers", "images/cucumbers.jpg"},
                        {"Zucchini (each)", "Produce", 1.49, 40, "Fresh green zucchini", "images/zuchinni.jpg"},
                        {"Carrots (2 lb bag)", "Produce", 1.99, 60, "Fresh baby carrots", "images/carrots.jpg"},
                        {"Celery (bunch)", "Produce", 1.79, 45, "Fresh celery stalks", "images/celery.jpg"},
                        {"Mushrooms (8 oz)", "Produce", 2.99, 40, "Fresh sliced white mushrooms", MUSHROOM},
                        {"Corn (4 ct)", "Produce", 3.49, 35, "Fresh sweet corn on the cob", "images/corn.jpg"},
                        {"Asparagus (1 lb)", "Produce", 3.99, 25, "Fresh green asparagus", "images/asparagus.jpg"},
                        {"Kale (bunch)", "Produce", 2.49, 30, "Fresh curly kale", "images/kale.jpg"},
                        {"Limes (2 lb bag)", "Produce", 3.49, 40, "Fresh bright limes", "images/limes.jpg"},
                        {"Mango (each)", "Produce", 1.79, 35, "Fresh ripe Ataulfo mango", "images/mango.jpg"},

                        // Beverages
                        {"Orange Juice (52 oz)", "Beverages", 4.99, 40, "100% pure squeezed orange juice", JUICE},
                        {"Apple Juice (64 oz)", "Beverages", 3.99, 35, "100% pure apple juice", "images/apple juice.jpg"},
                        {"Coca-Cola (12 pack)", "Beverages", 7.99, 55, "Classic Coca-Cola cans", "images/coca cola 12 pack.jpg"},
                        {"Diet Coke (12 pack)", "Beverages", 7.99, 50, "Diet Coca-Cola cans", "images/diet coke.png"},
                        {"Pepsi (12 pack)", "Beverages", 7.49, 45, "Classic Pepsi cans", "images/pepsi.jpg"},
                        {"Sprite (12 pack)", "Beverages", 7.49, 40, "Lemon lime soda", "images/sprite.jpg"},
                        {"Spring Water (24 pack)", "Beverages", 4.49, 80, "Pure spring water bottles", "images/spring water.jpg"},
                        {"Sparkling Water (12 pack)", "Beverages", 5.99, 40, "Unflavored sparkling water", "images/sparkling.jpg"},
                        {"Lemonade (52 oz)", "Beverages", 3.49, 35, "Simply lemonade", "images/lemonade.jpg"},
                        {"Green Tea (18.5 oz)", "Beverages", 1.99, 50, "Unsweetened green tea", TEA},
                        {"Coffee Beans (12 oz)", "Beverages", 10.99, 25, "Medium roast whole bean coffee", "images/coffee beans.jpg"},
                        {"Instant Coffee (8 oz)", "Beverages", 7.99, 30, "Classic instant coffee", COFFEE},
                        {"Almond Breeze (32 oz)", "Beverages", 3.49, 30, "Unsweetened almond beverage", "images/almond breeze.jpg"},
                        {"Gatorade (32 oz)", "Beverages", 2.29, 50, "Fruit punch sports drink", "images/gatorade.jpg"},
                        {"Red Bull (8.4 oz)", "Beverages", 2.99, 40, "Original energy drink", "images/red bull.jpg"},

                        // Pantry
                        {"Pasta (16 oz)", "Pantry", 1.79, 90, "Barilla spaghetti pasta", "images/pasta.jpg"},
                        {"Penne Pasta (16 oz)", "Pantry", 1.79, 80, "Barilla penne pasta", "images/penne.jpg"},
                        {"Fusilli Pasta (16 oz)", "Pantry", 1.79, 70, "Barilla fusilli pasta", "images/fusili.jpg"},
                        {"Marinara Sauce (24 oz)", "Pantry", 3.49, 75, "Classic tomato marinara sauce", "images/marinara.jpg"},
                        {"Alfredo Sauce (15 oz)", "Pantry", 3.99, 50, "Creamy parmesan alfredo sauce", "images/alfredo.jpg"},
                        {"Peanut Butter (16 oz)", "Pantry", 3.99, 60, "Creamy peanut butter", "images/peanut butter.jpg"},
                        {"Almond Butter (12 oz)", "Pantry", 7.99, 25, "Natural almond butter", "images/almond butter.jpg"},
                        {"Strawberry Jam (18 oz)", "Pantry", 3.49, 45, "Strawberry jam", "images/strawberry jam.jpg"},
                        {"Honey (12 oz)", "Pantry", 6.99, 30, "Pure natural honey", "images/honey.jpg"},
                        {"Maple Syrup (12 oz)", "Pantry", 8.99, 20, "Pure Vermont maple syrup", "images/maple syrup.jpg"},
                        {"Cheerios (18 oz)", "Pantry", 4.99, 50, "Original whole grain Cheerios cereal", "images/cheerios.jpg"},
                        {"Frosted Flakes (13.5 oz)", "Pantry", 4.49, 45, "Kellogg's Frosted Flakes", "images/frosted flakes.jpg"},
                        {"Oatmeal (42 oz)", "Pantry", 5.49, 40, "Old fashioned rolled oats", "images/oatmeal.jpg"},
                        {"Granola (12 oz)", "Pantry", 5.99, 30, "Honey oat granola", "images/granola.jpg"},
                        {"White Rice (5 lb)", "Pantry", 5.99, 55, "Long grain white rice", "images/white rice.jpg"},
                        {"Brown Rice (5 lb)", "Pantry", 6.49, 40, "Whole grain brown rice", "images/brown rice.jpg"},
                        {"Chicken Broth (32 oz)", "Pantry", 2.99, 50, "Low sodium chicken broth", "images/chicken broth.jpg"},
                        {"Vegetable Broth (32 oz)", "Pantry", 2.99, 40, "Organic vegetable broth", "images/vegetable broth.jpg"},
                        {"Diced Tomatoes (14.5 oz)", "Pantry", 1.49, 70, "Hunt's diced tomatoes", "images/diced tomatoes.jpg"},
                        {"Black Beans (15 oz)", "Pantry", 1.29, 65, "Low sodium black beans", "images/black beans.jpg"},
                        {"Chickpeas (15 oz)", "Pantry", 1.39, 60, "Garbanzo beans", "images/chickpeas.jpg"},
                        {"Lentils (16 oz)", "Pantry", 2.49, 40, "Dried green lentils", "images/lentils.jpg"},
                        {"Olive Oil (16.9 oz)", "Pantry", 8.99, 35, "Extra virgin olive oil", "images/olive oil.jpg"},
                        {"Vegetable Oil (48 oz)", "Pantry", 5.99, 40, "Pure vegetable oil", "images/vegetable oil.jpg"},
                        {"All Purpose Flour (5 lb)", "Pantry", 4.29, 45, "Gold Medal all purpose flour", "images/all purpose flour.jpg"},
                        {"Sugar (4 lb)", "Pantry", 3.99, 50, "Pure cane granulated sugar", "images/sugar.jpg"},
                        {"Brown Sugar (2 lb)", "Pantry", 2.99, 40, "Light brown sugar", "images/brown sugar.jpg"},
                        {"Baking Powder (8.1 oz)", "Pantry", 2.49, 35, "Double acting baking powder", "images/baking powder.jpg"},
                        {"Baking Soda (16 oz)", "Pantry", 1.49, 45, "Pure baking soda", "images/baking soda.jpg"},
                        {"Salt (26 oz)", "Pantry", 1.29, 60, "Iodized table salt", "images/salt.jpg"},
                        {"Black Pepper (3 oz)", "Pantry", 3.99, 40, "Ground black pepper", "images/black pepper.jpg"},
                        {"Garlic Powder (3.12 oz)", "Pantry", 2.99, 35, "Pure garlic powder", "images/garlic powder.jpg"},
                        {"Italian Seasoning (0.75 oz)", "Pantry", 2.49, 40, "Blend of Italian herbs", "images/italian seasoning.jpg"},
                        {"Ketchup (32 oz)", "Pantry", 3.49, 55, "Heinz tomato ketchup", "images/ketchup.jpg"},
                        {"Mustard (20 oz)", "Pantry", 2.49, 45, "French's yellow mustard", "images/mustard.jpg"},
                        {"Mayonnaise (30 oz)", "Pantry", 5.49, 35, "Hellmann's real mayonnaise", "images/mayo.jpg"},
                        {"Soy Sauce (10 oz)", "Pantry", 2.99, 40, "Kikkoman soy sauce", "images/soy.jpg"},
                        {"Hot Sauce (5 oz)", "Pantry", 2.49, 40, "Frank's RedHot original", "images/hot sauce.jpg"},
                        {"Salsa (16 oz)", "Pantry", 3.99, 35, "Pace chunky salsa medium", "images/salsa.jpg"},
                        {"Tortilla Chips (13 oz)", "Pantry", 4.49, 45, "Tostitos original tortilla chips", "images/tortilla chips.jpg"},
                        {"Potato Chips (8 oz)", "Pantry", 4.29, 50, "Lay's classic potato chips", "images/potato chips.jpg"},
                        {"Crackers (13.8 oz)", "Pantry", 3.99, 40, "Ritz original crackers", "images/crackers.jpg"},
                        {"Popcorn (3 pack)", "Pantry", 3.49, 45, "Orville Redenbacher butter popcorn", "images/popcorn.jpg"},
                        {"Dark Chocolate (3.5 oz)", "Pantry", 3.49, 30, "72% dark chocolate bar", "images/dark chocolate.jpg"},
                        {"Nutella (13 oz)", "Pantry", 4.99, 30, "Hazelnut chocolate spread", "images/nutella.jpg"},
                        {"Vanilla Extract (2 oz)", "Pantry", 4.99, 25, "Pure vanilla extract", "images/vanilla extract.jpg"},
                        {"Cocoa Powder (8 oz)", "Pantry", 4.49, 25, "Unsweetened cocoa powder", "images/cocoa powder.jpg"},
                        {"Protein Bar (each)", "Pantry", 2.99, 60, "Chocolate chip protein bar", "images/protein b ar.jpg"},
                        {"Tuna Can (5 oz)", "Pantry", 1.79, 55, "Chunk light tuna in water", "images/tuna can.jpg"},

                        // Frozen
                        {"Frozen Pizza (12 inch)", "Frozen", 6.99, 30, "DiGiorno pepperoni frozen pizza", "images/frozen pizza.jpg"},
                        {"Cheese Frozen Pizza (12 inch)", "Frozen", 5.99, 25, "DiGiorno four cheese frozen pizza", "images/frozen cheese.jpg"},
                        {"Frozen Chicken Nuggets (32 oz)", "Frozen", 8.99, 30, "Tyson chicken nuggets", "images/frozen chicken nuggets.jpg"},
                        {"Frozen French Fries (32 oz)", "Frozen", 4.49, 40, "Ore-Ida golden french fries", FRIES},
                        {"Frozen Waffles (10 ct)", "Frozen", 3.99, 35, "Eggo homestyle waffles", "images/waffles.jpg"},
                        {"Frozen Burritos (8 ct)", "Frozen", 7.99, 25, "Bean and cheese burritos", "images/frozen burrtiops.jpg"},
                        {"Frozen Vegetables Mix (12 oz)", "Frozen", 2.99, 45, "Steam fresh mixed vegetables", "images/frozen vegetables mix.jpg"},
                        {"Frozen Peas (16 oz)", "Frozen", 2.49, 40, "Birds Eye sweet peas", "images/frozen peas.jpg"},
                        {"Frozen Corn (16 oz)", "Frozen", 2.49, 40, "Birds Eye sweet corn", "images/frozen corn.jpg"},
                        {"Frozen Edamame (12 oz)", "Frozen", 3.49, 30, "Shelled edamame", "images/frozen edamame.jpg"},
                        {"Ice Cream (48 oz)", "Frozen", 5.99, 25, "Breyers vanilla ice cream", "images/icecream.jpg"},
                        {"Chocolate Ice Cream (48 oz)", "Frozen", 5.99, 25, "Breyers chocolate ice cream", "images/chocolate ice cream.jpg"},
                        {"Frozen Fish Sticks (24.6 oz)", "Frozen", 6.99, 20, "Gorton's crispy fish sticks", "images/frozen fish sticks.jpg"},
                        {"Frozen Shrimp (12 oz)", "Frozen", 8.99, 20, "SeaPak popcorn shrimp", "images/frozen shrimp.jpg"},
                        {"Frozen Lasagna (38 oz)", "Frozen", 9.99, 15, "Stouffer's lasagna with meat sauce", "images/frozen lasagna.jpg"},
                };

                for (Object[] p : products) {
                    Product product = new Product();
                    product.setName((String) p[0]);
                    product.setCategory((String) p[1]);
                    product.setPrice((Double) p[2]);
                    product.setQuantity((Integer) p[3]);
                    product.setDescription((String) p[4]);
                    if (p.length > 5) product.setImagePath((String) p[5]);
                    productRepository.save(product);
                }
            }
        };
    }

    private User createUser(String email, String password, String fullName) {
        User u = new User();
        u.setEmail(email);
        u.setPassword(password);
        u.setFullName(fullName);
        u.setRole("CUSTOMER");
        u.setEnabled(true);
        u.setBalance(100.0);
        return u;
    }
}