package com.example.expensestracker;

import com.example.expensestracker.calendarevents.CalendarEvent;
import com.example.expensestracker.calendarevents.DeadlineEvent;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.HashMap;

public interface CalendarDataPass {
    public void onCalendarDataPassed(HashMap<Integer, HashMap<LocalDate, ArrayList<CalendarEvent>>> events, ArrayList<DeadlineEvent> deadlines);
}
