package com.example.expensestracker.calendar;

import com.example.expensestracker.calendar.CalendarEvent;

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

    @Override
    public boolean equals(Object o) {
        DeadlineEvent deadline = (DeadlineEvent) o;
        return getMonth() == deadline.getMonth() && getDay() == deadline.getDay() && getYear() == deadline.getYear() && getAmount() == deadline.getAmount() && getInformation().equals(deadline.getInformation());
    }

    @Override
    public int hashCode() {
        final int prime = 31;
        int result = 1;
        result = prime * result + getMonth();
        result = prime * result + getDay();
        result = prime * result + getYear();
        long amountAsLong = Double.doubleToLongBits(getAmount());
        result = prime * result + (int) (amountAsLong ^ (amountAsLong >>> 32));
        result = prime * result + (getInformation() != null ? getInformation().hashCode() : 0);
        return result;
    }
}
