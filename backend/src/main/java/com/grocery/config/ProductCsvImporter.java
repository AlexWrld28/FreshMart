package com.grocery.config;

import com.grocery.model.Product;
import com.grocery.repository.ProductRepository;
import com.opencsv.CSVReader;
import com.opencsv.CSVReaderBuilder;
import org.springframework.boot.CommandLineRunner;
import org.springframework.stereotype.Component;

import java.io.InputStreamReader;
import java.io.Reader;
import java.util.concurrent.ThreadLocalRandom;

@Component
public class ProductCsvImporter implements CommandLineRunner {

    private final ProductRepository productRepository;

    public ProductCsvImporter(final ProductRepository productRepository) {
        this.productRepository = productRepository;
    }

    @Override
    public void run(String... args) throws Exception {
        //if (productRepository.count() > 0) {
        //    return; //Prevent duplicate imports every time app starts
        //}

        productRepository.deleteAll();

        try (Reader reader = new InputStreamReader(
                getClass().getResourceAsStream("/BigBasket.csv"))) {

            CSVReader csvReader = new CSVReaderBuilder(reader)
                    .withSkipLines(1)
                    .build();

            String[] line;

            while ((line = csvReader.readNext()) != null) {

                String productName = line[0];
                if (productRepository.existsByName(productName)) {
                    continue;
                }

                Product product = new Product();

                product.setName(line[0]); //Product Name
                product.setPrice(generateUsdPrice(line[6])); //USD Price
                product.setImagePath(line[4]); //Image Url
                int stockQuantity = ThreadLocalRandom.current().nextInt(25, 101);
                product.setQuantity(stockQuantity);
                product.setDescription(line[5]); //CSV Quantity like "1 kg" or "500 g"
                product.setCategory(line[6]);

                System.out.println("Imported: " + product.getName());
                productRepository.save(product);

            }
        }
    }

    private double generateUsdPrice(String category) {

        double min;
        double max;

        switch (category.toLowerCase()) {

            case "fruits & vegetables":
                min = 0.99;
                max = 6.99;
                break;

            case "bakery, cakes & dairy":
                min = 2.49;
                max = 8.99;
                break;

            case "beverages":
                min = 1.49;
                max = 14.99;
                break;

            case "snacks & branded foods":
                min = 2.49;
                max = 9.99;
                break;

            case "beauty & hygiene":
                min = 3.99;
                max = 19.99;
                break;

            case "cleaning & household":
                min = 3.99;
                max = 24.99;
                break;

            default:
                min = 1.99;
                max = 14.99;
                break;
        }

        double price = ThreadLocalRandom.current().nextDouble(min, max);
        return Math.round(price * 100.0) / 100.0;
    }
}
