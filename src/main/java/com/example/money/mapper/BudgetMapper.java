package com.example.money.mapper;

import com.example.money.dto.BudgetDTO;
import com.example.money.dto.BudgetDTO;
import com.example.money.entity.Budget;

public class BudgetMapper {

    public static BudgetDTO toDTO(Budget budget) {
        BudgetDTO dto = new BudgetDTO();
        dto.setId(budget.getId());
        dto.setName(budget.getName());
        dto.setAmount(budget.getAmount());
        dto.setIcon(budget.getIcon());
        dto.setSortOrder(budget.getSortOrder());

        return dto;
    }
}