package com.example.expensestracker.monthlyinfo;

import androidx.room.ColumnInfo;
import androidx.room.Entity;
import androidx.room.PrimaryKey;

import java.util.Objects;

@Entity
public class MonthlyExpense {
    @PrimaryKey(autoGenerate = true)
    public int key;

    @ColumnInfo(name="description")
    private String description;

    @ColumnInfo(name="amount")
    private double amount;

    public MonthlyExpense(String description, double amount) {
        this.description = description;
        this.amount = amount;
    }

    public String getDescription() { return description; }

    public double getAmount() { return amount; }

    public void setDescription(String description) { this.description = description; }

    public void setAmount(double amount) { this.amount = amount; }

    @Override
    public String toString() {
        return "Description=" + getDescription() + "\tAmount=" + getAmount();
    }

    @Override
    public boolean equals(Object o) {
        MonthlyExpense comparedExpense = (MonthlyExpense) o;
        return getDescription().equals(comparedExpense.getDescription()) && getAmount() == comparedExpense.getAmount();
    }

    @Override
    public int hashCode() {
        return Objects.hash(getDescription(), getAmount());
    }

}
