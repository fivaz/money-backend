package com.example.money.dto;

import com.fasterxml.jackson.annotation.JsonFormat;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;

@Data
public class TransactionDTO {
    private Long id;
    private String description;
    private BigDecimal amount;

    @JsonFormat(pattern = "yyyy-MM-dd'T'HH:mm")
    private LocalDateTime date;
    @JsonProperty("isPaid")
    private boolean paid;

    @JsonInclude(JsonInclude.Include.NON_NULL)
    private LocalDate referenceDate;

    @JsonInclude(JsonInclude.Include.NON_NULL)
    private LocalDate spreadStart;

    @JsonInclude(JsonInclude.Include.NON_NULL)
    private LocalDate spreadEnd;
    private BudgetDTO budget;

    // Getters and Setters (or use Lombok @Data if preferred)
}