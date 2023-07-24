package com.example.expensestracker.helpers;

import com.example.expensestracker.monthlyinfo.MonthlyExpense;

import java.util.List;

public class MonthlyInfoHelper {
    public static void populateDataset(String[] dataset, List<MonthlyExpense> monthlyExpenses) {
        for (int i = 0; i < monthlyExpenses.size(); i++) {
            dataset[i] = monthlyExpenses.get(i).getDescription() + ": $" + monthlyExpenses.get(i).getAmount();
        }
    }
}
