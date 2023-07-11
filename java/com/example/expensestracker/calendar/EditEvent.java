package com.example.expensestracker.calendar;

// Interface for creating, updating, and deleting operations involving CalendarEvents
public interface EditEvent {
    public void sendCalendarEventDate(int month, int year, int day);
    public void modifyCalendarEvent(CalendarEvent targetedEvent, double amount);
    public void deleteCalendarEvent(CalendarEvent targetedEvent);

    public void sendDeadlineEventDate(double amount, String information, int month, int year, int day, int hour, int minute, String hourType);
    public void modifyDeadlineEvent(DeadlineEvent targetDeadline, String previousInformation, double previousAmount, int index);

    public void deleteDeadlineEvent(int index, DeadlineEvent targetDeadline);
}
