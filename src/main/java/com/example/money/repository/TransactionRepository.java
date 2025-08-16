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
import java.time.OffsetDateTime;
import java.util.List;

public interface TransactionRepository extends JpaRepository<Transaction, Long> {

    //    find transactions of an account in a given month (based on spread -> date)
    @Query("""
                SELECT t FROM Transaction t
                JOIN FETCH t.account
                LEFT JOIN FETCH t.destination
                LEFT JOIN FETCH t.budget
                WHERE (t.account.id = :accountId OR t.destination.id = :accountId)
                  AND t.userId = :userId
                  AND t.isDeleted = false
                  AND (
                      (t.spreadStart IS NOT NULL AND t.spreadEnd IS NOT NULL
                          AND t.spreadStart <= :endOfMonthDate
                          AND t.spreadEnd >= :startOfMonthDate
                      )
                      OR
                      (
                          (t.spreadStart IS NULL OR t.spreadEnd IS NULL)
                          AND t.date BETWEEN :startOfMonthDateTime AND :endOfMonthDateTime
                      )
                  )
            """)
    List<Transaction> findByAccountIdOrDestinationIdAndUserIdAndDateRange(
            @Param("accountId") Long accountId,
            @Param("userId") String userId,
            @Param("startOfMonthDate") LocalDate startOfMonthDate,
            @Param("endOfMonthDate") LocalDate endOfMonthDate,
            @Param("startOfMonthDateTime") OffsetDateTime startOfMonthDateTime,
            @Param("endOfMonthDateTime") OffsetDateTime endOfMonthDateTime
    );

    // find transactions (+budget, +accounts) of a budget for a given month (based on spread -> reference -> date)
    @Query("""
                SELECT t FROM Transaction t
                JOIN FETCH t.account
                LEFT JOIN FETCH t.destination
                LEFT JOIN FETCH t.budget
                WHERE t.budget.id = :budgetId
                  AND t.userId = :userId
                  AND t.isDeleted = false
                  AND (
                    (t.referenceDate IS NOT NULL AND t.referenceDate BETWEEN :startDate AND :endDate)
                    OR (
                      t.referenceDate IS NULL
                      AND t.spreadStart IS NOT NULL AND t.spreadEnd IS NOT NULL
                      AND t.spreadStart <= :endDate
                      AND t.spreadEnd >= :startDate
                    )
                    OR (
                      t.referenceDate IS NULL
                      AND (t.spreadStart IS NULL OR t.spreadEnd IS NULL)
                      AND t.date BETWEEN :startDateTime AND :endDateTime
                    )
                  )
            """)
    List<Transaction> findByBudgetIdAndUserIdAndDateRange(
            @Param("budgetId") Long budgetId,
            @Param("userId") String userId,
            @Param("startDate") LocalDate startDate,
            @Param("endDate") LocalDate endDate,
            @Param("startDateTime") OffsetDateTime startDateTime,
            @Param("endDateTime") OffsetDateTime endDateTime
    );


    //    find transactions of an account from the beginning up to a given month (based on spread -> date)
    @Query("""
                SELECT t FROM Transaction t
                JOIN FETCH t.account
                LEFT JOIN FETCH t.destination
                LEFT JOIN FETCH t.budget
                WHERE (t.account.id = :accountId OR t.destination.id = :accountId)
                  AND t.userId = :userId
                  AND t.isPaid = true
                  AND t.isDeleted = false
                  AND (
                      (t.spreadStart IS NOT NULL AND t.spreadStart <= :endOfMonthDate)
                      OR
                      (t.spreadStart IS NULL AND t.date <= :endOfMonthDateTime)
                  )
            """)
    List<Transaction> findUpToMonthAndYearPaidTransactions(
            @Param("accountId") Long accountId,
            @Param("userId") String userId,
            @Param("endOfMonthDateTime") OffsetDateTime endOfMonthDateTime,
            @Param("endOfMonthDate") LocalDate endOfMonthDate
    );

    //    search by description or budget name
    @Query("""
            SELECT t FROM Transaction t
            LEFT JOIN FETCH t.budget
            LEFT JOIN FETCH t.account
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

    // sum of paid transactions
    @Query("""
                SELECT COALESCE(SUM(t.amount), 0)
                FROM Transaction t
                JOIN t.account a
                WHERE t.userId = :userId
                  AND t.isDeleted = false
                  AND t.isPaid = true
                  AND t.destination IS NULL
                  AND a.isDeleted = false
            """)
    BigDecimal calculateBalance(@Param("userId") String userId);

    // sum of paid budgeted transactions in a given period (month)
    @Query("""
                SELECT COALESCE(SUM(t.amount), 0)
                FROM Transaction t
                WHERE t.userId = :userId
                  AND t.isDeleted = false
                  AND t.isPaid = true
                  AND t.budget IS NOT NULL
                  AND (
                    (t.referenceDate IS NOT NULL AND t.referenceDate BETWEEN :startDate AND :endDate)
                    OR (
                      t.referenceDate IS NULL
                      AND t.spreadStart IS NOT NULL AND t.spreadEnd IS NOT NULL
                      AND t.spreadStart <= :endDate
                      AND t.spreadEnd >= :startDate
                    )
                    OR (
                      t.referenceDate IS NULL
                      AND (t.spreadStart IS NULL OR t.spreadEnd IS NULL)
                      AND t.date BETWEEN :startDateTime AND :endDateTime
                    )
                  )
            """)
    BigDecimal calculateBudgetedAmountInDateRange(
            @Param("userId") String userId,
            @Param("startDate") LocalDate startDate,
            @Param("endDate") LocalDate endDate,
            @Param("startDateTime") OffsetDateTime startDateTime,
            @Param("endDateTime") OffsetDateTime endDateTime
    );

    // find transactions of a budget in a given period based on (referenceDate -> spread -> date)
    @Query("""
                SELECT t FROM Transaction t
                WHERE t.budget.id = :budgetId
                  AND t.isDeleted = false
                  AND (
                    (t.referenceDate IS NOT NULL AND t.referenceDate BETWEEN :startDate AND :endDate)
                    OR (
                      t.referenceDate IS NULL
                      AND t.spreadStart IS NOT NULL AND t.spreadEnd IS NOT NULL
                      AND t.spreadStart <= :endDate
                      AND t.spreadEnd >= :startDate
                    )
                    OR (
                      t.referenceDate IS NULL
                      AND (t.spreadStart IS NULL OR t.spreadEnd IS NULL)
                      AND t.date BETWEEN :startDateTime AND :endDateTime
                    )
                  )
            """)
    List<Transaction> findByBudgetIdAndDateInRange(
            @Param("budgetId") Long budgetId,
            @Param("startDate") LocalDate startDate,
            @Param("endDate") LocalDate endDate,
            @Param("startDateTime") OffsetDateTime startDateTime,
            @Param("endDateTime") OffsetDateTime endDateTime
    );

    /*Old*/

    @Query(value = """
            SELECT TO_CHAR(t.date, 'YYYY-MM') AS time, ABS(SUM(t.amount)) AS total
            FROM transaction t
            WHERE t.amount < 0 AND t.user_id = :userId
            GROUP BY TO_CHAR(t.date, 'YYYY-MM')
            ORDER BY TO_CHAR(t.date, 'YYYY-MM') ASC
            """, nativeQuery = true)
    List<MonthlyExpenseSummary> findMonthlyExpenseSummary(@Param("userId") String userId);
}