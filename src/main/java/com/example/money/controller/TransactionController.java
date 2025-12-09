package com.example.money.controller;

import com.example.money.dto.MonthlyExpenseSummary;
import com.example.money.entity.Budget;
import com.example.money.entity.Transaction;
import com.example.money.repository.BudgetRepository;
import com.example.money.repository.TransactionRepository;
import com.example.money.service.TransactionService;
import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;
import java.util.Optional;

@RestController
@RequestMapping("/transactions")
@RequiredArgsConstructor
public class TransactionController {

    private final TransactionRepository transactionRepository;

    private final BudgetRepository budgetRepository;

    private final TransactionService transactionService;

    @GetMapping("/search")
    public ResponseEntity<Page<Transaction>> searchTransactions(
            @RequestParam String query,
            @PageableDefault(size = 10) Pageable pageable,
            HttpServletRequest request
    ) {
        String userId = (String) request.getAttribute("firebaseUid");

        if (query != null && !query.isBlank()) {
            query = query.trim().toLowerCase();
        }

        Page<Transaction> result = transactionRepository.searchByDescriptionOrBudgetName(userId, query, pageable);
        return ResponseEntity.ok(result);
    }

    @GetMapping("/monthly-summary")
    public ResponseEntity<List<MonthlyExpenseSummary>> getMonthlySummary(HttpServletRequest request) {
        String userId = (String) request.getAttribute("firebaseUid");

        List<MonthlyExpenseSummary> summary = transactionRepository.findMonthlyExpenseSummary(userId);
        return ResponseEntity.ok(summary);
    }

    @PostMapping
    public ResponseEntity<?> create(@RequestBody Transaction transaction, HttpServletRequest request) {
        String userId = (String) request.getAttribute("firebaseUid");

        transaction.setUserId(userId);
        transaction.setId(null);

        // Validate and attach budget if present
        if (transactionService.hasBudget(transaction)) {
            Optional<Budget> budgetOpt = budgetRepository.findByIdAndUserIdAndIsDeletedFalse(
                    transaction.getBudget().getId(), userId
            );
            if (budgetOpt.isEmpty()) {
                return ResponseEntity.status(HttpStatus.FORBIDDEN).body(Map.of("message", "Invalid budget"));
            }
            transaction.setBudget(budgetOpt.get());
        } else {
            transaction.setBudget(null);
        }

        Transaction saved = transactionService.saveTransactionWithAccountChecks(transaction, userId);
        return ResponseEntity.status(HttpStatus.CREATED).body(saved);
    }

    @PutMapping("/{id}")
    public ResponseEntity<?> update(@PathVariable Long id,
                                    @RequestBody Transaction updated,
                                    HttpServletRequest request) {
        String userId = (String) request.getAttribute("firebaseUid");

        Optional<Transaction> optionalTx = transactionRepository.findById(id);
        if (optionalTx.isEmpty()) {
            return ResponseEntity
                    .status(HttpStatus.NOT_FOUND)
                    .body(Map.of("message", "Transaction not found."));
        }

        Transaction tx = optionalTx.get();
        if (!tx.getUserId().equals(userId) || tx.isDeleted()) {
            return ResponseEntity
                    .status(HttpStatus.FORBIDDEN)
                    .body(Map.of("message", "Unauthorized or deleted transaction."));
        }

        // Validate budget
        if (transactionService.hasBudget(updated)) {
            Optional<Budget> budgetOpt = budgetRepository.findByIdAndUserIdAndIsDeletedFalse(
                    updated.getBudget().getId(), userId
            );
            if (budgetOpt.isEmpty()) {
                return ResponseEntity
                        .status(HttpStatus.FORBIDDEN)
                        .body(Map.of("message", "Invalid budget"));
            }
            tx.setBudget(budgetOpt.get());
        } else {
            tx.setBudget(null);
        }

        // Validate account
        if (transactionService.doesNotOwnAccount(updated.getAccount(), userId)) {
            return ResponseEntity
                    .status(HttpStatus.FORBIDDEN)
                    .body(Map.of("message", "Invalid account"));
        }

        // Validate destination
        if (updated.getDestination() != null && transactionService.doesNotOwnAccount(updated.getDestination(), userId)) {
            return ResponseEntity
                    .status(HttpStatus.FORBIDDEN)
                    .body(Map.of("message", "Invalid destination account"));
        }

        // Apply updates to transaction
        transactionService.copyUpdatableFields(updated, tx);

        Transaction saved = transactionRepository.save(tx);
        return ResponseEntity.ok(saved);
    }


    @DeleteMapping("/{id}")
    public ResponseEntity<?> delete(@PathVariable Long id, HttpServletRequest request) {
        String userId = (String) request.getAttribute("firebaseUid");

        Optional<Transaction> optionalTx = transactionRepository.findById(id);
        if (optionalTx.isEmpty()) {
            return ResponseEntity
                    .status(HttpStatus.NOT_FOUND)
                    .body(Map.of("message", "Transaction not found."));
        }

        Transaction tx = optionalTx.get();
        if (!tx.getUserId().equals(userId)) {
            return ResponseEntity
                    .status(HttpStatus.FORBIDDEN)
                    .body(Map.of("message", "Unauthorized to delete this transaction."));
        }

        if (tx.isDeleted()) {
            return ResponseEntity
                    .status(HttpStatus.GONE)
                    .body(Map.of("message", "Transaction already deleted."));
        }

        tx.setDeleted(true);
        transactionRepository.save(tx);
        return ResponseEntity.ok(Map.of("message", "Transaction deleted successfully."));
    }
}