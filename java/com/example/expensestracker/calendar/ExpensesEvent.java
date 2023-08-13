package com.example.expensestracker.calendar;

import androidx.room.Entity;
import androidx.room.PrimaryKey;

import com.example.expensestracker.calendar.CalendarEvent;

import java.time.LocalDate;
import java.util.Objects;

public class ExpensesEvent extends CalendarEvent {
    private ExpenseCategory category = null;
    private boolean notificationsEnabled = false;

    public ExpensesEvent(double expenses, double income, LocalDate date) {
        super(expenses, income, date);
        setIncome(0);
    }
    @Override
    public String getType() {
        return "expenses";
    }

    @Override
    public void setAmount(double amount) { setExpenses(amount); }

    @Override
    public double getAmount() { return getExpenses(); }

    public boolean isNotificationsEnabled() { return notificationsEnabled; }

    public void setNotificationsStatus(boolean status) { notificationsEnabled = status; }

    public ExpenseCategory getCategory() { return category; }

    public void setCategory(ExpenseCategory category) { this.category = category; }

    @Override
    public String toString() {
        if (category != null) {
            return "" + getDate().getMonthValue() + "/" + getDate().getDayOfMonth() + "/" + getDate().getYear()
                    + ": -$" + getAmount();
        } else {
            return "" + getDate().getMonthValue() + "/" + getDate().getDayOfMonth() + "/" + getDate().getYear()
                    + ": Additional Expense - $" + getAmount();
        }
    }

    @Override
    public boolean equals(Object o) {
        ExpensesEvent event = (ExpensesEvent) o;
        return category.equals(event.getCategory()) && getAmount() == event.getAmount()
                && getDate().equals(event.getDate());
    }

    @Override
    public int hashCode() {
        return Objects.hash(getCategory(), getExpenses(), getIncome(), getDate());
    }
}
