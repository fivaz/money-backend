package com.example.money.controller;

import com.example.money.entity.Account;
import com.example.money.entity.Transaction;
import com.example.money.repository.AccountRepository;
import com.example.money.repository.TransactionRepository;
import com.example.money.service.AccountService;
import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.time.YearMonth;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

@RestController
@RequestMapping("/accounts")
@RequiredArgsConstructor
public class AccountController {

    private final AccountRepository accountRepository;

    private final TransactionRepository transactionRepository;

    private final AccountService accountService;

    @GetMapping
    public ResponseEntity<List<Account>> getAll(HttpServletRequest request) {
        String userId = (String) request.getAttribute("firebaseUid");
        List<Account> accounts = accountRepository.findByUserIdAndIsDeletedFalseOrderBySortOrderAsc(userId);

        return ResponseEntity.ok(accounts);
    }

    @GetMapping("/{id}/transactions")
    public List<Transaction> getTransactionsByAccountAndMonth(@PathVariable Long id,
                                                              @RequestParam int year,
                                                              @RequestParam int month,
                                                              HttpServletRequest request) {
        String userId = (String) request.getAttribute("firebaseUid");

        YearMonth ym = YearMonth.of(year, month);
        LocalDate startOfMonth = ym.atDay(1);
        LocalDate endOfMonth = ym.atEndOfMonth();

        LocalDateTime startOfMonthDateTime = startOfMonth.atStartOfDay();
        LocalDateTime endOfMonthDateTime = endOfMonth.atTime(LocalTime.MAX); // 23:59:59.999999999

        return transactionRepository.findByAccountIdOrDestinationIdAndUserIdAndDateRange(
                id,
                userId,
                startOfMonth,
                endOfMonth,
                startOfMonthDateTime,
                endOfMonthDateTime
        );
    }


    @GetMapping("/{id}/balance")
    public BigDecimal getBalance(@PathVariable Long id,
                                 @RequestParam int year,
                                 @RequestParam int month,
                                 HttpServletRequest request) {
        String userId = (String) request.getAttribute("firebaseUid");

        YearMonth ym = YearMonth.of(year, month);
        LocalDate endOfMonthDate = ym.atEndOfMonth();
        LocalDateTime endOfMonthDateTime = endOfMonthDate.atTime(23, 59, 59);

        List<Transaction> transactions = transactionRepository.findUpToMonthAndYearPaidTransactions(
                id, userId, endOfMonthDateTime, endOfMonthDate
        );

        return accountService.calculateBalance(transactions, id, endOfMonthDateTime);
    }

    @PostMapping
    public ResponseEntity<?> create(@RequestBody Account account, HttpServletRequest request) {
        String userId = (String) request.getAttribute("firebaseUid");

        try {
            account.setId(null);
            account.setUserId(userId);
            Account saved = accountRepository.save(account);
            return ResponseEntity.status(HttpStatus.CREATED).body(saved);
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body("Failed to create account: " + e.getMessage());
        }
    }

    @PutMapping("/{id}")
    public ResponseEntity<?> update(@PathVariable Long id, @RequestBody Account updated, HttpServletRequest request) {
        String userId = (String) request.getAttribute("firebaseUid");

        Optional<Account> optional = accountRepository.findById(id);
        if (optional.isEmpty()) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body("Account not found.");
        }

        Account account = optional.get();
        if (!account.getUserId().equals(userId) || account.isDeleted()) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN).body("Unauthorized or deleted account.");
        }

        account.setName(updated.getName());
        account.setSortOrder(updated.getSortOrder());
        account.setIcon(updated.getIcon());

        Account saved = accountRepository.save(account);
        return ResponseEntity.ok(saved);
    }

    @PutMapping("/reorder")
    public ResponseEntity<?> reorder(@RequestBody List<Account> reorderedAccounts, HttpServletRequest request) {
        String userId = (String) request.getAttribute("firebaseUid");

        List<Account> toSave = new ArrayList<>();

        for (int i = 0; i < reorderedAccounts.size(); i++) {
            Account incoming = reorderedAccounts.get(i);

            Optional<Account> optional = accountRepository.findById(incoming.getId());
            if (optional.isEmpty()) {
                return ResponseEntity.status(HttpStatus.NOT_FOUND).body("Account not found: ID " + incoming.getId());
            }

            Account account = optional.get();

            if (!account.getUserId().equals(userId) || account.isDeleted()) {
                return ResponseEntity.status(HttpStatus.FORBIDDEN).body("Unauthorized to reorder account: ID " + account.getId());
            }

            // Update sort order to match new position
            account.setSortOrder(i);
            toSave.add(account);
        }

        accountRepository.saveAll(toSave);

        return ResponseEntity.noContent().build();
    }


    @DeleteMapping("/{id}")
    public ResponseEntity<?> delete(@PathVariable Long id, HttpServletRequest request) {
        String userId = (String) request.getAttribute("firebaseUid");

        Optional<Account> optional = accountRepository.findById(id);
        if (optional.isEmpty()) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body("Account not found.");
        }

        Account account = optional.get();
        if (!account.getUserId().equals(userId) || account.isDeleted()) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN).body("Unauthorized or already deleted.");
        }

        account.setDeleted(true);
        accountRepository.save(account);

        return ResponseEntity.noContent().build();
    }
}