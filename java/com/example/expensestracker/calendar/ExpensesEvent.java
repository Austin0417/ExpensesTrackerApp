package com.example.expensestracker.calendar;

import com.example.expensestracker.calendar.CalendarEvent;

import java.time.LocalDate;

public class ExpensesEvent extends CalendarEvent {
    public ExpensesEvent(double expenses, double income, LocalDate date) {
        super(expenses, income, date);
        setIncome(0);
    }
    @Override
    public String getType() {
        return "expenses";
    }

    @Override
    public void setAmount(double amount) { setExpenses(amount); }

    @Override
    public double getAmount() { return getExpenses(); }
}
