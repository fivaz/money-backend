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
import java.time.ZoneId;

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
    public BigDecimal getBalance(
            @RequestParam String asOf,
            @RequestHeader("X-User-Timezone") String timezone,
            HttpServletRequest request) {
        String userId = (String) request.getAttribute("firebaseUid");

        // 1. Parse inputs
        LocalDate localDate = LocalDate.parse(asOf);
        ZoneId zoneId = ZoneId.of(timezone);

        // 2. Calculate Cutoff
        // We want "Up to 2023-10-25", which means we include everything
        // BEFORE 2023-10-26 00:00:00 in the user's timezone.
        OffsetDateTime cutoffDate = localDate.plusDays(1)
                .atStartOfDay(zoneId)
                .toOffsetDateTime();

        return transactionRepository.calculatePaidBalance(userId, cutoffDate);
    }

    @GetMapping("/calculate-unpaid-balance")
    public BigDecimal getUnpaidBalance(HttpServletRequest request) {
        String userId = (String) request.getAttribute("firebaseUid");

        return transactionRepository.calculateUnpaidBalance(userId);
    }

    @GetMapping("/calculate-budgeted-spent")
    public BigDecimal calculateBudgetedAmountBetween(@RequestParam int year,
                                                     @RequestParam int month,
                                                     @RequestHeader("X-User-Timezone") String timezone,
                                                     HttpServletRequest request) {
        String userId = (String) request.getAttribute("firebaseUid");

        ZoneId zoneId = ZoneId.of(timezone);
        YearMonth ym = YearMonth.of(year, month);
        LocalDate startOfMonth = ym.atDay(1);
        LocalDate endOfMonth = ym.atEndOfMonth();
        OffsetDateTime startOfMonthTime = startOfMonth.atStartOfDay(zoneId).toOffsetDateTime();
        OffsetDateTime endOfMonthTime = endOfMonth.atTime(LocalTime.MAX).atZone(zoneId).toOffsetDateTime();

        return transactionRepository.calculateBudgetedAmountInDateRange(userId, startOfMonth, endOfMonth, startOfMonthTime, endOfMonthTime);
    }
}