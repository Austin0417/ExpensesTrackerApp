package com.example.expensestracker.calendar;

import android.annotation.SuppressLint;

import java.time.LocalDate;
import java.util.Calendar;
import java.util.Date;

public class CalendarEvent {
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

    public void setExpenses(double expenses) {
        this.expenses = expenses;
    }

    public void setIncome(double income) {
        this.income = income;
    }

    public void updateAmount(double newAmount) {
        if (this instanceof ExpensesEvent) {
            setExpenses(newAmount);
        } else {
            setIncome(newAmount);
        }
    }
    public double getAmount() {
        if (this instanceof ExpensesEvent || this instanceof DeadlineEvent) {
            return getExpenses();
        } else {
            return getIncome();
        }
    }

    public void setAmount(double amount) {
        if (this instanceof ExpensesEvent || this instanceof DeadlineEvent) {
            setExpenses(amount);
        } else {
            setIncome(amount);
        }
    }

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
    public void setInformation(String information) {
        this.information = information;
    }
}



