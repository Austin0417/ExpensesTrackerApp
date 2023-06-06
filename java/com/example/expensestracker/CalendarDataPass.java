package com.example.expensestracker;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;

public interface CalendarDataPass {
    public void onCalendarDataPassed(HashMap<Integer, HashMap<LocalDate, ArrayList<CalendarEvent>>> events);
}
