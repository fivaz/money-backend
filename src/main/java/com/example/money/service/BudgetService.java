package com.example.money.service;

import java.time.LocalDate;
import java.time.YearMonth;
import java.time.temporal.ChronoUnit;

public class BudgetService {

    public static int monthsBetweenInclusive(LocalDate start, YearMonth end) {
        if (start == null || end == null) return 0;

        YearMonth startMonth = YearMonth.from(start);
        if (startMonth.isAfter(end)) return 0;

        return (int) ChronoUnit.MONTHS.between(startMonth, end) + 1;
    }
}
