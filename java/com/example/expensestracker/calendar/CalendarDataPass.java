package com.example.expensestracker.calendar;

import com.example.expensestracker.calendar.CalendarEvent;
import com.example.expensestracker.calendar.DeadlineEvent;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.HashMap;

public interface CalendarDataPass {
    public void onCalendarDataPassed(HashMap<Integer, HashMap<LocalDate, ArrayList<CalendarEvent>>> events, ArrayList<DeadlineEvent> deadlines);
}
