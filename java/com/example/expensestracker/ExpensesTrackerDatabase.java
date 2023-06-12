package com.example.expensestracker;

import androidx.room.Database;
import androidx.room.RoomDatabase;
@Database(entities={MonthlyInfoEntity.class, CalendarEventsEntity.class, DeadlineEventsEntity.class}, version=1)
public abstract class ExpensesTrackerDatabase  extends RoomDatabase {
    public abstract MonthlyInfoDAO monthlyInfoDAO();
    public abstract CalendarEventsDAO calendarEventsDAO();
    public abstract DeadlineEventsDAO deadlineEventsDAO();
}
