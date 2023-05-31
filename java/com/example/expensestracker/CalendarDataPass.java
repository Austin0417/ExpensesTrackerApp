package com.example.expensestracker;

import java.util.ArrayList;
import java.util.HashMap;

public interface CalendarDataPass {
    public void onCalendarDataPassed(HashMap<Integer, ArrayList<CalendarEvent>> events);
}
