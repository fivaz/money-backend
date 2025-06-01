package com.example.money.repository;

import com.example.money.entity.Budget;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.time.LocalDateTime;
import java.util.List;

public interface BudgetRepository extends JpaRepository<Budget, Long> {
    List<Budget> findByUserIdAndIsDeletedFalseOrderBySortOrderAsc(String userId);

    @Query("SELECT DISTINCT b FROM Budget b LEFT JOIN FETCH b.transactions t " +
            "WHERE b.userId = :userId AND t.date BETWEEN :start AND :end AND t.isDeleted = false")
    List<Budget> findWithTransactionsForMonth(
            @Param("userId") String userId,
            @Param("start") LocalDateTime start,
            @Param("end") LocalDateTime end
    );
}