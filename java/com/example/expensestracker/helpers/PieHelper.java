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
    // Looping through the list of all CalendarEvents in the current month, to
    // create two Maps which keep key-value pairs of each ExpenseCategory and their total amount, as well as each ExpenseCategory and all of the events under the category
    public static CostMapping initializeCostsPerCategoryMapping(List<CalendarEvent> events) {
        Map<ExpenseCategory, Double> categoryToAmountMapping = new HashMap<ExpenseCategory, Double>();
        Map<ExpenseCategory, List<ExpensesEvent>> categoryEventMapping = new HashMap<ExpenseCategory, List<ExpensesEvent>>();
        CostMapping costMapping = new CostMapping();
        for (CalendarEvent event : events) {
            if (event instanceof ExpensesEvent) {
                ExpenseCategory category = ((ExpensesEvent) event).getCategory();
                if (categoryToAmountMapping.containsKey(category)) {
                    double amount = categoryToAmountMapping.get(category);
                    categoryToAmountMapping.put(category, amount + event.getAmount());
                    categoryEventMapping.get(category).add((ExpensesEvent) event);
                } else {
                    categoryToAmountMapping.put(category, event.getAmount());
                    List<ExpensesEvent> expenseEvents = new ArrayList<ExpensesEvent>();
                    expenseEvents.add((ExpensesEvent) event);
                    categoryEventMapping.put(category, expenseEvents);
                }
            } else {
                costMapping.setAdditionalIncomeFromCalendar(costMapping.getAdditionalIncomeFromCalendar() + event.getAmount());
            }
        }
        costMapping.setCategoryToAmountMapping(categoryToAmountMapping);
        costMapping.setCategoryToEventMapping(categoryEventMapping);
        return costMapping;
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
