package com.example.expensestracker;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.content.Context;
import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentResultListener;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.CalendarView;

import java.lang.reflect.Array;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;

/**
 * A simple {@link Fragment} subclass.
 * Use the {@link CalendarFragment#newInstance} factory method to
 * create an instance of this fragment.
 */
public class CalendarFragment extends Fragment{

    // TODO: Rename parameter arguments, choose names that match
    // the fragment initialization parameters, e.g. ARG_ITEM_NUMBER
    private static final String ARG_PARAM1 = "param1";
    private static final String ARG_PARAM2 = "param2";

    // TODO: Rename and change types of parameters
    private String mParam1;
    private String mParam2;
    private Button exitBtn;
    private CalendarView calendar;
    private CalendarDataPass dataPasser;
    private int currentMonth, currentYear, currentDay;
    private HashMap<Integer, HashMap<LocalDate, ArrayList<CalendarEvent>>> monthlyExpensesMapping = new HashMap<Integer, HashMap<LocalDate, ArrayList<CalendarEvent>>>();

    private RecyclerView list;
    private CustomAdapter mAdapter;
    private RecyclerView.LayoutManager mLayoutManager;
    private String[] mDataset;

    public CalendarFragment() {
        // Required empty public constructor
    }


    /**
     * Use this factory method to create a new instance of
     * this fragment using the provided parameters.
     *
     * @param param1 Parameter 1.
     * @param param2 Parameter 2.
     * @return A new instance of fragment CalendarFragment.
     */
    // TODO: Rename and change types and number of parameters
    public static CalendarFragment newInstance(String param1, String param2) {
        CalendarFragment fragment = new CalendarFragment();
        Bundle args = new Bundle();
        args.putString(ARG_PARAM1, param1);
        args.putString(ARG_PARAM2, param2);
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (getArguments() != null) {
            mParam1 = getArguments().getString(ARG_PARAM1);
            mParam2 = getArguments().getString(ARG_PARAM2);
        }

        Activity activity = getActivity();
        MainActivity parentActivity = (MainActivity) activity;
        if (parentActivity.getMonthlyMapping() != null && !parentActivity.getMonthlyMapping().isEmpty()) {
            monthlyExpensesMapping = parentActivity.getMonthlyMapping();
        }

        getParentFragmentManager().setFragmentResultListener("calendarevent", this, new FragmentResultListener() {
            @Override
            @SuppressLint("NewApi")
            public void onFragmentResult(@NonNull String requestKey, @NonNull Bundle result) {
                double data[] = result.getDoubleArray("calendarevent");
                if (data != null) {
                    Calendar calendar = Calendar.getInstance();
                    calendar.set(currentYear, currentMonth, currentDay);
                    LocalDate date = LocalDate.of(currentYear, currentMonth, currentDay);
                    // Expenses Event
                    if (data[1] == 0) {
                        Log.i("Dialog Data", "Type: Expense. Amount: " + data[0]);
                        // There are already some events in the current month
                        if (monthlyExpensesMapping.containsKey(currentMonth)) {
                            HashMap<LocalDate, ArrayList<CalendarEvent>> dayMapping = monthlyExpensesMapping.get(currentMonth);
                            // There are already some events associated with this particular day
                            if (dayMapping.containsKey(date)) {
                                monthlyExpensesMapping.get(currentMonth).get(date).add(new ExpensesEvent(data[0], data[1], date));
                            // First event associated with this particular day, so we have to initialize the array in the nested hashmap
                            } else {
                                ArrayList<CalendarEvent> events = new ArrayList<CalendarEvent>();
                                events.add(new ExpensesEvent(data[0], data[1], date));
                                dayMapping.put(date, events);
                                monthlyExpensesMapping.put(currentMonth, dayMapping);
                            }
                        // No events in the current month yet, initialize the hashmaps and array
                        } else {
                            HashMap<LocalDate, ArrayList<CalendarEvent>> eventsOnDay = new HashMap<LocalDate, ArrayList<CalendarEvent>>();
                            ArrayList<CalendarEvent> events = new ArrayList<CalendarEvent>();
                            events.add(new ExpensesEvent(data[0], data[1], date));
                            eventsOnDay.put(date, events);
                            monthlyExpensesMapping.put(currentMonth, eventsOnDay);
                        }
                    // Income Event
                    } else if (data[0] == 0) {
                        if (monthlyExpensesMapping.containsKey(currentMonth)) {
                            HashMap<LocalDate, ArrayList<CalendarEvent>> dayMapping = monthlyExpensesMapping.get(currentMonth);
                            if (dayMapping.containsKey(date)) {
                                monthlyExpensesMapping.get(currentMonth).get(date).add(new IncomeEvent(data[0], data[1], date));
                            } else {
                                ArrayList<CalendarEvent> events = new ArrayList<CalendarEvent>();
                                events.add(new IncomeEvent(data[0], data[1], date));
                                dayMapping.put(date, events);
                                monthlyExpensesMapping.put(currentMonth, dayMapping);
                            }
                        } else {
                            HashMap<LocalDate, ArrayList<CalendarEvent>> eventsOnDay = new HashMap<LocalDate, ArrayList<CalendarEvent>>();
                            ArrayList<CalendarEvent> events = new ArrayList<CalendarEvent>();
                            events.add(new IncomeEvent(data[0], data[1], date));
                            eventsOnDay.put(date, events);
                            monthlyExpensesMapping.put(currentMonth, eventsOnDay);
                        }
                    }
                }
                initializeData();
            }
        });
        currentMonth = Calendar.getInstance().get(Calendar.MONTH) + 1;
        currentYear = Calendar.getInstance().get(Calendar.YEAR);
        currentDay = Calendar.getInstance().get(Calendar.DAY_OF_MONTH);

    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View v = inflater.inflate(R.layout.fragment_calendar, container, false);
        exitBtn = v.findViewById(R.id.exitBtn);
        calendar = v.findViewById(R.id.calendarView);
        list = v.findViewById(R.id.recyclerView);
        initializeData();


        exitBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                ((MainActivity) getActivity()).unhideMainUI();
                if (monthlyExpensesMapping != null && !monthlyExpensesMapping.isEmpty()) {
                    dataPasser.onCalendarDataPassed(monthlyExpensesMapping);
                }
                getParentFragmentManager().popBackStack();
            }
        });
        calendar.setOnDateChangeListener(new CalendarView.OnDateChangeListener() {
            @Override
            public void onSelectedDayChange(@NonNull CalendarView calendarView, int year, int month, int dayOfMonth) {
                Log.i("Selected Date Change", "New date is " + (month + 1) + "/" + dayOfMonth + "/" + year);
                currentMonth = month + 1;
                currentYear = year;
                currentDay = dayOfMonth;
                CalendarDialogFragment dialog = new CalendarDialogFragment();
                dialog.show(getParentFragmentManager(), "Add additional expenses/income");
            }
        });
        return v;
    }
    @Override
    public void onAttach(Context context) {
        super.onAttach(context);
        dataPasser = (CalendarDataPass) context;
    }

    static ArrayList<Double> calculateTotalBudget(ArrayList<CalendarEvent> events) {
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
        for (int i = 0; i < events.size(); i++) {
                if (events.get(i) instanceof ExpensesEvent) {
                    netExpenses += events.get(i).getExpenses();
                    expensesEvents++;
                } else {
                    netIncome += events.get(i).getIncome();
                    incomeEvents++;
                }
        }
        res.add(Math.round((netIncome - netExpenses) * 100.0) / 100.0);
        res.add(netExpenses);
        res.add(netIncome);
        res.add((double)expensesEvents);
        res.add((double)incomeEvents);
        return res;
    }
    static int getTotalEventsInMonth(HashMap<LocalDate, ArrayList<CalendarEvent>> events) {
        int size = 0;
        for (Map.Entry<LocalDate, ArrayList<CalendarEvent>> entry: events.entrySet()) {
            size += entry.getValue().size();
        }
        return size;
    }

    @SuppressLint("NewApi")
    public void initializeData() {
        if (monthlyExpensesMapping != null && !monthlyExpensesMapping.isEmpty() && monthlyExpensesMapping.containsKey(currentMonth)) {
            HashMap<LocalDate, ArrayList<CalendarEvent>> events = monthlyExpensesMapping.get(currentMonth);
            int datasetSize = getTotalEventsInMonth(events);
            mDataset = new String[datasetSize];
            int datasetCounter = 0;
            for (Map.Entry<LocalDate, ArrayList<CalendarEvent>> entry: events.entrySet()) {
                ArrayList<CalendarEvent> eventsOnDay = entry.getValue();
                LocalDate date = LocalDate.of(currentYear, currentMonth, currentDay);
                ArrayList<Double> dayInfo = calculateTotalBudget(eventsOnDay);
                double totalBudget = dayInfo.get(0);
                int numberOfExpenses = dayInfo.get(3).intValue();
                int numberOfIncome = dayInfo.get(4).intValue();
                mDataset[datasetCounter] = eventsOnDay.get(0).getMonth() + "/" + eventsOnDay.get(0).getDay() + "/" + eventsOnDay.get(0).getYear() + ": " + eventsOnDay.size() + " events (" + numberOfExpenses + " expenses, " + numberOfIncome + " income)"  + " Budget: " + totalBudget;
                datasetCounter++;
            }
            mLayoutManager = new LinearLayoutManager(getActivity());
            mAdapter = new CustomAdapter(mDataset);
            list.setLayoutManager(mLayoutManager);
            list.setAdapter(mAdapter);
        }
    }
}