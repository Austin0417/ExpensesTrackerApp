package com.example.expensestracker.helpers;

import android.content.Context;
import android.widget.Toast;

import com.example.expensestracker.calendar.CalendarEvent;
import com.example.expensestracker.calendar.DeadlineEvent;
import com.example.expensestracker.calendar.ExpensesEvent;
import com.example.expensestracker.calendar.IncomeEvent;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.HashMap;

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
}
