package com.example.expensestracker;

import java.time.LocalDate;
import java.util.Calendar;
import java.util.Date;

public class CalendarEvent {
    private String information;
    private double expenses;
    private double income;

    private Date date;

    public CalendarEvent(double expenses, double income, Date date) {
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
}
