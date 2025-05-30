package com.example.money.entity;

import jakarta.persistence.*;

@Entity
public class Transaction {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private Long userId;

    private Double amount;

    public Transaction() {}

    public Transaction(Long userId, Double amount) {
        this.userId = userId;
        this.amount = amount;
    }

    public Long getId() { return id; }
    public Long getUserId() { return userId; }
    public Double getAmount() { return amount; }

    public void setUserId(Long userId) { this.userId = userId; }
    public void setAmount(Double amount) { this.amount = amount; }
}