package com.example.expensestracker.calendarevents;

import com.example.expensestracker.calendarevents.CalendarEvent;

import java.time.LocalDate;
import java.util.UUID;

public class DeadlineEvent extends CalendarEvent {
    private int id;
    public DeadlineEvent(double expenses, double income, LocalDate date, String information) {
        super(expenses, income, date);
        setIncome(0);
        setInformation(information);
        id = UUID.randomUUID().hashCode();
    }
    public int getId() { return id; }
}
