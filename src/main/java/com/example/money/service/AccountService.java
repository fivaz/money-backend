package com.example.money.service;

import com.example.money.entity.Transaction;
import org.springframework.context.annotation.Bean;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.time.OffsetDateTime;
import java.time.YearMonth;
import java.time.temporal.ChronoUnit;
import java.util.List;

@Service
public class AccountService {

    public BigDecimal calculateBalance(List<Transaction> transactions, Long accountId, OffsetDateTime asOfDateTime) {
        return transactions.stream()
                .map(t -> calculateTransactionAmount(t, accountId, asOfDateTime))
                .reduce(BigDecimal.ZERO, BigDecimal::add);
    }

    private BigDecimal calculateTransactionAmount(Transaction t, Long accountId, OffsetDateTime asOfDateTime) {
        boolean isIncomingTransfer = t.getDestination() != null
                && t.getDestination().getId().equals(accountId);

        BigDecimal amount = t.getAmount();

        if (isSpreadTransaction(t)) {
            amount = calculateSpreadAmount(t, asOfDateTime);
        }

        return isIncomingTransfer ? amount.abs() : amount;
    }

    private boolean isSpreadTransaction(Transaction t) {
        return t.getSpreadStart() != null && t.getSpreadEnd() != null;
    }

    private BigDecimal calculateSpreadAmount(Transaction t, OffsetDateTime asOfDateTime) {
        YearMonth spreadStart = YearMonth.from(t.getSpreadStart());
        YearMonth spreadEnd = YearMonth.from(t.getSpreadEnd());
        YearMonth asOfMonth = YearMonth.from(asOfDateTime);

        if (asOfMonth.isBefore(spreadStart)) {
            return BigDecimal.ZERO; // not started yet
        }

        YearMonth calculationEnd = spreadEnd.isBefore(asOfMonth) ? spreadEnd : asOfMonth;
        long monthsInRange = ChronoUnit.MONTHS.between(spreadStart, calculationEnd) + 1;

        return monthsInRange < 1 ? BigDecimal.ZERO : t.getAmount().multiply(BigDecimal.valueOf(monthsInRange));
    }
}
