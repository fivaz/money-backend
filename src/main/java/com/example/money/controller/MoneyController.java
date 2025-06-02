package com.example.money.controller;

import com.example.money.service.BalanceService;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.math.BigDecimal;

@RestController
public class MoneyController {

    private final BalanceService balanceService;

    public MoneyController(BalanceService balanceService) {
        this.balanceService = balanceService;
    }

    @GetMapping("/calculate-balance")
    public BigDecimal getBalance(HttpServletRequest request) {
        String uid = (String) request.getAttribute("firebaseUid");

        return balanceService.calculateBalance(uid);
    }
}