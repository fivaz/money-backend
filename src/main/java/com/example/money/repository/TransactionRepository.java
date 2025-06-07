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
        (EXTRACT(YEAR FROM t.date) = :year AND EXTRACT(MONTH FROM t.date) = :month)
        OR (
          t.spreadStart IS NOT NULL AND t.spreadEnd IS NOT NULL
          AND (
            EXTRACT(YEAR FROM t.spreadStart) < :year OR
            (EXTRACT(YEAR FROM t.spreadStart) = :year AND EXTRACT(MONTH FROM t.spreadStart) <= :month)
          )
          AND (
            EXTRACT(YEAR FROM t.spreadEnd) > :year OR
            (EXTRACT(YEAR FROM t.spreadEnd) = :year AND EXTRACT(MONTH FROM t.spreadEnd) >= :month)
          )
        )
      )
    ORDER BY t.date DESC
""")
    List<Transaction> findByUserIdAndMonthAndYearAndIsDeletedFalseWithBudgetOrderByDateDesc(
            @Param("userId") String userId,
            @Param("month") int month,
            @Param("year") int year
    );

    @Query(value = """
    SELECT t.* FROM transaction t
    LEFT JOIN budget b ON b.id = t.budget_id
    WHERE b.id = :budgetId
      AND t.user_id = :userId
      AND t.is_deleted = false
      AND (
        (
          EXTRACT(MONTH FROM COALESCE(t.reference_date, t.date)) = :month
          AND EXTRACT(YEAR FROM COALESCE(t.reference_date, t.date)) = :year
        )
        OR (
          t.spread_start IS NOT NULL AND t.spread_end IS NOT NULL
          AND (
            (
              EXTRACT(YEAR FROM t.spread_start) < :year OR
              (EXTRACT(YEAR FROM t.spread_start) = :year AND EXTRACT(MONTH FROM t.spread_start) <= :month)
            )
            AND
            (
              EXTRACT(YEAR FROM t.spread_end) > :year OR
              (EXTRACT(YEAR FROM t.spread_end) = :year AND EXTRACT(MONTH FROM t.spread_end) >= :month)
            )
          )
        )
      )
    ORDER BY t.date DESC
    """, nativeQuery = true)
    List<Transaction> findByBudgetIdAndUserIdAndMonthAndYearAndIsDeletedFalseWithBudget(
            @Param("budgetId") Long budgetId,
            @Param("userId") String userId,
            @Param("month") int month,
            @Param("year") int year
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


    @Query("""
    SELECT t FROM Transaction t
    LEFT JOIN FETCH t.budget
    WHERE t.userId = :userId
        AND t.isDeleted = false
        AND (
          :query IS NULL OR
          :query = '' OR
          LOWER(t.description) LIKE LOWER(CONCAT('%', :query, '%')) OR
          (t.budget IS NOT NULL AND LOWER(t.budget.name) LIKE LOWER(CONCAT('%', :query, '%')))
        )
    """)
    List<Transaction> searchByDescriptionOrBudgetName(
            @Param("userId") String userId,
            @Param("query") String query);
}