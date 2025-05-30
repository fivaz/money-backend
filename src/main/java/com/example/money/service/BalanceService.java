package com.example.money.service;

import java.math.BigDecimal;

import com.example.money.entity.Transaction;
import com.example.money.repository.TransactionRepository;
import org.springframework.stereotype.Service;

@Service
public class BalanceService {

    private final TransactionRepository transactionRepository;

    public BalanceService(TransactionRepository transactionRepository) {
        this.transactionRepository = transactionRepository;
    }

    public BigDecimal calculateBalance(String userId) {
        return transactionRepository.findByUserIdAndIsDeletedFalse(userId)
                .stream()
                .map(Transaction::getAmount)
                .reduce(BigDecimal.ZERO, BigDecimal::add);
    }
}