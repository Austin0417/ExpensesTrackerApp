package com.example.expensestracker.pie;

import com.example.expensestracker.calendar.ExpenseCategory;
import com.example.expensestracker.calendar.ExpensesEvent;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class CostMapping {
    private Map<ExpenseCategory, Double> categoryToAmountMapping = new HashMap<ExpenseCategory, Double>();
    private Map<ExpenseCategory, List<ExpensesEvent>> categoryToEventMapping = new HashMap<ExpenseCategory, List<ExpensesEvent>>();
    private double additionalIncomeFromCalendar = 0;

    public CostMapping() {}

    public CostMapping(Map<ExpenseCategory, Double> categoryToAmountMapping,
                       Map<ExpenseCategory, List<ExpensesEvent>> categoryToEventMapping) {
        this.categoryToAmountMapping = categoryToAmountMapping;
        this.categoryToEventMapping = categoryToEventMapping;
    }

    public Map<ExpenseCategory, Double> getCategoryToAmountMapping() { return categoryToAmountMapping; }
    public Map<ExpenseCategory, List<ExpensesEvent>> getCategoryToEventMapping() { return categoryToEventMapping; }
    public double getAdditionalIncomeFromCalendar() { return additionalIncomeFromCalendar; }
    public void setAdditionalIncomeFromCalendar(double additionalIncome) { additionalIncomeFromCalendar = additionalIncome; }
    public void setCategoryToAmountMapping(Map<ExpenseCategory, Double> categoryToAmountMapping) { this.categoryToAmountMapping = categoryToAmountMapping; }
    public void setCategoryToEventMapping(Map<ExpenseCategory, List<ExpensesEvent>> categoryToEventMapping) { this.categoryToEventMapping = categoryToEventMapping; }
}
