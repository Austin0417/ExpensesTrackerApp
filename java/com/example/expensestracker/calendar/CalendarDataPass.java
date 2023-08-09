package com.example.expensestracker.calendar;

import com.example.expensestracker.CalendarEventsEntity;
import com.example.expensestracker.calendar.CalendarEvent;
import com.example.expensestracker.calendar.DeadlineEvent;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

public interface CalendarDataPass {
    public void onCalendarDataPassed(HashMap<Integer, HashMap<LocalDate, ArrayList<CalendarEvent>>> events, ArrayList<DeadlineEvent> deadlines);
    public List<CalendarEventsEntity> getEventsInMonth(int month);
    public List<ExpenseCategory> getCategory(int category_id);
    public List<ExpenseCategory> getAllCurrentCategories();
}
