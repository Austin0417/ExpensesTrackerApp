package com.example.expensestracker.database;

import androidx.room.Database;
import androidx.room.RoomDatabase;

import com.example.expensestracker.monthlyinfo.MonthlyInfoEntity;

@Database(entities={MonthlyInfoEntity.class, CalendarEventsEntity.class, DeadlineEventsEntity.class}, version=1)
public abstract class ExpensesTrackerDatabase  extends RoomDatabase {
    public abstract MonthlyInfoDAO monthlyInfoDAO();
    public abstract CalendarEventsDAO calendarEventsDAO();
    public abstract DeadlineEventsDAO deadlineEventsDAO();
}
