package com.example.expensestracker;

import java.time.LocalDate;
import java.util.Date;

public class ExpensesEvent extends CalendarEvent {
    public ExpensesEvent(double expenses, double income, LocalDate date) {
        super(expenses, income, date);
        setIncome(0);
    }
    @Override
    public String getType() {
        return "expenses";
    }
}
