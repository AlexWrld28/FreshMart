package com.grocery.repository;

import com.grocery.model.Product;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

public interface ProductRepository extends JpaRepository<Product, Long> {
    List<Product> findByCategory(String category);
    List<Product> findByNameContainingIgnoreCase(String name);

    @Modifying(clearAutomatically = true, flushAutomatically = true)
    @Transactional
    @Query("""
            update Product p
            set p.quantity = p.quantity - :quantity
            where p.id = :productId and p.quantity >= :quantity
            """)
    int decrementQuantityIfAvailable(@Param("productId") Long productId, @Param("quantity") int quantity);
}
