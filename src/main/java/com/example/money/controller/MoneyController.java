package com.example.money.controller;

import com.example.money.repository.TransactionRepository;
import com.example.money.service.BalanceService;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;

@RestController
public class MoneyController {

    private final BalanceService balanceService;
    private final TransactionRepository transactionRepository;

    public MoneyController(BalanceService balanceService, TransactionRepository transactionRepository) {
        this.balanceService = balanceService;
        this.transactionRepository = transactionRepository;
    }

    @GetMapping("/health")
    public ResponseEntity<String> healthCheck() {
        return ResponseEntity.ok("OK");
    }

    @GetMapping("/calculate-balance")
    public BigDecimal getBalance(HttpServletRequest request) {
        String userId = (String) request.getAttribute("firebaseUid");

//        return balanceService.calculateBalance(uid);
        return transactionRepository.calculateBalance(userId);
    }

    @GetMapping("/calculate-budgeted-spent")
    public BigDecimal calculateBudgetedAmountBetween(@RequestParam int year,
                                                     @RequestParam int month,
                                                     HttpServletRequest request) {
        String userId = (String) request.getAttribute("firebaseUid");

        LocalDate startDate = LocalDate.of(year, month, 1);
        LocalDateTime start = startDate.atStartOfDay();
        LocalDateTime end = startDate.withDayOfMonth(startDate.lengthOfMonth()).atTime(23, 59, 59);

        return transactionRepository.calculateBudgetedAmountBetween(userId, start, end);
    }
}