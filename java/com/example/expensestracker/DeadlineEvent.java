package com.example.expensestracker;

import java.time.LocalDate;

public class DeadlineEvent extends CalendarEvent {

    public DeadlineEvent(double expenses, double income, LocalDate date, String information) {
        super(expenses, income, date);
        setIncome(0);
        setInformation(information);

    }
}
