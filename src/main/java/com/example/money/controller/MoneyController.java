package com.example.money.controller;

import com.example.money.repository.TransactionRepository;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalTime;
import java.time.OffsetDateTime;
import java.time.YearMonth;
import java.time.ZoneOffset;

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
        OffsetDateTime startOfMonthTime = startOfMonth.atStartOfDay().atOffset(ZoneOffset.UTC);
        OffsetDateTime endOfMonthTime = endOfMonth.atTime(LocalTime.MAX).atOffset(ZoneOffset.UTC);

        return transactionRepository.calculateBudgetedAmountInDateRange(userId, startOfMonth, endOfMonth, startOfMonthTime, endOfMonthTime);
    }
}