package com.example.expensestracker.pie;

import java.util.List;

public class CSVData {
    private List<String[]> calendarEventEntries;
    private List<String[]> monthlyExpensesEntries;

    public CSVData(List<String[]> calendarEventEntries, List<String[]> monthlyExpensesEntries) {
        this.calendarEventEntries = calendarEventEntries;
        this.monthlyExpensesEntries = monthlyExpensesEntries;
    }

    public List<String[]> getCalendarEventEntries() { return calendarEventEntries; }
    public List<String[]> getMonthlyExpensesEntries() { return monthlyExpensesEntries; }
}
