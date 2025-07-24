package com.example.money.service;

import com.example.money.dto.BudgetDetailsDTO;
import com.example.money.entity.Budget;

import java.time.LocalDate;
import java.time.YearMonth;
import java.time.temporal.ChronoUnit;

public class BudgetService {

    public static int monthsBetweenInclusive(LocalDate start, YearMonth endMonth) {
        YearMonth startMonth = YearMonth.from(start);
        return (int) ChronoUnit.MONTHS.between(startMonth, endMonth);
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
}
