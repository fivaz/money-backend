package com.example.money.controller;

import com.example.money.repository.TransactionRepository;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.time.YearMonth;

@RestController
public class MoneyController {

    private final TransactionRepository transactionRepository;

    public MoneyController(TransactionRepository transactionRepository) {
        this.transactionRepository = transactionRepository;
    }

    @GetMapping("/health")
    public ResponseEntity<String> healthCheck() {
        return ResponseEntity.ok("OK");
    }

    @GetMapping("/calculate-balance")
    public BigDecimal getBalance(HttpServletRequest request) {
        String userId = (String) request.getAttribute("firebaseUid");

        return transactionRepository.calculateBalance(userId);
    }

    @GetMapping("/calculate-budgeted-spent")
    public BigDecimal calculateBudgetedAmountBetween(@RequestParam int year,
                                                     @RequestParam int month,
                                                     HttpServletRequest request) {
        String userId = (String) request.getAttribute("firebaseUid");

        YearMonth ym = YearMonth.of(year, month);
        LocalDate startOfMonth = ym.atDay(1);
        LocalDate endOfMonth = ym.atEndOfMonth();
        LocalDateTime startOfMonthTime = startOfMonth.atStartOfDay();
        LocalDateTime endOfMonthTime = endOfMonth.atTime(LocalTime.MAX);


        return transactionRepository.calculateBudgetedAmountByMonthAndYear(userId, startOfMonth, endOfMonth, startOfMonthTime, endOfMonthTime);
    }
}