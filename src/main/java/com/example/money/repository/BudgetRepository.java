package com.example.money.repository;

import com.example.money.entity.Budget;
import org.springframework.data.jpa.repository.JpaRepository;

import java.time.LocalDateTime;
import java.util.List;

public interface BudgetRepository extends JpaRepository<Budget, Long> {
    List<Budget> findByUserIdAndIsDeletedFalseOrderBySortOrderAsc(String userId);
}