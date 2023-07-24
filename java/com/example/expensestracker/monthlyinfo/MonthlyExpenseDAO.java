package com.example.expensestracker.monthlyinfo;

import androidx.room.Dao;
import androidx.room.Delete;
import androidx.room.Insert;
import androidx.room.Query;

import java.util.List;

@Dao
public interface MonthlyExpenseDAO {
    @Insert
    public void insert(MonthlyExpense expense);

    @Delete
    public void delete(MonthlyExpense expense);

    @Query("SELECT * FROM MonthlyExpense")
    public List<MonthlyExpense> getMonthlyExpenses();

    @Query("DELETE FROM MonthlyExpense")
    public void clearMonthlyExpenses();

    @Query("DELETE FROM MonthlyExpense WHERE description=:description AND amount=:amount")
    public void deleteExpense(String description, double amount);

    @Query("UPDATE MonthlyExpense SET description=:new_description, amount=:new_amount WHERE description=:prev_description AND amount=:prev_amount")
    public void updateExpense(String new_description, double new_amount, String prev_description, double prev_amount);
}
