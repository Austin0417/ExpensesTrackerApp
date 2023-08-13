package com.example.expensestracker.calendar;

import androidx.room.Dao;
import androidx.room.Delete;
import androidx.room.Insert;
import androidx.room.Query;

@Dao
public interface ExpenseNotificationDAO {
    @Insert
    public void insert(ExpenseNotification expenseNotification);

    @Delete
    public void delete(ExpenseNotification expenseNotification);

    @Query("SELECT * FROM ExpenseNotification WHERE eventHashCode=:hashCode LIMIT 1")
    public ExpenseNotification getExpenseNotification(int hashCode);

    @Query("SELECT id FROM ExpenseNotification WHERE eventHashCode=:hashCode AND daysBeforeAlert=:daysBeforeAlert")
    public int getId(int hashCode, int daysBeforeAlert);

    @Query("UPDATE ExpenseNotification SET eventHashCode=:newHashCode WHERE id=:id")
    public void updateHashCode(int newHashCode, int id);
}
