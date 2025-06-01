package com.example.money.controller;

import com.example.money.entity.Transaction;
import com.example.money.repository.TransactionRepository;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

@RestController
@RequestMapping("/transactions")
public class TransactionController {

    private final TransactionRepository transactionRepository;

    public TransactionController(TransactionRepository transactionRepository) {
        this.transactionRepository = transactionRepository;
    }

    @GetMapping
    public List<Transaction> getAll(HttpServletRequest request) {
        String userId = (String) request.getAttribute("firebaseUid");
        return transactionRepository.findByUserIdAndIsDeletedFalse(userId);
    }

    @GetMapping("/{id}")
    public ResponseEntity<Transaction> getById(@PathVariable Long id, HttpServletRequest request) {
        String userId = (String) request.getAttribute("firebaseUid");
        return transactionRepository.findById(id)
                .filter(tx -> tx.getUserId().equals(userId) && !tx.isDeleted())
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }

    @GetMapping("/current-month")
    public List<Transaction> getCurrentMonth(
            @RequestParam int year,
            @RequestParam int month,
            HttpServletRequest request
    ) {
        String userId = (String) request.getAttribute("firebaseUid");

        LocalDate startDate = LocalDate.of(year, month, 1);
        LocalDateTime start = startDate.atStartOfDay(); // 00:00
        LocalDateTime end = startDate.withDayOfMonth(startDate.lengthOfMonth()).atTime(23, 59, 59); // 23:59:59

        return transactionRepository.findByUserIdAndDateBetweenAndIsDeletedFalseOrderByDateDescWithBudget(userId, start, end);
    }

    @PostMapping
    public ResponseEntity<Transaction> create(@RequestBody Transaction transaction, HttpServletRequest request) {
        String userId = (String) request.getAttribute("firebaseUid");

        transaction.setUserId(userId);
        transaction.setId(null); // make sure it’s treated as a new entity
        return ResponseEntity.ok(transactionRepository.save(transaction));
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
                    .body("Transaction not found.");
        }

        Transaction tx = optionalTx.get();
        if (!tx.getUserId().equals(userId) || tx.isDeleted()) {
            return ResponseEntity
                    .status(HttpStatus.FORBIDDEN)
                    .body("Unauthorized or deleted transaction.");
        }

        tx.setDescription(updated.getDescription());
        tx.setAmount(updated.getAmount());
        tx.setDate(updated.getDate());
        tx.setReferenceDate(updated.getReferenceDate());
        tx.setPaid(updated.isPaid());

        return ResponseEntity.ok(transactionRepository.save(tx));
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