package com.example.expensestracker;

import androidx.room.ColumnInfo;
import androidx.room.Entity;
import androidx.room.PrimaryKey;

@Entity(tableName="deadline_events")
public class DeadlineEventsEntity {
    @PrimaryKey(autoGenerate = true)
    public int key;

    @ColumnInfo(name="month")
    public int month;

    @ColumnInfo(name="day")
    public int day;

    @ColumnInfo(name="year")
    public int year;

    @ColumnInfo(name="information")
    public String information;

    @ColumnInfo(name="amount")
    public double expense;

    @ColumnInfo(name="hour")
    public int hour;

    @ColumnInfo(name="minute")
    public int minute;

    @ColumnInfo(name="hour_type")
    public int hour_type;
}
