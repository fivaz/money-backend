package com.example.money.dto;
import java.math.BigDecimal;

public interface MonthlyExpenseSummary {
    String getTime();
    BigDecimal getTotal();
}