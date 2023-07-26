package com.example.expensestracker.helpers;

import com.example.expensestracker.calendar.CalendarEvent;
import com.example.expensestracker.calendar.ExpenseCategory;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

public interface ExpenseCategoryCallback {
    public List<CalendarEvent> canDeleteCategory(HashMap<Integer, HashMap<LocalDate, ArrayList<CalendarEvent>>> totalEvents, ExpenseCategory category);
}
