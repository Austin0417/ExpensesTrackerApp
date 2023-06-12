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
    LiveData<List<DeadlineEventsEntity>> getDeadlineEvents();

    @Query("DELETE FROM deadline_events")
    public void clearDeadlineEvents();
}
