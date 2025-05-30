package com.example.money.service;

import com.example.money.repository.TransactionRepository;
import org.springframework.stereotype.Service;

@Service
public class BalanceService {

    private final TransactionRepository transactionRepository;

    public BalanceService(TransactionRepository transactionRepository) {
        this.transactionRepository = transactionRepository;
    }

//    public double calculateBalance(String userId) {
//        return transactionRepository.findByUserId(userId)
//                .stream()
//                .mapToDouble(t -> t.getAmount())
//                .sum();
//    }
}