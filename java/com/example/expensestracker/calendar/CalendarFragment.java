package com.example.expensestracker.calendar;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.os.AsyncTask;
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
import android.widget.TextView;
import android.widget.Toast;

import com.example.expensestracker.CalendarEventsDAO;
import com.example.expensestracker.CalendarEventsEntity;
import com.example.expensestracker.DeadlineEventsDAO;
import com.example.expensestracker.DeadlineEventsEntity;
import com.example.expensestracker.ExpensesTrackerDatabase;
import com.example.expensestracker.MainActivity;
import com.example.expensestracker.R;
import com.example.expensestracker.calendar.CalendarEvent;
import com.example.expensestracker.calendar.DeadlineEvent;
import com.example.expensestracker.calendar.ExpensesEvent;
import com.example.expensestracker.calendar.IncomeEvent;
import com.example.expensestracker.dialogs.CalendarDialogFragment;
import com.example.expensestracker.dialogs.ClearDialog;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.Calendar;
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
    // Remove this later
    public static final String SENDER_ID = "1056081938816";

    // TODO: Rename and change types of parameters
    ExpensesTrackerDatabase db;
    private String mParam1;
    private String mParam2;
    private Button exitBtn;
    private Button deadlineBtn;
    private Button clearBtn;
    private CalendarView calendar;
    private CalendarDataPass dataPasser;
    private int currentMonth, currentYear, currentDay;
    private HashMap<Integer, HashMap<LocalDate, ArrayList<CalendarEvent>>> monthlyExpensesMapping = new HashMap<Integer, HashMap<LocalDate, ArrayList<CalendarEvent>>>();
    private ArrayList<DeadlineEvent> deadlines = new ArrayList<DeadlineEvent>();

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
        if (parentActivity.getDeadlines() != null && !parentActivity.getDeadlines().isEmpty()) {
            deadlines = parentActivity.getDeadlines();
        }

        getParentFragmentManager().setFragmentResultListener("fragment_data", this, new FragmentResultListener() {
            @Override
            @SuppressLint("NewApi")
            public void onFragmentResult(@NonNull String requestKey, @NonNull Bundle result) {
                double data[] = result.getDoubleArray("calendarevent");
                boolean clearSelections[] = result.getBooleanArray("buttons_selected");
                String deadlineDescription = result.getString("deadline_description");

                if (clearSelections != null) {
                    if (clearSelections[0] && clearSelections[1]) {
                        // Clear both calendar and deadlines
                        if (monthlyExpensesMapping.containsKey(currentMonth)) {
                            monthlyExpensesMapping.get(currentMonth).clear();
                            initializeData();
                        }
                        deadlines.clear();
                        AsyncTask.execute(new Runnable() {
                            @Override
                            public void run() {
                                CalendarEventsDAO calendarDAO = db.calendarEventsDAO();
                                DeadlineEventsDAO deadlineDAO = db.deadlineEventsDAO();
                                calendarDAO.clearCalendarEvents();
                                deadlineDAO.clearDeadlineEvents();
                            }
                        });

                    } else if (clearSelections[0]) {
                        // Clear only calendar events
                        if (monthlyExpensesMapping.containsKey(currentMonth)) {
                            monthlyExpensesMapping.get(currentMonth).clear();
                            initializeData();
                            AsyncTask.execute(new Runnable() {
                                @Override
                                public void run() {
                                    CalendarEventsDAO dao = db.calendarEventsDAO();
                                    dao.clearCalendarEvents();
                                }
                            });
                        }
                    } else {
                        // Clear only deadline events
                        deadlines.clear();
                        AsyncTask.execute(new Runnable() {
                            @Override
                            public void run() {
                                DeadlineEventsDAO dao = db.deadlineEventsDAO();
                                dao.clearDeadlineEvents();
                                Log.i("Clearing", "Deadline Events, success");
                            }
                        });
                    }
                }

                if (data != null) {
                    LocalDate date = LocalDate.of(currentYear, currentMonth, currentDay);

                    // Deadline Event
                    if (deadlineDescription != null) {
                        Log.i("Deadline Description", deadlineDescription);
                        DeadlineEvent deadline = new DeadlineEvent(data[0], data[1], date, deadlineDescription);
                        Calendar calendar = Calendar.getInstance();
                        calendar.set(deadline.getYear(), deadline.getMonth() - 1, deadline.getDay());
                        // Check for the case where the user attempts to add a deadline for a past date
                        if (calendar.getTimeInMillis() - System.currentTimeMillis() < 0) {
                            Toast.makeText(getActivity(), "Cannot set a deadline in the past!", Toast.LENGTH_LONG).show();
                            return;
                        }
                        deadlines.add(deadline);
                        parentActivity.setAlarmForDeadline(deadline);
                        Toast.makeText(getActivity(), "Successfully set deadline for " + deadline.getMonth() + "/" + deadline.getDay() + "/" + deadline.getYear(), Toast.LENGTH_LONG).show();

                        // After every calendar event addition, we have to update the database accordingly
                        AsyncTask.execute(new Runnable() {
                            @Override
                            public void run() {
                                DeadlineEventsDAO deadlineDAO = db.deadlineEventsDAO();
                                DeadlineEventsEntity entity = new DeadlineEventsEntity();
                                entity.information = deadlineDescription;
                                entity.expense = data[0];
                                entity.day = date.getDayOfMonth();
                                entity.month = date.getMonthValue();
                                entity.year = date.getYear();
                                deadlineDAO.insert(entity);
                                Log.i("Database insertion", "Inserted deadline!");
                            }
                        });
                    }

                    // Expenses Event
                    else if (data[1] == 0) {
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

                        AsyncTask.execute(new Runnable() {
                            @Override
                            public void run() {
                                CalendarEventsDAO dao = db.calendarEventsDAO();
                                CalendarEventsEntity entity = new CalendarEventsEntity();
                                entity.day = date.getDayOfMonth();
                                entity.month = date.getMonthValue();
                                entity.year = date.getYear();
                                entity.expense = data[0];
                                entity.income = data[1];
                                dao.insert(entity);
                                Log.i("Database insertion", "Expense event inserted!");
                            }
                        });

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

                        AsyncTask.execute(new Runnable() {
                            @Override
                            public void run() {
                                CalendarEventsDAO dao = db.calendarEventsDAO();
                                CalendarEventsEntity entity = new CalendarEventsEntity();
                                entity.day = date.getDayOfMonth();
                                entity.month = date.getMonthValue();
                                entity.year = date.getYear();
                                entity.expense = data[0];
                                entity.income = data[1];
                                dao.insert(entity);
                                Log.i("Database insertion", "Income event inserted!");
                            }
                        });

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
    @SuppressLint("NewAPI")
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View v = inflater.inflate(R.layout.fragment_calendar, container, false);
        exitBtn = v.findViewById(R.id.exitBtn);
        deadlineBtn = v.findViewById(R.id.deadlineBtn);
        clearBtn = v.findViewById(R.id.clearBtn);
        calendar = v.findViewById(R.id.calendarView);
        list = v.findViewById(R.id.recyclerView);

        initializeData();

        // Setting click listeners for buttons
        exitBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                getParentFragmentManager().popBackStack();
                ((MainActivity) getActivity()).unhideMainUI();
                ((MainActivity) getActivity()).updateTextColorStatus();
                dataPasser.onCalendarDataPassed(monthlyExpensesMapping, deadlines);

            }
        });
        deadlineBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                AlertDialog.Builder builder = new AlertDialog.Builder(getContext());
                builder.setTitle("Deadline Information");
                LayoutInflater inflater = LayoutInflater.from(getContext());
                View dialogView = inflater.inflate(R.layout.deadline_info_layout, null);
                builder.setView(dialogView);
                TextView deadlineText = dialogView.findViewById(R.id.deadlineText);
                deadlineText.setText(displayDeadlines(deadlines));
                builder.setPositiveButton("OK", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        dialog.dismiss();
                    }
                });
                AlertDialog dialog = builder.create();
                dialog.show();
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
        clearBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                ClearDialog clearDialog = new ClearDialog();
                clearDialog.show(getParentFragmentManager(), "Clear");
            }
        });
        return v;
    }
    @Override
    public void onAttach(Context context) {
        super.onAttach(context);
        dataPasser = (CalendarDataPass) context;
    }

    // Handle to the database for this fragment
    public void setDatabase(ExpensesTrackerDatabase db) {
        this.db = db;
    }

    // Helper method to calculate all financial information
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
        res.add((double)expensesEvents);
        res.add((double)incomeEvents);
        res.add((double)deadlineEvents);
        return res;
    }
    public static int getTotalEventsInMonth(HashMap<LocalDate, ArrayList<CalendarEvent>> events) {
        int size = 0;
        for (Map.Entry<LocalDate, ArrayList<CalendarEvent>> entry: events.entrySet()) {
            size += entry.getValue().size();
        }
        return size;
    }
    public static int getNumberOfDeadlines(ArrayList<CalendarEvent> events) {
        int deadlineCount = 0;
        for (int i = 0; i < events.size(); i++) {
            if (events.get(i) instanceof DeadlineEvent && !events.get(i).isMarked()) {
                deadlineCount++;
                events.get(i).setMarked(true);
            }
        }
        return deadlineCount;
    }

    // Displaying deadlines as text information in the TextView for when deadlinesBtn is clicked
    public static String displayDeadlines(ArrayList<DeadlineEvent> deadlines) {
        String res = "";
        for (int i = 0; i < deadlines.size(); i++) {
            res += deadlines.get(i).getMonth() + "/" + deadlines.get(i).getDay() + "/" + deadlines.get(i).getYear() + ": $" + deadlines.get(i).getExpenses() + " - " + deadlines.get(i).getInformation() + "\n";
        }
        return res;
    }

    // Initializes and updates the RecyclerView that displays all calendar events in a list
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
                mDataset[datasetCounter] = eventsOnDay.get(0).getMonth() + "/" + eventsOnDay.get(0).getDay() + "/" + eventsOnDay.get(0).getYear() + ": " + eventsOnDay.size() + " events (" + numberOfExpenses + " expenses, " + numberOfIncome + " income)"  + " Budget: $" + totalBudget;
                datasetCounter++;
            }
            mLayoutManager = new LinearLayoutManager(getActivity());
            mAdapter = new CustomAdapter(mDataset);
            list.setLayoutManager(mLayoutManager);
            list.setAdapter(mAdapter);
        }
    }

}