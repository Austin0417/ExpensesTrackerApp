package com.example.expensestracker.calendar;

public interface EditEvent {
    public void sendCalendarEventDate(int month, int year, int day);
    public void modifyCalendarEvent(CalendarEvent targetedEvent, double amount);
    public void deleteCalendarEvent(CalendarEvent targetedEvent);

    public void sendDeadlineEventDate(double amount, String information, int month, int year, int day);
    public void modifyDeadlineEvent(DeadlineEvent targetDeadline, String previousInformation);
    public void deleteDeadlineEvent(int index, DeadlineEvent targetDeadline);
}
