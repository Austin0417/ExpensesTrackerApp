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
    private String name;

    public ExpenseCategory(String name) { this.name = name; }

    public String getName() { return name; }

    public void setName(String name) { this.name = name; }

    public void setCategoryNumber(int category_number) { this.category_number = category_number; }

    @Override
    public boolean equals(Object o) {
        ExpenseCategory comparedCategory = (ExpenseCategory) o;
        return getName().equals(comparedCategory.getName());
    }

    @Override
    public int hashCode() {
        return Objects.hash(getName());
    }

    public int getCategoryNumber() { return category_number; }

    @Override
    public String toString() {
        return getName();
    }

    public ExpenseCategory copy() {
        ExpenseCategory result = new ExpenseCategory(getName());
        result.setCategoryNumber(getCategoryNumber());
        return result;
    }
}
