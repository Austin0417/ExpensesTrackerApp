package com.example.expensestracker;

import java.time.LocalDate;
import java.util.Date;

public class IncomeEvent extends CalendarEvent {
    public IncomeEvent(double expenses, double income, LocalDate date) {
        super(expenses, income, date);
        setExpenses(0);
    }
    @Override
    public String getType() {
        return "income";
    }
}
