package com.example.money.repository;

import com.example.money.dto.MonthlyExpenseSummary;
import com.example.money.entity.Transaction;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;

public interface TransactionRepository extends JpaRepository<Transaction, Long> {

    /*New*/
    @Query("""
                SELECT t FROM Transaction t
                JOIN FETCH t.account
                LEFT JOIN FETCH t.destination
                LEFT JOIN FETCH t.budget
                WHERE (t.account.id = :accountId OR t.destination.id = :accountId)
                  AND t.userId = :userId
                  AND EXTRACT(MONTH FROM t.date) = :month
                  AND EXTRACT(YEAR FROM t.date) = :year
                  AND t.isDeleted = false
            """)
    List<Transaction> findByAccountIdOrDestinationIdAndUserIdAndMonthAndYearAndIsDeletedFalse(
            @Param("accountId") Long accountId,
            @Param("userId") String userId,
            @Param("month") int month,
            @Param("year") int year
    );

    /*Old*/

    List<Transaction> findByUserIdAndIsDeletedFalse(String userId);

    @Query("""
                SELECT t FROM Transaction t
                WHERE t.budget.id = :budgetId
                  AND t.isDeleted = false
                  AND (
                    COALESCE(t.referenceDate, t.date) BETWEEN :startDate AND :endDate
                    OR
                    (
                      t.spreadStart IS NOT NULL AND t.spreadEnd IS NOT NULL
                      AND t.spreadStart <= :endDate
                      AND t.spreadEnd >= :startDate
                    )
                  )
                ORDER BY t.date DESC
            """)
    List<Transaction> findByBudgetIdAndDateRangeWithSpecialCases(
            @Param("budgetId") Long budgetId,
            @Param("startDate") LocalDate startDate,
            @Param("endDate") LocalDate endDate
    );

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

    @Query("""
            SELECT t FROM Transaction t
            LEFT JOIN FETCH t.budget
            WHERE t.budget.id = :budgetId
              AND t.userId = :userId
              AND t.isDeleted = false
              AND (
                (
                  EXTRACT(MONTH FROM COALESCE(t.referenceDate, t.date)) = :month
                  AND EXTRACT(YEAR FROM COALESCE(t.referenceDate, t.date)) = :year
                )
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
    List<Transaction> findByBudgetIdAndUserIdAndMonthAndYearAndIsDeletedFalseWithBudget(
            @Param("budgetId") Long budgetId,
            @Param("userId") String userId,
            @Param("month") int month,
            @Param("year") int year
    );

    @Query("""
            SELECT COALESCE(SUM(t.amount), 0)
            FROM Transaction t
            WHERE t.userId = :userId
              AND t.isDeleted = false
              AND t.budget IS NOT NULL
              AND EXTRACT(MONTH FROM COALESCE(t.referenceDate, t.date)) = :month
              AND EXTRACT(YEAR FROM COALESCE(t.referenceDate, t.date)) = :year
            """)
    BigDecimal calculateBudgetedAmountByMonthAndYear(
            @Param("userId") String userId,
            @Param("month") int month,
            @Param("year") int year
    );

    @Query("""
            SELECT COALESCE(SUM(t.amount), 0)
            FROM Transaction t
            WHERE t.userId = :userId
              AND t.isDeleted = false
              AND t.isPaid = true
            """)
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
            ORDER BY t.date DESC
            """)
    Page<Transaction> searchByDescriptionOrBudgetName(
            @Param("userId") String userId,
            @Param("query") String query,
            Pageable pageable
    );

    @Query(value = """
            SELECT TO_CHAR(t.date, 'YYYY-MM') AS time, ABS(SUM(t.amount)) AS total
            FROM transaction t
            WHERE t.amount < 0 AND t.user_id = :userId
            GROUP BY TO_CHAR(t.date, 'YYYY-MM')
            ORDER BY TO_CHAR(t.date, 'YYYY-MM') ASC
            """, nativeQuery = true)
    List<MonthlyExpenseSummary> findMonthlyExpenseSummary(@Param("userId") String userId);
}