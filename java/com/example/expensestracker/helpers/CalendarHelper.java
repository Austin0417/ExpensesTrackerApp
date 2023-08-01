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
                    categoryMap.put(eventCategory, arr);
                }
            } else {
                int[] arr = categoryMap.get(new ExpenseCategory("Income"));
                int pos = arr[0];
                int count = arr[1];
                result.set(pos + (number_categories * count), event);
            }
        }
        return result;
    }

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

    public static void initializeCategoryMapping(HashMap<Integer, HashMap<LocalDate, ArrayList<CalendarEvent>>> events,
                                                 Map<Integer, HashMap<ExpenseCategory, int[]>> categoryMap,
                                                 List<ExpenseCategory> categories) {
        // Looping through all of the months and their HashMaps
        for (Map.Entry<Integer, HashMap<LocalDate, ArrayList<CalendarEvent>>> monthToEvents : events.entrySet()) {
            int currentMonth = monthToEvents.getKey();
            HashMap<LocalDate, ArrayList<CalendarEvent>> dateToEvents = monthToEvents.getValue();
            // Looping through all of the events in a particular month
            for (Map.Entry<LocalDate, ArrayList<CalendarEvent>> e: dateToEvents.entrySet()) {
                ArrayList<CalendarEvent> eventsOnDay = e.getValue();
                // Looping through all of the events on a particular day
                for (CalendarEvent calendarEvent: eventsOnDay) {
                    if (calendarEvent instanceof ExpensesEvent) {
                        ExpenseCategory categoryOfEvent = ((ExpensesEvent) calendarEvent).getCategory();
                        // Integer month key doesn't yet exist in the categoryMap, so initialize it
                        if (!categoryMap.containsKey(currentMonth)) {
                            HashMap<ExpenseCategory, int[]> newCategory = new HashMap<ExpenseCategory, int[]>();
                            newCategory.put(categoryOfEvent, new int[]{categories.indexOf(categoryOfEvent), 1});
                            categoryMap.put(currentMonth, newCategory);
                        // Integer month key exists, obtain the HashMap corresponding to the month
                        } else {
                            HashMap<ExpenseCategory, int[]> categoryPosAndCount = categoryMap.get(currentMonth);
                            // Check to see if the HashMap of the particular month already contains the event category
                            // If not, initialize the key-value pair
                            if (!categoryPosAndCount.containsKey(categoryOfEvent)) {
                                categoryPosAndCount.put(categoryOfEvent, new int[]{categories.indexOf(categoryOfEvent), 1});
                                categoryMap.put(currentMonth, categoryPosAndCount);
                            // If it does, simply increment the category count.
                            } else {
                                int[] categoryInfo = categoryPosAndCount.get(((ExpensesEvent) calendarEvent).getCategory());
                                categoryInfo[1]++;
                                categoryPosAndCount.put(categoryOfEvent, categoryInfo);
                                categoryMap.put(currentMonth, categoryPosAndCount);
                            }
                        }
                    // Event is of instance IncomeEvent
                    } else {
                        ExpenseCategory incomeCategory = new ExpenseCategory("Income");
                        if (!categoryMap.containsKey(currentMonth)) {
                            HashMap<ExpenseCategory, int[]> newCategory = new HashMap<ExpenseCategory, int[]>();
                            newCategory.put(incomeCategory, new int[]{categories.indexOf(incomeCategory), 1});
                            categoryMap.put(currentMonth, newCategory);
                        } else {
                            HashMap<ExpenseCategory, int[]> categoryPosAndCount = categoryMap.get(currentMonth);
                            if (!categoryPosAndCount.containsKey(incomeCategory)) {
                                categoryPosAndCount.put(incomeCategory, new int[]{categories.indexOf(incomeCategory), 1});
                                categoryMap.put(currentMonth, categoryPosAndCount);
                            } else {
                                int categoryInfo[] = categoryPosAndCount.get(incomeCategory);
                                categoryInfo[1]++;
                                categoryPosAndCount.put(incomeCategory, categoryInfo);
                                categoryMap.put(currentMonth, categoryPosAndCount);
                            }
                        }
                    }
                }
            }
        }
    }

    public static int updateCategoryMapping(Map<Integer, HashMap<ExpenseCategory, int[]>> categoryMap, List<ExpenseCategory> categories, CalendarEvent event) {
        int pos;
        int count;
        if (!categoryMap.containsKey(event.getMonth())) {
            HashMap<ExpenseCategory, int[]> monthlyCategories = new HashMap<ExpenseCategory, int[]>();
            if (event instanceof ExpensesEvent) {
                ExpenseCategory categoryEvent = ((ExpensesEvent) event).getCategory();
                monthlyCategories.put(categoryEvent, new int[]{categories.indexOf(categoryEvent), 1});
                categoryMap.put(event.getMonth(), monthlyCategories);
                pos = categories.indexOf(categoryEvent);
                count = 1;
            } else {
                ExpenseCategory incomeCategory = new ExpenseCategory("Income");
                monthlyCategories.put(incomeCategory, new int[]{categories.indexOf(incomeCategory), 1});
                categoryMap.put(event.getMonth(), monthlyCategories);
                pos = categories.indexOf(incomeCategory);
                count = 1;
            }
        } else {
            HashMap<ExpenseCategory, int[]> monthlyCategories = categoryMap.get(event.getMonth());
            if (event instanceof ExpensesEvent) {
                ExpenseCategory category = ((ExpensesEvent) event).getCategory();
                pos = categories.indexOf(category);
                if (!monthlyCategories.containsKey(category)) {
                    monthlyCategories.put(category, new int[]{categories.indexOf(category), 1});
                    categoryMap.put(event.getMonth(), monthlyCategories);
                    count = 1;
                } else {
                    int categoryInfo[] = monthlyCategories.get(category);
                    categoryInfo[1]++;
                    monthlyCategories.put(category, categoryInfo);
                    categoryMap.put(event.getMonth(), monthlyCategories);
                    count = categoryInfo[1];
                }
            } else {
                ExpenseCategory incomeCategory = new ExpenseCategory("Income");
                pos = categories.indexOf(incomeCategory);
                if (!monthlyCategories.containsKey(incomeCategory)) {
                    monthlyCategories.put(incomeCategory, new int[]{categories.indexOf(incomeCategory), 1});
                    categoryMap.put(event.getMonth(), monthlyCategories);
                    count = 1;
                } else {
                    int[] categoryInfo = monthlyCategories.get(incomeCategory);
                    categoryInfo[1]++;
                    monthlyCategories.put(incomeCategory, categoryInfo);
                    categoryMap.put(event.getMonth(), monthlyCategories);
                    count = categoryInfo[1];
                }
            }
        }
        return (pos + ((categories.size() + 1) * count));
    }
}
