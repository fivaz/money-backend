package com.example.money.controller;

import com.example.money.dto.BudgetDetailsDTO;
import com.example.money.entity.Budget;
import com.example.money.entity.Transaction;
import com.example.money.repository.BudgetRepository;
import com.example.money.repository.TransactionRepository;
import com.example.money.service.BudgetService;
import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.time.*;
import java.util.*;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/budgets")
@RequiredArgsConstructor
public class BudgetController {

    private final BudgetRepository budgetRepository;

    private final TransactionRepository transactionRepository;

    private final BudgetService budgetService;

    @GetMapping("/by-date")
    public List<Budget> getByDate(
            @RequestParam int year,
            @RequestParam int month,
            HttpServletRequest request) {

        String userId = (String) request.getAttribute("firebaseUid");
        YearMonth targetMonth = YearMonth.of(year, month);
        LocalDate firstDay = targetMonth.atDay(1);
        LocalDate lastDay = targetMonth.atEndOfMonth();

        return budgetRepository.findBudgetsByUserIdWithinDateRangeSortOrderAsc(userId, firstDay, lastDay);
    }

    @GetMapping("/by-date-with-details")
    public List<BudgetDetailsDTO> getByDateWithDetails(
            @RequestParam int year,
            @RequestParam int month,
            HttpServletRequest request) {

        String userId = (String) request.getAttribute("firebaseUid");
        YearMonth targetMonth = YearMonth.of(year, month);
        LocalDate firstDay = targetMonth.atDay(1);
        LocalDate lastDay = targetMonth.atEndOfMonth();

        List<Budget> budgets = budgetRepository
                .findBudgetsByUserIdWithinDateRangeSortOrderAsc(userId, firstDay, lastDay);

        return budgets.stream()
                .map(budget -> budgetService.buildBudgetDetails(budget, targetMonth))
                .collect(Collectors.toList());
    }

    @GetMapping("/{id}/transactions")
    public List<Transaction> getTransactionsByBudgetAndMonth(
            @PathVariable Long id,
            @RequestParam int year,
            @RequestParam int month,
            @RequestParam String timezone,
            HttpServletRequest request
    ) {
        String userId = (String) request.getAttribute("firebaseUid");

        ZoneId zoneId = ZoneId.of(timezone);

        YearMonth ym = YearMonth.of(year, month);

        // Start of month in user timezone
        ZonedDateTime startOfMonth = ym.atDay(1).atStartOfDay(zoneId);
        // Start of next month in user timezone
        ZonedDateTime startOfNextMonth = ym.plusMonths(1).atDay(1).atStartOfDay(zoneId);

        OffsetDateTime startOfMonthUTC = startOfMonth.toOffsetDateTime();
        OffsetDateTime startOfNextMonthUTC = startOfNextMonth.toOffsetDateTime();

        LocalDate startOfMonthDate = startOfMonth.toLocalDate();
        LocalDate endOfMonthDate = startOfMonth.plusMonths(1).minusDays(1).toLocalDate();

        return transactionRepository
                .findByBudgetIdAndUserIdAndDateRange(
                        id,
                        userId,
                        startOfMonthDate,
                        endOfMonthDate,
                        startOfMonthUTC,
                        startOfNextMonthUTC
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
        budget.setAccumulative(updated.isAccumulative());

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