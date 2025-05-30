package com.example.money.controller;

import com.example.money.service.BalanceService;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.web.bind.annotation.*;

@RestController
public class MoneyController {

    private final BalanceService balanceService;

    public MoneyController(BalanceService balanceService) {
        this.balanceService = balanceService;
    }

//    @GetMapping("/balance-calc")
//    public double getBalance(HttpServletRequest request) {
//        String uid = (String) request.getAttribute("firebaseUid");
//
//        return balanceService.calculateBalance(uid);
//    }
}