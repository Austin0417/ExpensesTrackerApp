package com.example.expensestracker.calendar;

import com.example.expensestracker.calendar.CalendarEvent;

import java.time.LocalDate;

public class IncomeEvent extends CalendarEvent {
    public IncomeEvent(double expenses, double income, LocalDate date) {
        super(expenses, income, date);
        setExpenses(0);
    }
    @Override
    public String getType() {
        return "income";
    }

    @Override
    public void setAmount(double amount) { setIncome(amount); }

    @Override
    public double getAmount() { return getIncome(); }
}
