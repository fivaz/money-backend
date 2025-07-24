package com.example.money.service;

import com.example.money.dto.BudgetDetailsDTO;
import com.example.money.entity.Budget;
import com.example.money.entity.Transaction;
import com.example.money.repository.TransactionRepository;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.YearMonth;
import java.time.temporal.ChronoUnit;

@Service
public class BudgetService {

    private final TransactionRepository transactionRepository;

    public BudgetService(TransactionRepository transactionRepository) {
        this.transactionRepository = transactionRepository;
    }

    public static BudgetDetailsDTO getDTO(Budget budget) {
        BudgetDetailsDTO budgetDTO = new BudgetDetailsDTO();
        budgetDTO.setId(budget.getId());
        budgetDTO.setUserId(budget.getUserId());
        budgetDTO.setName(budget.getName());
        budgetDTO.setSortOrder(budget.getSortOrder());
        budgetDTO.setCreatedAt(budget.getCreatedAt());
        budgetDTO.setAmount(budget.getAmount());
        budgetDTO.setIcon(budget.getIcon());
        budgetDTO.setAccumulative(budget.isAccumulative());
        budgetDTO.setStartAt(budget.getStartAt());
        budgetDTO.setEndAt(budget.getEndAt());
        return budgetDTO;
    }

    public BudgetDetailsDTO buildBudgetDetails(Budget budget, YearMonth targetMonth) {
        BudgetDetailsDTO dto = getDTO(budget);
        dto.setParent(getParentDTO(budget));

        BigDecimal accumulativeAmount = computeAccumulativeAmount(budget, targetMonth);
        dto.setAccumulativeAmount(accumulativeAmount);

        return dto;
    }

    private BudgetDetailsDTO getParentDTO(Budget budget) {
        if (budget.getParent() != null) {
            return getDTO(budget.getParent());
        } else {
            return null;
        }
    }

    public static int monthsBetweenInclusive(LocalDate start, YearMonth endMonth) {
        YearMonth startMonth = YearMonth.from(start);
        return (int) ChronoUnit.MONTHS.between(startMonth, endMonth);
    }

    private BigDecimal computeAccumulativeAmount(Budget budget, YearMonth targetMonth) {
        if (!budget.isAccumulative()) {
            return BigDecimal.ZERO;
        }

        LocalDate calcStart = budget.getStartAt();
        if (calcStart == null || !YearMonth.from(calcStart).isBefore(targetMonth)) {
            return BigDecimal.ZERO;
        }

        LocalDate calcEnd = targetMonth.minusMonths(1).atEndOfMonth();

        BigDecimal previousTransactionsSum = transactionRepository
                .findByBudgetIdAndDateRangeWithSpecialCases(budget.getId(), calcStart, calcEnd)
                .stream()
                .map(Transaction::getAmount)
                .reduce(BigDecimal.ZERO, BigDecimal::add);

        int monthsBetween = monthsBetweenInclusive(calcStart, targetMonth);

        return budget.getAmount()
                .multiply(BigDecimal.valueOf(monthsBetween))
                .add(previousTransactionsSum);
    }
}
