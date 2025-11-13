package com.example.money.dto;

import java.math.BigDecimal;

public record BudgetedSpentResponse(
        BigDecimal paid,
        BigDecimal unpaid
) {}