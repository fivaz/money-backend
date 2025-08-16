package com.example.money.entity;
import com.example.money.json.EmptyStringSerializer;
import com.example.money.json.StringToLocalDateDeserializer;
import com.fasterxml.jackson.annotation.*;
import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
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

    @Column(precision = 19, scale = 4, nullable = false)
    private BigDecimal amount;

    @Column(nullable = false)
    private LocalDateTime date;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "budget_id")
    private Budget budget;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "account_id", nullable = false)
    private Account account;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "destination_id")
    private Account destination;

    @JsonProperty("isPaid")
    private boolean isPaid = false;

    @JsonSerialize(nullsUsing = EmptyStringSerializer.class)
    @JsonDeserialize(using = StringToLocalDateDeserializer.class)
    private LocalDate referenceDate;

    @JsonSerialize(nullsUsing = EmptyStringSerializer.class)
    @JsonDeserialize(using = StringToLocalDateDeserializer.class)
    private LocalDate spreadStart;

    @JsonSerialize(nullsUsing = EmptyStringSerializer.class)
    @JsonDeserialize(using = StringToLocalDateDeserializer.class)
    private LocalDate spreadEnd;


    @Column(nullable = false, updatable = false)
    private LocalDateTime createdAt;

    @JsonInclude(JsonInclude.Include.NON_NULL)
    @JsonProperty("isDeleted")
    private boolean isDeleted = false;

    @PrePersist
    protected void onCreate() {
        this.createdAt = LocalDateTime.now();
    }
}