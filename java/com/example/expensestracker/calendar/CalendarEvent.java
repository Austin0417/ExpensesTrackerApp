package com.example.expensestracker.calendar;

import android.annotation.SuppressLint;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;

// Base class for both ExpensesEvent and IncomeEvent
public abstract class CalendarEvent implements Comparable<CalendarEvent> {
    public static int AM = 1;
    public static int PM = 2;

    // User input information for the CalendarEvent
    private String information;

    // User input expenses for the CalendarEvent
    private double expenses;

    // User input income for the CalendarEvent
    private double income;

    // Date of the CalendarEvent
    private LocalDate date;
    boolean isMarked = false;

    public CalendarEvent(double expenses, double income, LocalDate date) {
        this.expenses = expenses;
        this.income = income;
        this.date = date;
    }

    public String getInformation() {
        return information;
    }

    public double getExpenses() {
        return expenses;
    }

    public double getIncome() {
        return income;
    }

    public void setInformation(String information) { this.information = information; }

    public void setExpenses(double expenses) {
        this.expenses = expenses;
    }

    public void setIncome(double income) {
        this.income = income;
    }

    public abstract double getAmount();

    public abstract void setAmount(double amount);

    public boolean isExpense() { return this instanceof ExpensesEvent; }
    public boolean isIncome() { return this instanceof IncomeEvent; }

    @SuppressLint("NewAPI")
    public int getMonth() {
        return date.getMonthValue();
    }

    @SuppressLint("NewAPI")
    public int getDay() {
        return date.getDayOfMonth();
    }

    @SuppressLint("NewAPI")
    public int getYear() {
        return date.getYear();
    }
    public void setMarked(boolean status) { isMarked = status; }
    public boolean isMarked() { return isMarked; }
    public String getType() {
        return "calendarevent";
    }

    // Helper method for obtaining all of the LocalDates with an event in the month, returns an ArrayList<LocalDate>
    public static ArrayList<LocalDate> getDatesWithEvents(HashMap<LocalDate, ArrayList<CalendarEvent>> eventsInMonth) {
        ArrayList<LocalDate> dates = new ArrayList<LocalDate>();
        for (Map.Entry<LocalDate, ArrayList<CalendarEvent>> entry: eventsInMonth.entrySet()) {
            dates.add(entry.getKey());
        }
        return dates;
    }

    @Override
    // Implementation of Comparable interface method, used for sorting an ArrayList of CalendarEvents in chronological order
    public int compareTo(CalendarEvent calendarEvent) {
        if (getDay() - calendarEvent.getDay() != 0) {
            return getDay() - calendarEvent.getDay();
        }
        if (getMonth() - calendarEvent.getMonth() != 0) {
            return getMonth() - calendarEvent.getMonth();
        }
        return getYear() - calendarEvent.getYear();
    }
}



