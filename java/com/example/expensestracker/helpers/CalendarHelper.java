package com.example.expensestracker.helpers;

import android.content.Context;
import android.widget.Toast;

import com.example.expensestracker.calendar.CalendarEvent;
import com.example.expensestracker.calendar.DeadlineEvent;
import com.example.expensestracker.calendar.ExpenseCategory;
import com.example.expensestracker.calendar.ExpensesEvent;
import com.example.expensestracker.calendar.IncomeEvent;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;
import java.util.stream.Stream;

public class CalendarHelper {
    public static void insertEvent(HashMap<Integer, HashMap<LocalDate, ArrayList<CalendarEvent>>> monthlyMapping, CalendarEvent event, Context context) {
            if (monthlyMapping.containsKey(event.getMonth())) {
                HashMap<LocalDate, ArrayList<CalendarEvent>> dayMapping = monthlyMapping.get(event.getMonth());

                // There are already some events associated with this particular day
                if (dayMapping.containsKey(event.getDate())) {
                    ArrayList<CalendarEvent> eventsOnDay = monthlyMapping.get(event.getMonth()).get(event.getDate());
                    if (eventsOnDay.size() >= 10) {
                        // If the user has 10 or more events on a particular date, do not add events any further
                        Toast.makeText(context, "Event limit for a particular day has been reached!", Toast.LENGTH_LONG).show();
                        return;
                    }
                    eventsOnDay.add(event);

                    // First event associated with this particular day, so we have to initialize the array in the nested hashmap
                } else {
                    ArrayList<CalendarEvent> events = new ArrayList<CalendarEvent>();
                    events.add(event);
                    dayMapping.put(event.getDate(), events);
                    monthlyMapping.put(event.getMonth(), dayMapping);
                }

                // No events in the current month yet, initialize the hashmaps and array
            } else {
                HashMap<LocalDate, ArrayList<CalendarEvent>> eventsOnDay = new HashMap<LocalDate, ArrayList<CalendarEvent>>();
                ArrayList<CalendarEvent> events = new ArrayList<CalendarEvent>();
                events.add(event);
                eventsOnDay.put(event.getDate(), events);
                monthlyMapping.put(event.getMonth(), eventsOnDay);
            }
    }

    public static void insertEvent(ArrayList<DeadlineEvent> deadlines, DeadlineEvent event, Context context) {
        Calendar calendar = Calendar.getInstance();
        calendar.set(event.getYear(), event.getMonth() - 1, event.getDay());

        // Check for the case where the user attempts to add a deadline for a past date
        if (calendar.getTimeInMillis() - System.currentTimeMillis() < 0) {
            Toast.makeText(context, "Cannot set a deadline in the past!", Toast.LENGTH_LONG).show();
            return;
        }
        deadlines.add(event);
        Toast.makeText(context, "Successfully set deadline for " + event.getMonth() + "/" + event.getDay() + "/" + event.getYear(), Toast.LENGTH_LONG).show();
    }

    public static ArrayList<Double> calculateTotalBudget(ArrayList<CalendarEvent> events) {
        // Element 0: Net Additional Budget
        // Element 1: Net Expenses
        // Element 2: Net Income
        // Element 3: Number of ExpenseEvents
        // Element 4: Number of IncomeEvents
        ArrayList<Double> res = new ArrayList<Double>();
        double netExpenses = 0;
        double netIncome = 0;
        int expensesEvents = 0;
        int incomeEvents = 0;
        int deadlineEvents = 0;
        for (int i = 0; i < events.size(); i++) {
            if (events.get(i) instanceof ExpensesEvent) {
                netExpenses += events.get(i).getExpenses();
                expensesEvents++;
            } else if (events.get(i) instanceof IncomeEvent) {
                netIncome += events.get(i).getIncome();
                incomeEvents++;
            } else {
                deadlineEvents++;
            }
        }
        res.add(Math.round((netIncome - netExpenses) * 100.0) / 100.0);
        res.add(netExpenses);
        res.add(netIncome);
        res.add((double) expensesEvents);
        res.add((double) incomeEvents);
        res.add((double) deadlineEvents);
        return res;
    }

    // Helper method for obtaining all of the LocalDates with an event in the month, returns an ArrayList<LocalDate>
    // Call this before calling obtainCurrentMonthEvents, passing the sorted list returned by this method to the second argument of obtainCurrentMonthEvents
    public static List<LocalDate> getDatesWithEvents(HashMap<LocalDate, ArrayList<CalendarEvent>> eventsInMonth) {
        List<LocalDate> dates = new ArrayList<LocalDate>();
        for (Map.Entry<LocalDate, ArrayList<CalendarEvent>> entry: eventsInMonth.entrySet()) {
            dates.add(entry.getKey());
        }
        Collections.sort(dates);
        return dates;
    }

    // Returns a list which contains all of the events in the month, call this before translateListIndices, and pass the returned list as the first argument
    public static List<CalendarEvent> obtainCurrentMonthEvents(HashMap<LocalDate, ArrayList<CalendarEvent>> monthlyEvents, List<LocalDate> sortedDates) {
        List<CalendarEvent> res = new ArrayList<CalendarEvent>();
        if (sortedDates == null || monthlyEvents == null) {
            return res;
        }
        for (LocalDate date : sortedDates) {
            ArrayList<CalendarEvent> datesToConcat = monthlyEvents.get(date);
            if (datesToConcat != null) {
                res = Stream.concat(res.stream(), datesToConcat.stream()).collect(Collectors.toList());
            }
        }
        return res;
    }

    // Initially creates a list of size 100, filled with null elements
    // translateListIndices sets the elements in dataset at the correct indices which correspond to its category
    // E.g. If there are 3 categories total, and the category "Groceries" is located at position=0 with count=1, the correct index
    // of the next event with the Groceries category is (position + (number_of_categories * count)) = (0 + (3 * 1)) = 3
    public static List<CalendarEvent> translateListIndices(List<CalendarEvent> dataset, Map<ExpenseCategory, int[]> categoryMap) {
        resetCategoryMapping(categoryMap);
        List<CalendarEvent> result = new ArrayList<CalendarEvent>(Collections.nCopies(100, null));
        if (categoryMap == null || categoryMap.isEmpty() || dataset.isEmpty()) {
            return result;
        }
        int number_categories = categoryMap.size();
        for (CalendarEvent event : dataset) {
            if (event instanceof ExpensesEvent) {
                ExpenseCategory eventCategory = ((ExpensesEvent) event).getCategory();
                if (categoryMap.containsKey(eventCategory)) {
                    int[] arr = categoryMap.get(eventCategory);
                    int pos = arr[0];
                    int count = arr[1];
                    result.set(pos + (number_categories * count), event);
                    arr[1]++;
                }
            } else {
                int[] arr = categoryMap.get(new ExpenseCategory("Income"));
                int pos = arr[0];
                int count = arr[1];
                result.set(pos + (number_categories * count), event);
                arr[1]++;
            }
        }
        return result;
    }

    // This is called every time the user swipes to a new month on the calendar view
    // We reinitialize the categoryMap to reflect the CalendarEvent positions and count in the new month, if there are events in the new month
    public static void categoryMapOnMonthChanged(Map<ExpenseCategory, int[]> categoryMap, HashMap<LocalDate, ArrayList<CalendarEvent>> eventsInMonth,
                                                 List<ExpenseCategory> expenseCategories, List<CalendarEvent> dataset) {
        // This is only true if there are already events in the new month
        if (eventsInMonth != null) {
            List<LocalDate> eventDatesInMonth = CalendarHelper.getDatesWithEvents(eventsInMonth);
            List<CalendarEvent> events = CalendarHelper.obtainCurrentMonthEvents(eventsInMonth, eventDatesInMonth);
            for (CalendarEvent event : events) {
                if (event instanceof ExpensesEvent) {
                    ExpenseCategory expenseCategory = ((ExpensesEvent) event).getCategory();
                    int[] categoryInfo = categoryMap.get(expenseCategory);
                    int pos = categoryInfo[0];
                    int count = categoryInfo[1];
                    int index = pos + ((expenseCategories.size() + 1) * count);
                    dataset.set(index, event);
                    categoryInfo[1]++;
                    categoryMap.put(expenseCategory, categoryInfo);
                } else {
                    ExpenseCategory incomeCategory = new ExpenseCategory("Income");
                    int[] categoryInfo = categoryMap.get(incomeCategory);
                    int pos = categoryInfo[0];
                    int count = categoryInfo[1];
                    int index = pos + ((expenseCategories.size() + 1) * count);
                    dataset.set(index, event);
                    categoryInfo[1]++;
                    categoryMap.put(incomeCategory, categoryInfo);
                }
            }
        }
    }

    public static void resetCategoryMapping(Map<ExpenseCategory, int[]> categoryMap) {
        for (Map.Entry<ExpenseCategory, int[]> entry : categoryMap.entrySet()) {
            int[] categoryInfo = entry.getValue();
            int pos = categoryInfo[0];
            int count = categoryInfo[1];
            categoryMap.put(entry.getKey(), new int[]{pos, 1});
        }
    }

    public static Map<ExpenseCategory, int[]> copyCategoryMap(Map<ExpenseCategory, int[]> originalMap) {
        Map<ExpenseCategory, int[]> mapCopy = new HashMap<ExpenseCategory, int[]>();
        for (Map.Entry<ExpenseCategory, int[]> entry : originalMap.entrySet()) {
            ExpenseCategory categoryCopy = entry.getKey().copy();
            mapCopy.put(categoryCopy, entry.getValue());
        }
        return mapCopy;
    }
}
