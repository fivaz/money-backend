package com.example.money.repository;

import com.example.money.entity.Transaction;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.time.LocalDateTime;
import java.util.List;

public interface TransactionRepository extends JpaRepository<Transaction, Long> {
    List<Transaction> findByUserIdAndIsDeletedFalse(String userId);

    @Query("SELECT t FROM Transaction t LEFT JOIN FETCH t.budget WHERE t.userId = :userId AND t.date BETWEEN :start AND :end AND t.isDeleted = false ORDER BY t.date DESC")
    List<Transaction> findByUserIdAndDateBetweenAndIsDeletedFalseOrderByDateDescWithBudget(
            @Param("userId") String userId,
            @Param("start") LocalDateTime start,
            @Param("end") LocalDateTime end
    );
}