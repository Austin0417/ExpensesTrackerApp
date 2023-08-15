package com.example.expensestracker.pie;

import android.content.Context;
import android.os.Environment;
import android.widget.Toast;

import com.example.expensestracker.calendar.CalendarEvent;
import com.example.expensestracker.calendar.ExpensesEvent;
import com.example.expensestracker.monthlyinfo.MonthlyExpense;
import com.opencsv.CSVWriter;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

public class CSVExporter {
    private final Month selectedMonth;

    public CSVExporter(Month selectedMonth) {
        this.selectedMonth = selectedMonth;
    }

    public void export(Context context, List<MonthlyExpense> monthlyExpenses, List<CalendarEvent> calendarEvents) {
        File file = Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOCUMENTS);
        File csvFile = new File(file, "Expense_Tracker_Data_" + selectedMonth.getMonthString() + ".csv");
        try {
            CSVWriter writer = new CSVWriter(new FileWriter(csvFile));
            writer.writeNext(new String[]{"Date", "Category", "Amount"});
            CSVData csvData = writeCSVEntries(monthlyExpenses, calendarEvents, writer);
            writer.close();
            Toast.makeText(context, "Saved as " + "Expense_Tracker_Data_" + selectedMonth.getMonthString() +
                    ".csv. You can find the file in the Documents folder", Toast.LENGTH_LONG).show();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
    private CSVData writeCSVEntries(List<MonthlyExpense> monthlyExpenses, List<CalendarEvent> calendarEvents, CSVWriter writer) {
        List<String[]> monthlyExpenseEntries = new ArrayList<String[]>();
        List<String[]> calendarEventEntries = new ArrayList<String[]>();
        for (CalendarEvent calendarEvent : calendarEvents) {
            String[] entry = new String[3];
            entry[0] = calendarEvent.getDate().toString();
            if (calendarEvent instanceof ExpensesEvent) {
                entry[1] = ((ExpensesEvent) calendarEvent).getCategory().getName();
            } else {
                entry[1] = "Additional Income";
            }
            entry[2] = Double.toString(calendarEvent.getAmount());
            writer.writeNext(entry);
            calendarEventEntries.add(entry);
        }
        for (MonthlyExpense monthlyExpense : monthlyExpenses) {
            String[] entry = new String[3];
            entry[0] = "N/A (Recurring Monthly Expense)";
            entry[1] = monthlyExpense.getDescription();
            entry[2] = Double.toString(monthlyExpense.getAmount());
            writer.writeNext(entry);
            monthlyExpenseEntries.add(entry);
        }
        return new CSVData(calendarEventEntries, monthlyExpenseEntries);
    }
}
