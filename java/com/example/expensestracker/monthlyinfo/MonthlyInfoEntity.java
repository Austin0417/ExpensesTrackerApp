package com.example.expensestracker.monthlyinfo;

import androidx.room.ColumnInfo;
import androidx.room.Entity;
import androidx.room.PrimaryKey;

@Entity(tableName="monthly_info")
public class MonthlyInfoEntity {
    @PrimaryKey(autoGenerate = true)
    public int key;
    @ColumnInfo(name="monthly_expenses")
    public double expenses;
    @ColumnInfo(name="monthly_income")
    public double income;
}
