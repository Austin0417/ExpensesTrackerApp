package com.example.expensestracker.monthlyinfo;

import java.util.List;

public interface PassMonthlyData {
    public void onDataPassed(double expenses, double income);
    public void passMonthlyExpenseList(List<MonthlyExpense> monthlyExpenses);
    public void createExpense(MonthlyExpense expense);
    public void openExpenseDialog(MonthlyExpense expense);
    public void updateExpense(String newDescription, double newAmount, MonthlyExpense previousExpense, int index);
    public void deleteExpense(int index);
}
