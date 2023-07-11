package com.example.expensestracker.calendar;

import com.example.expensestracker.calendar.CalendarEvent;

import java.time.LocalDate;
import java.util.UUID;

public class DeadlineEvent extends CalendarEvent {
    private int id;
    private int hour;
    private int minute;
    private int am_or_pm;
    public DeadlineEvent(double expenses, double income, LocalDate date, String information) {
        super(expenses, income, date);
        setIncome(0);
        setInformation(information);
        id = UUID.randomUUID().hashCode();
    }
    public int getId() { return id; }

    public void setHour(int hour) { this.hour = hour; }
    public void setMinute(int minute) { this.minute = minute; }
    public int getMinute() { return minute; }
    public void setAmOrPm(int choice) { am_or_pm = choice; }
    public int getHour() { return hour; }
    public int getAmOrPm() { return am_or_pm; }

    @Override
    public boolean equals(Object o) {
        DeadlineEvent deadline = (DeadlineEvent) o;
        return getMonth() == deadline.getMonth()
                && getDay() == deadline.getDay()
                && getYear() == deadline.getYear()
                && getAmount() == deadline.getAmount()
                && getInformation().equals(deadline.getInformation())
                && getHour() == deadline.getHour()
                && getMinute() == deadline.getMinute()
                && getAmOrPm() == deadline.getAmOrPm();
    }

    @Override
    public void setAmount(double amount) {
        setExpenses(amount);
    }

    @Override
    public double getAmount() { return getExpenses(); }

    @Override
    public int hashCode() {
        final int prime = 31;
        int result = 1;
        result = prime * result + getMonth();
        result = prime * result + getDay();
        result = prime * result + getYear();
        result = prime * result + getHour();
        result = prime * result + getMinute();
        result = prime * result + getAmOrPm();
        long amountAsLong = Double.doubleToLongBits(getAmount());
        result = prime * result + (int) (amountAsLong ^ (amountAsLong >>> 32));
        result = prime * result + (getInformation() != null ? getInformation().hashCode() : 0);
        return result;
    }
}
