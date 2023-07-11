package com.example.expensestracker;

import androidx.lifecycle.LiveData;
import androidx.room.Dao;
import androidx.room.Delete;
import androidx.room.Insert;
import androidx.room.Query;
import androidx.room.Update;

import java.util.ArrayList;
import java.util.List;

@Dao
public interface DeadlineEventsDAO {
    @Insert
    void insert(DeadlineEventsEntity deadlineEvent);

    @Delete
    void delete(DeadlineEventsEntity deadlineEvent);

    @Update
    void update(DeadlineEventsEntity deadlineEvent);

    @Query("SELECT * FROM deadline_events")
    List<DeadlineEventsEntity> getDeadlineEvents();

    @Query("DELETE FROM deadline_events")
    public void clearDeadlineEvents();

    @Query("UPDATE deadline_events SET amount=:amount, information=:newInformation, hour=:hour, minute=:minute, hour_type=:hourType WHERE month=:month AND day=:day AND year=:year AND information=:previousInformation")
    public void updateDeadlineEvent(double amount, String newInformation, int month, int day, int year, int hour, int minute, int hourType, String previousInformation);

    @Query("DELETE FROM deadline_events WHERE amount=:amount AND information=:information AND month=:month AND day=:day AND year=:year")
    public void deleteDeadlineEvent(double amount, String information, int month, int day, int year);

    @Query("SELECT `key` FROM deadline_events WHERE amount=:amount AND information=:information AND month=:month AND day=:day AND year=:year")
    public int getUUID(double amount, String information, int month, int day, int year);
}
