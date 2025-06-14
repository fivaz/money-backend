package com.example.money.entity;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;
import jakarta.persistence.*;
import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Data
@Entity
public class Budget {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private String userId;

    private String name;

    private int sortOrder;

    @Column(nullable = false, updatable = false)
    private LocalDateTime createdAt;

    @Column(precision = 19, scale = 4)
    private BigDecimal amount;

    private String icon;

    @ManyToOne
    @JoinColumn(name = "parent_id")
    private Budget parent;

    @JsonProperty("isDeleted")
    private boolean isDeleted = false;

    @PrePersist
    protected void onCreate() {
        this.createdAt = LocalDateTime.now();
    }
}