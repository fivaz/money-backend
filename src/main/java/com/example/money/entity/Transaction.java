package com.example.money.entity;
import com.fasterxml.jackson.annotation.*;
import lombok.Data;

import jakarta.persistence.*;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;

@Data
@Entity
public class Transaction {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private String userId;

    private String description;

    @Column(nullable = false, updatable = false)
    private LocalDateTime createdAt;

    @Column(precision = 19, scale = 4, nullable = false)
    private BigDecimal amount;

    @JsonFormat(pattern = "yyyy-MM-dd'T'HH:mm")
    @Column(nullable = false)
    private LocalDateTime date;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "budget_id")
    private Budget budget;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "account_id")
    private Account account;

    @JsonProperty("isPaid")
    private boolean isPaid = false;

    private LocalDate referenceDate;

    private LocalDate spreadStart;

    private LocalDate spreadEnd;

    @JsonInclude(JsonInclude.Include.NON_NULL)
    @JsonProperty("isDeleted")
    private boolean isDeleted = false;

    @PrePersist
    protected void onCreate() {
        this.createdAt = LocalDateTime.now();
    }
}