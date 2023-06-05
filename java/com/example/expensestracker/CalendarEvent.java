package com.example.expensestracker;

import java.time.LocalDate;
import java.util.Calendar;
import java.util.Date;

public class CalendarEvent {
    private String information;
    private double expenses;
    private double income;
    Calendar calendar;
    private Date date;
    boolean isMarked = false;

    public CalendarEvent(double expenses, double income, Date date) {
        this.expenses = expenses;
        this.income = income;
        this.date = date;
        calendar = Calendar.getInstance();
        calendar.setTime(this.date);
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

    public int getMonth() {
        return calendar.get(Calendar.MONTH);
    }

    public int getDay() {
        return calendar.get(Calendar.DAY_OF_MONTH);
    }

    public int getYear() {
        return calendar.get(Calendar.YEAR);
    }
    public void setMarked(boolean status) { isMarked = status; }
    public boolean isMarked() { return isMarked; }
}
