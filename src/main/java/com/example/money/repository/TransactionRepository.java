package com.example.money.repository;

import com.example.money.entity.Transaction;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.math.BigDecimal;
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

    @Query("""
    SELECT t FROM Transaction t
    LEFT JOIN FETCH t.budget
    WHERE t.budget.id = :budgetId
      AND t.userId = :userId
      AND (
        CASE
            WHEN t.referenceDate IS NOT NULL
            THEN CAST(t.referenceDate AS LocalDateTime)
            ELSE t.date
        END
      ) BETWEEN :start AND :end
      AND t.isDeleted = false
    ORDER BY t.date DESC
""")
    List<Transaction> findByBudgetIdAndUserIdAndDateBetweenAndIsDeletedFalseOrderByDateDescWithBudget(
            @Param("budgetId") Long budgetId,
            @Param("userId") String userId,
            @Param("start") LocalDateTime start,
            @Param("end") LocalDateTime end
    );

    @Query(value = """
    SELECT COALESCE(SUM(t.amount), 0)
    FROM transaction t
    WHERE t.user_id = :userId
      AND t.is_deleted = false
      AND t.budget_id IS NOT NULL
      AND (
            CASE
                WHEN t.reference_date IS NOT NULL THEN t.reference_date::timestamp
                ELSE t.date
            END
          ) BETWEEN :start AND :end
    """, nativeQuery = true)
    BigDecimal calculateBudgetedAmountBetween(
            @Param("userId") String userId,
            @Param("start") LocalDateTime start,
            @Param("end") LocalDateTime end
    );

    @Query(value = """
    SELECT COALESCE(SUM(t.amount), 0)
    FROM transaction t
    WHERE t.user_id = :userId
      AND t.is_deleted = false
      AND t.is_paid = true
    """, nativeQuery = true)
    BigDecimal calculateBalance(@Param("userId") String userId);
}