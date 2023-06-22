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
}
