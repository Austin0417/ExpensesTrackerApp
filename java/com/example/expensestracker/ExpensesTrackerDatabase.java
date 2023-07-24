package com.example.expensestracker;

import androidx.room.Database;
import androidx.room.RoomDatabase;

import com.example.expensestracker.monthlyinfo.MonthlyExpense;
import com.example.expensestracker.monthlyinfo.MonthlyExpenseDAO;
import com.example.expensestracker.monthlyinfo.MonthlyInfoEntity;

@Database(entities={MonthlyInfoEntity.class, CalendarEventsEntity.class, DeadlineEventsEntity.class, MonthlyExpense.class}, version=1)
public abstract class ExpensesTrackerDatabase  extends RoomDatabase {
    public abstract MonthlyInfoDAO monthlyInfoDAO();
    public abstract CalendarEventsDAO calendarEventsDAO();
    public abstract DeadlineEventsDAO deadlineEventsDAO();
    public abstract MonthlyExpenseDAO monthlyExpenseDAO();
}
