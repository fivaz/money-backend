package com.example.money.repository;

import com.example.money.entity.Transaction;
import org.springframework.data.jpa.repository.JpaRepository;

import java.time.LocalDateTime;
import java.util.List;

public interface TransactionRepository extends JpaRepository<Transaction, Long> {
    List<Transaction> findByUserId(String userId);

    List<Transaction> findByUserIdAndIsDeletedFalse(String userId);

    List<Transaction> findByUserIdAndDateBetweenAndIsDeletedFalse(
            String userId,
            LocalDateTime start,
            LocalDateTime end
    );
}