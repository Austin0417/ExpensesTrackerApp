package com.example.expensestracker.calendarevents;

import com.example.expensestracker.calendarevents.CalendarEvent;

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
}
