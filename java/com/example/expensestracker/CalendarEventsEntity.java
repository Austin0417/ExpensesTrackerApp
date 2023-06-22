package com.example.expensestracker;

import androidx.room.ColumnInfo;
import androidx.room.Entity;
import androidx.room.PrimaryKey;

@Entity(tableName="calendar_events")
public class CalendarEventsEntity {
    @PrimaryKey(autoGenerate = true)
    public int key;
    @ColumnInfo(name="day")
    public int day;
    @ColumnInfo(name="month")
    public int month;
    @ColumnInfo(name="year")
    public int year;
    @ColumnInfo(name="expense")
    public double expense;
    @ColumnInfo(name="income")
    public double income;
}
