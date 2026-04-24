package com.grocery.repository;

import com.grocery.model.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;

public interface UserRepository extends JpaRepository<User, Long> {
    Optional<User> findByEmail(String email);
    List<User> findByRole(String role);

    @Modifying(clearAutomatically = true, flushAutomatically = true)
    @Transactional
    @Query("""
            update User u
            set u.balance = u.balance - :amount
            where u.id = :userId and u.balance >= :amount
            """)
    int deductBalanceIfSufficient(@Param("userId") Long userId, @Param("amount") double amount);

    @Modifying(clearAutomatically = true, flushAutomatically = true)
    @Transactional
    @Query("""
            update User u
            set u.balance = u.balance + :amount
            where u.id = :userId
            """)
    int incrementBalance(@Param("userId") Long userId, @Param("amount") double amount);
}
