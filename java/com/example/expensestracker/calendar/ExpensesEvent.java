package com.example.expensestracker.calendar;

import com.example.expensestracker.calendar.CalendarEvent;

import java.time.LocalDate;

public class ExpensesEvent extends CalendarEvent {
    private ExpenseCategory category = null;

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

    public ExpenseCategory getCategory() { return category; }

    public void setCategory(ExpenseCategory category) { this.category = category; }

    @Override
    public String toString() {
        if (category != null) {
            return "" + getDate().getMonthValue() + "/" + getDate().getDayOfMonth() + "/" + getDate().getYear()
                    + ": " + getCategory().getName() + " - $" + getAmount();
        } else {
            return "" + getDate().getMonthValue() + "/" + getDate().getDayOfMonth() + "/" + getDate().getYear()
                    + ": Additional Expense - $" + getAmount();
        }
    }
}
