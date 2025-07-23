package com.example.money.dto;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;

public record BudgetDTO(
        Long id,
        String userId,
        String name,
        int sortOrder,
        LocalDateTime createdAt,
        BigDecimal amount,
        String icon,
        LocalDate startAt,
        LocalDate endAt,
        Long parentId,
        boolean isDeleted,
        BigDecimal previousAmount
) {}