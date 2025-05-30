package com.example.money.controller;

import com.example.money.service.BalanceService;
import org.springframework.web.bind.annotation.*;

@RestController
public class BalanceController {

    private final BalanceService balanceService;

    public BalanceController(BalanceService balanceService) {
        this.balanceService = balanceService;
    }

    @GetMapping("/balance-calc")
    public double getBalance(@RequestParam Long userId) {
        return balanceService.calculateBalance(userId);
    }
}