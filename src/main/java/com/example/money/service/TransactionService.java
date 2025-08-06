package com.example.money.service;

import com.example.money.entity.Account;
import com.example.money.entity.Transaction;
import com.example.money.repository.AccountRepository;
import com.example.money.repository.TransactionRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;

@Service
@RequiredArgsConstructor
public class TransactionService {

    private final TransactionRepository transactionRepository;

    private final AccountRepository accountRepository;

    public boolean hasBudget(Transaction transaction) {
        return transaction.getBudget() != null && transaction.getBudget().getId() != null;
    }

    public Transaction saveTransactionWithAccountChecks(Transaction transaction, String userId) {
        // Validate account
        if (doesNotOwnAccount(transaction.getAccount(), userId)) {
            throw new ResponseStatusException(HttpStatus.FORBIDDEN, "Invalid account");
        }

        // Validate destination account if present
        if (transaction.getDestination() != null && doesNotOwnAccount(transaction.getDestination(), userId)) {
            throw new ResponseStatusException(HttpStatus.FORBIDDEN, "Invalid destination account");
        }

        return transactionRepository.save(transaction);
    }

    public boolean doesNotOwnAccount(Account account, String userId) {
        return account == null ||
                accountRepository.findByIdAndUserIdAndIsDeletedFalse(account.getId(), userId).isEmpty();
    }

    public void copyUpdatableFields(Transaction source, Transaction target) {
        target.setDescription(source.getDescription());
        target.setAmount(source.getAmount());
        target.setDate(source.getDate());
        target.setReferenceDate(source.getReferenceDate());
        target.setPaid(source.isPaid());
        target.setSpreadStart(source.getSpreadStart());
        target.setSpreadEnd(source.getSpreadEnd());
        target.setAccount(source.getAccount());
        target.setDestination(source.getDestination());
        // Note: userId, id, and deleted flags should never be copied blindly
    }
}
