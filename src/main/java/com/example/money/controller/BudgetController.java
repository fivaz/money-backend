package com.example.money.controller;

import com.example.money.dto.BudgetDTO;
import com.example.money.entity.Budget;
import com.example.money.entity.Transaction;
import com.example.money.repository.BudgetRepository;
import com.example.money.repository.TransactionRepository;
import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.YearMonth;
import java.util.*;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/budgets")
@RequiredArgsConstructor
public class BudgetController {

    private final BudgetRepository budgetRepository;

    private final TransactionRepository transactionRepository;

    @GetMapping
    public ResponseEntity<List<Budget>> getAll(HttpServletRequest request) {
        String userId = (String) request.getAttribute("firebaseUid");
        List<Budget> budgets = budgetRepository.findByUserIdAndIsDeletedFalseOrderBySortOrderAsc(userId);

        return ResponseEntity.ok(budgets);
    }

    @GetMapping("/by-date")
    public ResponseEntity<List<BudgetDTO>> getByDate(
            @RequestParam int year,
            @RequestParam int month,
            HttpServletRequest request) {

        String userId = (String) request.getAttribute("firebaseUid");
        YearMonth targetMonth = YearMonth.of(year, month);
        LocalDate firstDay = targetMonth.atDay(1);
        LocalDate lastDay = targetMonth.atEndOfMonth();

        List<Budget> budgets = budgetRepository.findBudgetsWithinDateRange(userId, firstDay, lastDay);

        if (budgets.isEmpty()) return ResponseEntity.ok(List.of());

        List<Long> budgetIds = budgets.stream().map(Budget::getId).toList();
        YearMonth previousMonth = targetMonth.minusMonths(1);
        LocalDate startOfPrev = budgets.stream()
                .map(Budget::getStartAt)
                .filter(Objects::nonNull)
                .min(LocalDate::compareTo)
                .orElse(previousMonth.atDay(1));

        LocalDate endOfPrev = previousMonth.atEndOfMonth();

        List<Object[]> sums = transactionRepository.sumAmountsByBudgetIdsAndPeriod(
                budgetIds, userId, startOfPrev, endOfPrev
        );

        // map: budgetId → sum
        Map<Long, BigDecimal> amountMap = sums.stream()
                .collect(Collectors.toMap(
                        row -> (Long) row[0],
                        row -> (BigDecimal) row[1]
                ));

        List<BudgetDTO> result = budgets.stream()
                .map(b -> new BudgetDTO(
                        b.getId(),
                        b.getUserId(),
                        b.getName(),
                        b.getSortOrder(),
                        b.getCreatedAt(),
                        b.getAmount(),
                        b.getIcon(),
                        b.getStartAt(),
                        b.getEndAt(),
                        b.getParent() != null ? b.getParent().getId() : null,
                        b.isDeleted(),
                        amountMap.getOrDefault(b.getId(), BigDecimal.ZERO)
                ))
                .toList();

        return ResponseEntity.ok(result);
    }

    @GetMapping("/{id}/transactions")
    public List<Transaction> getTransactionsByBudgetAndMonth(
            @PathVariable Long id,
            @RequestParam int year,
            @RequestParam int month,
            HttpServletRequest request
    ) {
        String userId = (String) request.getAttribute("firebaseUid");

        return transactionRepository.findByBudgetIdAndUserIdAndMonthAndYearAndIsDeletedFalseWithBudget(
                id, userId, month, year
        );
    }

    @PostMapping
    public ResponseEntity<?> create(@RequestBody Budget budget, HttpServletRequest request) {
        String userId = (String) request.getAttribute("firebaseUid");

        try {
            budget.setId(null);
            budget.setUserId(userId);
            Budget saved = budgetRepository.save(budget);
            return ResponseEntity.status(HttpStatus.CREATED).body(saved);
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body("Failed to create budget: " + e.getMessage());
        }
    }

    @PutMapping("/{id}")
    public ResponseEntity<?> update(@PathVariable Long id, @RequestBody Budget updated, HttpServletRequest request) {
        String userId = (String) request.getAttribute("firebaseUid");

        Optional<Budget> optional = budgetRepository.findById(id);
        if (optional.isEmpty()) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body("Budget not found.");
        }

        Budget budget = optional.get();
        if (!budget.getUserId().equals(userId) || budget.isDeleted()) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN).body("Unauthorized or deleted budget.");
        }

        budget.setName(updated.getName());
        budget.setSortOrder(updated.getSortOrder());
        budget.setAmount(updated.getAmount());
        budget.setIcon(updated.getIcon());
        budget.setParent(updated.getParent());
        budget.setStartAt(updated.getStartAt());
        budget.setEndAt(updated.getEndAt());

        Budget saved = budgetRepository.save(budget);
        return ResponseEntity.ok(saved);
    }

    @PutMapping("/reorder")
    public ResponseEntity<?> reorder(@RequestBody List<Budget> reorderedBudgets, HttpServletRequest request) {
        String userId = (String) request.getAttribute("firebaseUid");

        List<Budget> toSave = new ArrayList<>();

        for (int i = 0; i < reorderedBudgets.size(); i++) {
            Budget incoming = reorderedBudgets.get(i);

            Optional<Budget> optional = budgetRepository.findById(incoming.getId());
            if (optional.isEmpty()) {
                return ResponseEntity.status(HttpStatus.NOT_FOUND).body("Budget not found: ID " + incoming.getId());
            }

            Budget budget = optional.get();

            if (!budget.getUserId().equals(userId) || budget.isDeleted()) {
                return ResponseEntity.status(HttpStatus.FORBIDDEN).body("Unauthorized to reorder budget: ID " + budget.getId());
            }

            // Update sort order to match new position
            budget.setSortOrder(i);
            toSave.add(budget);
        }

        budgetRepository.saveAll(toSave);

        return ResponseEntity.noContent().build();
    }


    @DeleteMapping("/{id}")
    public ResponseEntity<?> delete(@PathVariable Long id, HttpServletRequest request) {
        String userId = (String) request.getAttribute("firebaseUid");

        Optional<Budget> optional = budgetRepository.findById(id);
        if (optional.isEmpty()) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body("Budget not found.");
        }

        Budget budget = optional.get();
        if (!budget.getUserId().equals(userId) || budget.isDeleted()) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN).body("Unauthorized or already deleted.");
        }

        budget.setDeleted(true);
        budgetRepository.save(budget);

        return ResponseEntity.noContent().build();
    }
}