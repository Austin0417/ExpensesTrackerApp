package com.example.expensestracker.calendarevents;

import com.example.expensestracker.calendarevents.CalendarEvent;

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
}
