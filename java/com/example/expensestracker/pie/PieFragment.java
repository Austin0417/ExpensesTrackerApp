package com.example.expensestracker.pie;

import android.app.AlertDialog;
import android.content.DialogInterface;
import android.graphics.Color;
import android.os.AsyncTask;
import android.os.Bundle;
import android.util.Log;
import android.view.DragEvent;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentResultListener;

import com.example.expensestracker.CalendarEventsEntity;
import com.example.expensestracker.MainActivity;
import com.example.expensestracker.R;
import com.example.expensestracker.calendar.CalendarDataPass;
import com.example.expensestracker.calendar.CalendarEvent;
import com.example.expensestracker.calendar.ExpenseCategory;
import com.example.expensestracker.calendar.ExpensesEvent;
import com.example.expensestracker.calendar.IncomeEvent;
import com.example.expensestracker.dialogs.EditExpenseDialog;
import com.example.expensestracker.helpers.PieHelper;
import com.example.expensestracker.monthlyinfo.MonthlyExpense;
import com.github.mikephil.charting.charts.PieChart;
import com.github.mikephil.charting.data.Entry;
import com.github.mikephil.charting.data.PieData;
import com.github.mikephil.charting.data.PieDataSet;
import com.github.mikephil.charting.data.PieEntry;
import com.github.mikephil.charting.highlight.Highlight;
import com.github.mikephil.charting.listener.OnChartValueSelectedListener;
import com.github.mikephil.charting.utils.ColorTemplate;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class PieFragment extends Fragment {
    private Button backBtn;
    private PieChart pieChart;
    private ImageView monthBack;
    private ImageView monthForward;
    private TextView monthDisplay;

    // User's set monthly income value, sent from MainActivity
    private float monthlyIncome;

    // User's created recurring monthly expenses
    private List<MonthlyExpense> expenseList = new ArrayList<MonthlyExpense>();

    // List of all calendar events in the current month
    private List<CalendarEvent> calendarEvents;

    private List<PieEntry> pieEntries = new ArrayList<PieEntry>();

    // Map to store the total cost of each ExpenseCategory
    private Map<ExpenseCategory, Double> costPerCategory;

    private Map<ExpenseCategory, List<ExpensesEvent>> categoryToEventMapping;

    // Int variable to keep track of the chart's current month (1-indexed)
    private int currentMonth = 1;

    // Flag that is set when the current month is either 1 or 12 (if the month is 1, and back is clicked, the new month should be 12)
    private boolean shouldReset = false;

    private int[] colorsDataset = new int[]{
            Color.LTGRAY,
            Color.GRAY,
            Color.RED,
            Color.GREEN,
            Color.BLUE,
            Color.YELLOW,
            Color.CYAN,
            Color.MAGENTA,
            Color.rgb(88, 22, 130),
            Color.rgb(147, 65, 242),
            Color.rgb(224, 149, 29),
            Color.rgb(49, 214, 178),
            Color.rgb(45, 239, 84),
            Color.rgb(23, 183, 237),
    };


    public PieFragment(List<MonthlyExpense> expenseList, List<CalendarEvent> calendarEvents, double monthlyIncome) {
        this.expenseList = expenseList;
        this.monthlyIncome = (float) monthlyIncome;
        this.calendarEvents = calendarEvents;
        this.currentMonth = LocalDate.now().getMonthValue();
    }


    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        return inflater.inflate(R.layout.pie_fragment, null);
    }

    @Override
    public void onViewCreated(View view, Bundle savedInstanceState) {
        backBtn = view.findViewById(R.id.pie_back_btn);
        pieChart = view.findViewById(R.id.pieChart);
        monthBack = view.findViewById(R.id.monthBackBtn);
        monthForward = view.findViewById(R.id.monthForwardBtn);
        monthDisplay = view.findViewById(R.id.monthTextView);

        initialize();
        pieEntries = generateChartEntries();
        PieDataSet dataset = generateChartDataset(pieEntries);
        setChartData(dataset);

        getParentFragmentManager().setFragmentResultListener("PIE_CHART", this, new FragmentResultListener() {
            @Override
            public void onFragmentResult(@NonNull String requestKey, @NonNull Bundle result) {
                String newDescription = result.getString("new_description");
                double newAmount = result.getDouble("new_amount");
                int index = result.getInt("index");
                expenseList.set(index, new MonthlyExpense(newDescription, newAmount));
                updatePieDataset();
            }
        });
        setViewClickListeners();
    }

    public void initialize() {
        monthDisplay.setText(Month.getMonth(currentMonth).getMonthString());
        if (calendarEvents != null && !calendarEvents.isEmpty()) {
            CostMapping costMapping = PieHelper.initializeCostsPerCategoryMapping(calendarEvents);
            costPerCategory = costMapping.getCategoryToAmountMapping();
            categoryToEventMapping = costMapping.getCategoryToEventMapping();
        } else {
            // If calendarEvents is null or empty, both calendarEvents and costsPerCategory will be empty
            costPerCategory = new HashMap<ExpenseCategory, Double>();
            categoryToEventMapping = new HashMap<ExpenseCategory, List<ExpensesEvent>>();
            calendarEvents = new ArrayList<CalendarEvent>();
        }
    }

    public void setViewClickListeners() {
        backBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                getParentFragmentManager().popBackStack();
                ((MainActivity) getContext()).unhideMainUI();
            }
        });
        monthForward.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (shouldReset && currentMonth == 12) {
                    currentMonth = 1;
                    shouldReset = false;
                } else if (shouldReset && currentMonth == 1) {
                    currentMonth++;
                    shouldReset = false;
                } else {
                    currentMonth++;
                }

                if (currentMonth == 1 || currentMonth == 12) {
                    shouldReset = true;
                }
                refresh();
            }
        });
        monthBack.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (shouldReset && currentMonth == 1) {
                    currentMonth = 12;
                    shouldReset = false;
                } else if (shouldReset && currentMonth == 12) {
                    currentMonth--;
                    shouldReset = false;
                } else {
                    currentMonth--;
                }

                if (currentMonth == 1 || currentMonth == 12) {
                    shouldReset = true;
                }
                refresh();
            }
        });
        pieChart.setOnChartValueSelectedListener(new OnChartValueSelectedListener() {
            @Override
            public void onValueSelected(Entry e, Highlight h) {
                PieEntry entry = (PieEntry) e;
                Log.i("PIE CHART", "Clicked entry " + entry.getLabel() + ". Value=" + entry.getValue());
                if (isSliceMonthlyExpense(new MonthlyExpense(entry.getLabel(), entry.getValue()))) {
                    MonthlyExpense selectedExpense = new MonthlyExpense(entry.getLabel(), entry.getValue());
                    EditExpenseDialog dialog = new EditExpenseDialog(selectedExpense, expenseList.indexOf(selectedExpense));
                    dialog.show(getParentFragmentManager(), "EDIT EXPENSE");
                } else if (isSliceCategory(new ExpenseCategory(entry.getLabel()))) {
                    ExpenseCategory category = new ExpenseCategory(entry.getLabel());
                    AlertDialog dialog = showCategoryDialog(category);
                    dialog.show();
                }
            }
            @Override
            public void onNothingSelected() {
            }
        });
    }

    // Updates necessary views and data for the pie chart when transitioning to a new month
    private void refresh() {
        Month month = Month.getMonth(currentMonth);
        monthDisplay.setText(month.getMonthString());
        CalendarDataPass dataPass = (MainActivity) getContext();
        List<CalendarEventsEntity> eventsInMonth = dataPass.getEventsInMonth(currentMonth);
        updateCalendarEvents(eventsInMonth);
        updatePieDataset();
    }

    // Initializes and returns a List<PieEntry>, which will be used to generate a PieDataSet
    private List<PieEntry> generateChartEntries() {
        List<PieEntry> entries = new ArrayList<PieEntry>();
        float totalExpenses = 0;

        // Loop through the MonthlyExpense List, adding a PieEntry for each MonthlyExpense
        if (expenseList != null && !expenseList.isEmpty()) {
            for (MonthlyExpense expense : expenseList) {
                float expenseAmount = (float) expense.getAmount();
                totalExpenses += expenseAmount;
                entries.add(new PieEntry(expenseAmount, expense.getDescription()));
            }
        }

        // Loop through the ExpenseCategory to Double mapping, adding a PieEntry for each ExpenseCategory
        if (costPerCategory != null && !costPerCategory.isEmpty()) {
            for (Map.Entry<ExpenseCategory, Double> entry : costPerCategory.entrySet()) {
                double value = entry.getValue();
                float categoryExpense = (float) value;
                totalExpenses += categoryExpense;
                entries.add(new PieEntry(categoryExpense, entry.getKey().getName()));
            }
        }

        // Simple check to verify if monthlyIncome is greater than the totalExpenses amount
        if (monthlyIncome - totalExpenses > 0) {
            entries.add(new PieEntry(monthlyIncome - totalExpenses, "Remaining Budget"));
        }
        return entries;
    }

    // Uses the PieEntry list from generateChartEntries to initialize a PieDataSet
    // Here we also set various custom options for the appearance of the pie chart, like color and text size
    private PieDataSet generateChartDataset(List<PieEntry> entries) {
        PieDataSet dataset = new PieDataSet(entries, "Monthly Finances");
        dataset.setValueTextSize((float)25.0);
        dataset.setValueFormatter(new PieFormatter());
        dataset.setColors(ColorTemplate.COLORFUL_COLORS);
        dataset.setValueTextColor(Color.BLACK);
        int[] selectedColors = new int[costPerCategory.size() + expenseList.size() + 1];
        PieHelper.setDatasetColors(colorsDataset, selectedColors);
        dataset.setColors(selectedColors);
        return dataset;
    }

    // Binding the PieDataSet to the pie chart
    private void setChartData(PieDataSet dataset) {
        PieData data = new PieData(dataset);
        pieChart.setUsePercentValues(true);
        pieChart.setDrawHoleEnabled(false);
        pieChart.getDescription().setEnabled(false);
        pieChart.setDrawEntryLabels(true);
        pieChart.setRotationEnabled(false);
        pieChart.setData(data);
        pieChart.invalidate();
    }

    private boolean isSliceMonthlyExpense(MonthlyExpense selectedExpense) {
        for (MonthlyExpense expense : expenseList) {
            if (expense.equals(selectedExpense)) {
                return true;
            }
        }
        return false;
    }

    private boolean isSliceCategory(ExpenseCategory category) {
        return categoryToEventMapping.containsKey(category);
    }

    // Called when user updates a MonthlyExpense after clicking on a slice of the pie chart
    private void updatePieDataset() {
        initialize();
        List<PieEntry> entries = generateChartEntries();
        PieDataSet dataset = generateChartDataset(entries);
        setChartData(dataset);
        pieChart.notifyDataSetChanged();
    }

    // Called whenever the user clicks to a different month on the pie chart, we need to reset calendarEvents to hold the events in the new month
    private void updateCalendarEvents(List<CalendarEventsEntity> events) {
        if (events != null && !events.isEmpty()) {
            calendarEvents.clear();
            for (CalendarEventsEntity entity : events) {
                if (entity.expense == 0) {
                    calendarEvents.add(new IncomeEvent(entity.expense, entity.income, LocalDate.of(entity.year, entity.month, entity.day)));
                } else {
                    ExpensesEvent event = new ExpensesEvent(entity.expense, entity.income, LocalDate.of(entity.year, entity.month, entity.day));
                    CalendarDataPass dataPass = (MainActivity) getContext();
                    List<ExpenseCategory> category = dataPass.getCategory(entity.category_id);
                    if (category != null && !category.isEmpty()) {
                        ((ExpensesEvent) event).setCategory(category.get(0));
                        calendarEvents.add(event);
                    } else {
                        Log.i("PIE_CHART", "Could not retrieve category for expense event");
                    }
                }
            }
        } else {
            Log.i("PIE_CHART", "Error updating pie chart data (CalendarEventsEntity List was null/empty)");
            calendarEvents = new ArrayList<CalendarEvent>();
        }
    }

    private AlertDialog showCategoryDialog(ExpenseCategory category) {
        View v = LayoutInflater.from(getContext()).inflate(R.layout.edit_event, null);
        List<ExpensesEvent> eventsInCategory = categoryToEventMapping.get(category);
        String[] events = new String[eventsInCategory.size()];
        for (int i = 0; i < events.length; i++) {
            events[i] = eventsInCategory.get(i).toString();
        }
        return new AlertDialog.Builder(getContext())
                .setMessage(PieHelper.listEventsAsString(eventsInCategory))
                .setPositiveButton("OK", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {

                    }
                })
                .setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {

                    }
                })
                .setTitle(category.getName())
                .create();
    }
}
