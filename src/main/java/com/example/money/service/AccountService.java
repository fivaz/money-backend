package com.example.money.service;

import com.example.money.entity.Transaction;
import org.springframework.context.annotation.Bean;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.time.YearMonth;
import java.time.temporal.ChronoUnit;
import java.util.List;

@Service
public class AccountService {

    public BigDecimal calculateBalance(List<Transaction> transactions, Long accountId, LocalDateTime endOfMonth) {
        return transactions.stream()
                .map(t -> calculateTransactionAmount(t, accountId, endOfMonth))
                .reduce(BigDecimal.ZERO, BigDecimal::add);
    }

    private BigDecimal calculateTransactionAmount(Transaction t, Long accountId, LocalDateTime endOfMonth) {
        boolean isIncomingTransfer = t.getDestination() != null
                && t.getDestination().getId().equals(accountId);

        BigDecimal amount = t.getAmount();

        if (isSpreadTransaction(t)) {
            amount = calculateSpreadAmount(t, endOfMonth);
        }

        return isIncomingTransfer ? amount.abs() : amount;
    }

    private boolean isSpreadTransaction(Transaction t) {
        return t.getSpreadStart() != null && t.getSpreadEnd() != null;
    }

    private BigDecimal calculateSpreadAmount(Transaction t, LocalDateTime endOfMonth) {
        YearMonth spreadStart = YearMonth.from(t.getSpreadStart());
        YearMonth spreadEnd = YearMonth.from(t.getSpreadEnd());
        YearMonth effectiveEnd = YearMonth.from(endOfMonth);

        if (effectiveEnd.isBefore(spreadStart)) {
            return BigDecimal.ZERO; // not started yet
        }

        YearMonth calculationEnd = spreadEnd.isBefore(effectiveEnd) ? spreadEnd : effectiveEnd;
        long monthsInRange = ChronoUnit.MONTHS.between(spreadStart, calculationEnd) + 1;

        return monthsInRange < 1 ? BigDecimal.ZERO : t.getAmount().multiply(BigDecimal.valueOf(monthsInRange));
    }
}
