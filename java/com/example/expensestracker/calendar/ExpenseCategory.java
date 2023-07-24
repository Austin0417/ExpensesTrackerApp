package com.example.expensestracker.calendar;


import androidx.room.ColumnInfo;
import androidx.room.Entity;
import androidx.room.ForeignKey;
import androidx.room.PrimaryKey;

import java.util.Objects;

@Entity
public class ExpenseCategory {
    @PrimaryKey(autoGenerate = true)
    public int category_number;

    @ColumnInfo
    public int id_of_expense;

    @ColumnInfo
    private String name;

    public ExpenseCategory(String name) { this.name = name; }

    public String getName() { return name; }

    public void setName(String name) { this.name = name; }

    public int getId_of_expense() { return id_of_expense; }

    public void setId_of_expense(int id) { id_of_expense = id; }

    @Override
    public boolean equals(Object o) {
        ExpenseCategory comparedCategory = (ExpenseCategory) o;
        return getName().equals(comparedCategory.getName());
    }

    @Override
    public int hashCode() {
        return Objects.hash(getName());
    }
}
