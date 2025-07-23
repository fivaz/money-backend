package com.example.money.repository;

import com.example.money.entity.Budget;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;

public interface BudgetRepository extends JpaRepository<Budget, Long> {
    List<Budget> findByUserIdAndIsDeletedFalseOrderBySortOrderAsc(String userId);

    @Query("""
            SELECT b FROM Budget b
            WHERE b.userId = :userId
              AND b.isDeleted = false
              AND (
                (b.startAt IS NULL OR b.startAt <= :end)
                AND (b.endAt IS NULL OR b.endAt >= :start)
              )
            ORDER BY b.sortOrder ASC
            """)
    List<Budget> findBudgetsWithinDateRange(
            @Param("userId") String userId,
            @Param("start") LocalDate start,
            @Param("end") LocalDate end
    );

}
