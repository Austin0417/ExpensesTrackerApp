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
public interface MonthlyInfoDAO {
    @Insert
    void insert(MonthlyInfoEntity monthlyInfo);

    @Delete
    void delete(MonthlyInfoEntity monthlyInfo);

    @Update
    void update(MonthlyInfoEntity monthlyInfo);

    @Query("SELECT * FROM monthly_info")
    public LiveData<List<MonthlyInfoEntity>> getMonthlyInfo();

    @Query("DELETE FROM monthly_info")
    public void clearMonthlyInfo();
}