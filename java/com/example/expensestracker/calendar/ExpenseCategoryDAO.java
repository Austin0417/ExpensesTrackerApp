package com.example.expensestracker.calendar;

import androidx.room.Dao;
import androidx.room.Delete;
import androidx.room.Insert;
import androidx.room.Query;

import java.util.List;

@Dao
public interface ExpenseCategoryDAO {
    @Insert
    public void createCategory(ExpenseCategory category);

    @Delete
    public void deleteCategory(ExpenseCategory category);

    @Query("DELETE FROM ExpenseCategory")
    public void clearCategories();

    @Query("SELECT * FROM ExpenseCategory")
    public List<ExpenseCategory> getAllCategories();

    @Query("SELECT * FROM ExpenseCategory WHERE category_number=:id")
    public List<ExpenseCategory> getCategory(int id);

    @Query("SELECT * FROM ExpenseCategory WHERE name=:category_name")
    public List<ExpenseCategory> getCategory(String category_name);

    @Query("SELECT category_number FROM ExpenseCategory WHERE name=:category_name")
    public int getCategoryId(String category_name);

    @Query("DELETE FROM ExpenseCategory WHERE name=:category_name")
    public void deleteCategory(String category_name);
}
