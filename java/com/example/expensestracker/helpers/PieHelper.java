package com.example.expensestracker.helpers;

import com.example.expensestracker.calendar.CalendarEvent;
import com.example.expensestracker.calendar.ExpenseCategory;
import com.example.expensestracker.calendar.ExpensesEvent;
import com.example.expensestracker.pie.CostMapping;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Random;

public class PieHelper {
    public static CostMapping initializeCostsPerCategoryMapping(List<CalendarEvent> events) {
        Map<ExpenseCategory, Double> res = new HashMap<ExpenseCategory, Double>();
        Map<ExpenseCategory, List<ExpensesEvent>> categoryEventMapping = new HashMap<ExpenseCategory, List<ExpensesEvent>>();
        for (CalendarEvent event : events) {
            if (event instanceof ExpensesEvent) {
                ExpenseCategory category = ((ExpensesEvent) event).getCategory();
                if (res.containsKey(category)) {
                    double amount = res.get(category);
                    res.put(category, amount + event.getAmount());
                    categoryEventMapping.get(category).add((ExpensesEvent) event);
                } else {
                    res.put(category, event.getAmount());
                    List<ExpensesEvent> expenseEvents = new ArrayList<ExpensesEvent>();
                    expenseEvents.add((ExpensesEvent) event);
                    categoryEventMapping.put(category, expenseEvents);
                }
            }
        }
        return new CostMapping(res, categoryEventMapping);
    }

    private static int selectRandomColor(int[] colors) {
        Random random = new Random();
        int index = random.nextInt(colors.length);
        return colors[index];
    }
    public static void setDatasetColors(int[] colorsDataset, int[] selectedColors) {
        Map<Integer, Boolean> presentColors = new HashMap<Integer, Boolean>();
        int i = 0;
        while (i < selectedColors.length) {
            int selectedColor = selectRandomColor(colorsDataset);
            if (presentColors.containsKey(selectedColor)) {
                continue;
            } else {
                selectedColors[i] = selectedColor;
                presentColors.put(selectedColor, true);
                i++;
            }
        }
    }

    public static String listEventsAsString(List<ExpensesEvent> events) {
        String res = "";
        for (ExpensesEvent event : events) {
            res += event.toString() + "\n";
        }
        return res;
    }
}
