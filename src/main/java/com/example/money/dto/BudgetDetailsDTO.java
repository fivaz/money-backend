package com.example.money.dto;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;

@Data
public// Prevent infinite recursion in parent serialization
class BudgetDetailsDTO {
    private Long id;

    private String userId;

    private String name;

    private int sortOrder;

    private LocalDateTime createdAt;

    private BigDecimal amount;

    private BigDecimal accumulativeAmount;

    private String icon;

    @JsonProperty("isAccumulative")
    private boolean isAccumulative;

    private LocalDate startAt;

    private LocalDate endAt;

    private BudgetDetailsDTO parent;
}