package com.example.money.mapper;

import com.example.money.dto.*;
import com.example.money.entity.*;

public class TransactionMapper {

    public static TransactionDTO toDTO(Transaction transaction) {
        TransactionDTO dto = new TransactionDTO();
        dto.setId(transaction.getId());
        dto.setDescription(transaction.getDescription());
        dto.setAmount(transaction.getAmount());
        dto.setDate(transaction.getDate());
        dto.setPaid(transaction.isPaid());
        dto.setReferenceDate(transaction.getReferenceDate());
        dto.setSpreadStart(transaction.getSpreadStart());
        dto.setSpreadEnd(transaction.getSpreadEnd());

        if (transaction.getBudget() != null) {
            dto.setBudget(BudgetMapper.toDTO(transaction.getBudget()));
        }

        return dto;
    }
}