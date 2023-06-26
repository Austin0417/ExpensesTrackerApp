package com.example.expensestracker.calendar;

import java.util.ArrayList;

public interface EditEvent {
    public void sendDate(int month, int year, int day);
    public void newAmount(CalendarEvent targetedEvent, double amount);
    public void deleteDate(CalendarEvent targetedEvent);
}
