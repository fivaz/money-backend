package com.example.money.repository;

import com.example.money.entity.Transaction;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.time.LocalDate;
import java.util.List;

public interface TransactionRepository extends JpaRepository<Transaction, Long> {
    List<Transaction> findByUserIdAndIsDeletedFalse(String userId);

    //fetch transactions if date match month and year of monthStart, or if month and year of spreadStart and spreadEnd fits between the month and year of monthStart
    @Query("""
    SELECT t FROM Transaction t
    LEFT JOIN FETCH t.budget
    WHERE t.userId = :userId
      AND t.isDeleted = false
      AND (
        EXTRACT(YEAR FROM CAST(t.date AS TIMESTAMP)) = EXTRACT(YEAR FROM CAST(:monthStart AS DATE))
        AND EXTRACT(MONTH FROM CAST(t.date AS TIMESTAMP)) = EXTRACT(MONTH FROM CAST(:monthStart AS DATE))
        OR (
          t.spreadStart IS NOT NULL
          AND t.spreadEnd IS NOT NULL
          AND (
            EXTRACT(YEAR FROM CAST(t.spreadStart AS DATE)) < EXTRACT(YEAR FROM CAST(:monthStart AS DATE))
            OR (
              EXTRACT(YEAR FROM CAST(t.spreadStart AS DATE)) = EXTRACT(YEAR FROM CAST(:monthStart AS DATE))
              AND EXTRACT(MONTH FROM CAST(t.spreadStart AS DATE)) <= EXTRACT(MONTH FROM CAST(:monthStart AS DATE))
            )
          )
          AND (
            EXTRACT(YEAR FROM CAST(t.spreadEnd AS DATE)) > EXTRACT(YEAR FROM CAST(:monthStart AS DATE))
            OR (
              EXTRACT(YEAR FROM CAST(t.spreadEnd AS DATE)) = EXTRACT(YEAR FROM CAST(:monthStart AS DATE))
              AND EXTRACT(MONTH FROM CAST(t.spreadEnd AS DATE)) >= EXTRACT(MONTH FROM CAST(:monthStart AS DATE))
            )
          )
        )
      )
    ORDER BY t.date DESC
""")
    List<Transaction> findByUserIdAndMonthOrSpreadAndIsDeletedFalseOrderByDateDescWithBudget(
            @Param("userId") String userId,
            @Param("monthStart") LocalDate monthStart
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