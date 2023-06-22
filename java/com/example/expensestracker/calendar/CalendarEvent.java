package com.example.expensestracker.calendar;

import android.annotation.SuppressLint;

import java.time.LocalDate;
import java.util.Calendar;
import java.util.Date;

public class CalendarEvent {
    private String information;
    private double expenses;
    private double income;
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



