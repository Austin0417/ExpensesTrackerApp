package com.example.expensestracker.calendar;

import androidx.room.ColumnInfo;
import androidx.room.Entity;
import androidx.room.PrimaryKey;

@Entity
public class ExpenseNotification {
    @PrimaryKey(autoGenerate = true)
    private int id;

    // Int variable to store and associate each ExpenseNotification with a specific ExpensesEvent using it's hashcode
    // The hashCode will be used to query the id for a specific ExpenseNotification within the ExpenseNotification table
    @ColumnInfo
    private int eventHashCode;

    @ColumnInfo
    private int daysBeforeAlert;

    public ExpenseNotification(int eventHashCode, int daysBeforeAlert) {
        this.eventHashCode = eventHashCode;
        this.daysBeforeAlert = daysBeforeAlert;
    }

    public int getId() { return id; }
    public void setId(int id) { this.id = id; }
    public int getEventHashCode() { return eventHashCode; }
    public int getDaysBeforeAlert() { return daysBeforeAlert; }
    public void setEventHashCode(int hashCode) { eventHashCode = hashCode; }
    public void setDaysBeforeAlert(int days) { daysBeforeAlert = days; }
}
