package com.example.expensestracker;

import androidx.lifecycle.LiveData;
import androidx.room.Dao;
import androidx.room.Delete;
import androidx.room.Insert;
import androidx.room.Query;
import androidx.room.Update;

import java.util.List;

@Dao
public interface CalendarEventsDAO {
    @Insert
    void insert(CalendarEventsEntity calendarEvent);

    @Delete
    void delete(CalendarEventsEntity calendarEvent);

    @Update
    void update(CalendarEventsEntity calendarEvent);

    @Query("SELECT * FROM calendar_events")
    LiveData<List<CalendarEventsEntity>> getCalendarEvents();

    @Query("DELETE FROM calendar_events")
    public void clearCalendarEvents();

    @Query("UPDATE calendar_events SET expense=:newExpense WHERE month=:month AND day=:day AND year=:year")
    public void updateExpenseEvent(double newExpense, int month, int day, int year);

    @Query("UPDATE calendar_events SET income=:newIncome WHERE month=:month AND day=:day AND year=:year")
    public void updateIncomeEvent(double newIncome, int month, int day, int year);

    @Query("DELETE FROM calendar_events WHERE day=:day AND month=:month AND year=:year AND expense=:amount")
    public void deleteExpenseEvent(double amount, int day, int month, int year);

    @Query("DELETE FROM calendar_events WHERE day=:day AND month=:month AND year=:year AND income=:amount")
    public void deleteIncomeEvent(double amount, int day, int month, int year);
}
