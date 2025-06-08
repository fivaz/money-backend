package com.example.money.controller;

import com.example.money.entity.Budget;
import com.example.money.entity.Transaction;
import com.example.money.repository.BudgetRepository;
import com.example.money.repository.TransactionRepository;
import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

@RestController
@RequestMapping("/transactions")
@RequiredArgsConstructor
public class TransactionController {

    private final TransactionRepository transactionRepository;

    private final BudgetRepository budgetRepository;

    @GetMapping("/by-date")
    public List<Transaction> getByDate(
            @RequestParam int year,
            @RequestParam int month,
            HttpServletRequest request
    ) {
        String userId = (String) request.getAttribute("firebaseUid");

        return transactionRepository.findByUserIdAndMonthAndYearAndIsDeletedFalseWithBudgetOrderByDateDesc(userId, month, year);
    }

    @GetMapping("/search")
    public Page<Transaction> searchTransactions(
            @RequestParam String query,
            @PageableDefault(size = 10) Pageable pageable,
            HttpServletRequest request
    ) {
        String userId = (String) request.getAttribute("firebaseUid");

        if (query != null && !query.isBlank()) {
            query = query.trim().toLowerCase();
        }

        return transactionRepository.searchByDescriptionOrBudgetName(userId, query, pageable);
    }

    @PostMapping
    public ResponseEntity<?> create(@RequestBody Transaction transaction, HttpServletRequest request) {
        String userId = (String) request.getAttribute("firebaseUid");

        transaction.setUserId(userId);
        transaction.setId(null); // new entity

        if (transaction.getBudget() != null && transaction.getBudget().getId() != null) {
            Optional<Budget> budgetOpt = budgetRepository.findById(transaction.getBudget().getId());
            if (budgetOpt.isEmpty() || !budgetOpt.get().getUserId().equals(userId)) {
                return ResponseEntity.status(HttpStatus.FORBIDDEN).body("Invalid budget");
            }
            transaction.setBudget(budgetOpt.get());
        } else {
            transaction.setBudget(null);
        }

        Transaction saved = transactionRepository.save(transaction);
        return ResponseEntity.ok(saved);
    }

    @PutMapping("/{id}")
    public ResponseEntity<?> update(@PathVariable Long id,
                                    @RequestBody Transaction updated,
                                    HttpServletRequest request) {
        String userId = (String) request.getAttribute("firebaseUid");

        Optional<Transaction> optionalTx = transactionRepository.findById(id);
        if (optionalTx.isEmpty()) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body("Transaction not found.");
        }

        Transaction tx = optionalTx.get();
        if (!tx.getUserId().equals(userId) || tx.isDeleted()) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN).body("Unauthorized or deleted transaction.");
        }

        tx.setDescription(updated.getDescription());
        tx.setAmount(updated.getAmount());
        tx.setDate(updated.getDate());
        tx.setReferenceDate(updated.getReferenceDate());
        tx.setPaid(updated.isPaid());
        tx.setSpreadStart(updated.getSpreadStart());
        tx.setSpreadEnd(updated.getSpreadEnd());

        // Handle budget update
        if (updated.getBudget() != null && updated.getBudget().getId() != null) {
            Optional<Budget> budgetOpt = budgetRepository.findById(updated.getBudget().getId());
            if (budgetOpt.isEmpty() || !budgetOpt.get().getUserId().equals(userId)) {
                return ResponseEntity.status(HttpStatus.FORBIDDEN).body("Invalid budget");
            }
            tx.setBudget(budgetOpt.get());
        } else {
            tx.setBudget(null);
        }

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
                    .body("Transaction not found.");
        }

        Transaction tx = optionalTx.get();
        if (!tx.getUserId().equals(userId)) {
            return ResponseEntity
                    .status(HttpStatus.FORBIDDEN)
                    .body("Unauthorized to delete this transaction.");
        }

        if (tx.isDeleted()) {
            return ResponseEntity
                    .status(HttpStatus.GONE)
                    .body("Transaction already deleted.");
        }

        tx.setDeleted(true);
        transactionRepository.save(tx);
        return ResponseEntity.noContent().build();
    }
}