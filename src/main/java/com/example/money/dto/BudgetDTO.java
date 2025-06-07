package com.example.money.dto;

import com.example.money.entity.Budget;
import lombok.Data;

import java.math.BigDecimal;

@Data
public class BudgetDTO {
    private Long id;
    private String name;
    private BigDecimal amount;
    private String icon;
    private Budget parent;
    private int sortOrder;
}