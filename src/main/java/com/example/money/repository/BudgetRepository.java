package com.example.money.repository;

import com.example.money.entity.Budget;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

public interface BudgetRepository extends JpaRepository<Budget, Long> {
    Optional<Budget> findByIdAndUserIdAndIsDeletedFalse(Long id, String userId);

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
    List<Budget> findBudgetsByUserIdWithinDateRangeSortOrderAsc(
            @Param("userId") String userId,
            @Param("start") LocalDate start,
            @Param("end") LocalDate end
    );

}
