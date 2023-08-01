package com.example.expensestracker.calendar;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.content.Context;
import android.os.AsyncTask;
import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentResultListener;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.Toast;

import com.example.expensestracker.CalendarEventsDAO;
import com.example.expensestracker.CalendarEventsEntity;
import com.example.expensestracker.DeadlineEventsDAO;
import com.example.expensestracker.DeadlineEventsEntity;
import com.example.expensestracker.ExpensesTrackerDatabase;
import com.example.expensestracker.MainActivity;
import com.example.expensestracker.R;
import com.example.expensestracker.dialogs.CalendarDialogFragment;
import com.example.expensestracker.dialogs.ClearDialog;
import com.example.expensestracker.dialogs.DeadlineDialog;
import com.example.expensestracker.dialogs.ExpenseCategoryDialog;
import com.example.expensestracker.helpers.CalendarHelper;
import com.prolificinteractive.materialcalendarview.CalendarDay;
import com.prolificinteractive.materialcalendarview.MaterialCalendarView;
import com.prolificinteractive.materialcalendarview.OnDateSelectedListener;
import com.prolificinteractive.materialcalendarview.OnMonthChangedListener;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * A simple {@link Fragment} subclass.
 * Use the {@link CalendarFragment#newInstance} factory method to
 * create an instance of this fragment.
 */
public class CalendarFragment extends Fragment {

    // TODO: Rename parameter arguments, choose names that match
    // the fragment initialization parameters, e.g. ARG_ITEM_NUMBER
    private static final String ARG_PARAM1 = "param1";
    private static final String ARG_PARAM2 = "param2";


    // TODO: Rename and change types of parameters

    // Reference to MainActivity's database
    ExpensesTrackerDatabase db;

    private String mParam1;
    private String mParam2;

    // References to UI elements within fragment_calendar.xml
    private Button exitBtn;
    private Button deadlineBtn;
    private Button clearBtn;
    private Button categoryBtn;
    private MaterialCalendarView calendar;
    private CalendarDataPass dataPasser;
    private RecyclerView list;
    private CalendarRecyclerAdapter mAdapter;
    private RecyclerView.LayoutManager mLayoutManager;
    private String[] mDataset;

    // Private variables to keep track of the currently selected month, year and day in the calendar
    public int currentMonth, currentYear, currentDay;

    // Boolean array data member to keep track of the current month's up to date status. 12 indexes each representing a month
    private boolean[] calendarIsUpToDate = new boolean[12];

    // Main data structure that will be used to store the mappings of months (key) to day-events (value)
    private HashMap<Integer, HashMap<LocalDate, ArrayList<CalendarEvent>>> monthlyExpensesMapping;

    private List<LocalDate> dates = new ArrayList<LocalDate>();

    // Simple ArrayList that will keep track of all deadlines, specific date of the deadline is not important
    private ArrayList<DeadlineEvent> deadlines;

    private List<ExpenseCategory> expenseCategories = new ArrayList<ExpenseCategory>();

    // Map to keep track each ExpenseCategory's position within the RecyclerView (int array index 0) and number of events under each category (int array index 1)
    private Map<ExpenseCategory, int[]> categoryMap = new HashMap<ExpenseCategory, int[]>();

    private List<CalendarEvent> totalEventsInMonth;


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

    public List<ExpenseCategory> getCategories() { return expenseCategories; }

    public CalendarDataPass getCalendarDataPasser() { return dataPasser; }

    public void initialize() {
        Activity activity = getActivity();
        MainActivity parentActivity = (MainActivity) activity;

        // Check to see if the data structures in MainActivity has been initialized/contain data
        // This happens in the case when the app is closed, and data is retrieved from the database the next time the app is launched
        if (parentActivity.getMonthlyMapping() != null && !parentActivity.getMonthlyMapping().isEmpty()) {
            monthlyExpensesMapping = parentActivity.getMonthlyMapping();
        } else {
            monthlyExpensesMapping = new HashMap<Integer, HashMap<LocalDate, ArrayList<CalendarEvent>>>();
        }

        if (parentActivity.getDeadlines() != null && !parentActivity.getDeadlines().isEmpty()) {
            deadlines = parentActivity.getDeadlines();
        } else {
            deadlines = new ArrayList<DeadlineEvent>();
        }

        if (parentActivity.getDatabase() != null) {
            db = parentActivity.getDatabase();
        }

        AsyncTask.execute(new Runnable() {
            @Override
            public void run() {
                ExpenseCategoryDAO dao = db.expenseCategoryDAO();
                List<ExpenseCategory> categories_in_db = dao.getAllCategories();
                if (categories_in_db != null && !categories_in_db.isEmpty()) {
                    expenseCategories = categories_in_db;
                    //CalendarHelper.initializeCategoryMapping(monthlyExpensesMapping, categoryMap, expenseCategories);
                    for (int i = 0; i < expenseCategories.size(); i++) {
                        categoryMap.put(expenseCategories.get(i), new int[]{i, 1});
                    }
                    categoryMap.put(new ExpenseCategory("Income"), new int[]{expenseCategories.size(), 1});
                }
            }
        });
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (getArguments() != null) {
            mParam1 = getArguments().getString(ARG_PARAM1);
            mParam2 = getArguments().getString(ARG_PARAM2);
        }

        initialize();

        // Set a fragment result listener for the main CalendarFragment.
        // This listener will allow child dialogs/fragments of CalendarFragment to send data back to CalendarFragment
        // Ex. Obtaining user inputted information and amount for a CalendarEvent from a pop-up child DialogFragment
        getParentFragmentManager().setFragmentResultListener("fragment_data", this, new FragmentResultListener() {
            @Override
            @SuppressLint("NewApi")
            public void onFragmentResult(@NonNull String requestKey, @NonNull Bundle result) {
                boolean clearSelections[] = result.getBooleanArray("buttons_selected");
                double data[] = result.getDoubleArray("calendarevent");
                String deadlineDescription = result.getString("deadline_description");
                String category_name = result.getString("category_name");
                int deletedCategoryIndex = result.getInt("deleted_category_index", -1);
                boolean clear_categories = result.getBoolean("clear_categories", false);

                // Either data or clearSelections must be null, only one of these arrays can be non-null at any given onFragmentResult callback
                // If data is not null, this indicates the user has created an event (either Expense or Income) on the Calendar. Data will contain the relevant values to construct the object
                // If clearSelections is not null, this indicates the user has clicked clearBtn.
                if (clearSelections != null) {

                    // If the user has checked both Calendar and Deadline boxes, we clear all CalendarEvents for the current month, and ALL deadlines
                    // Element 0 of clearSelections = calendar checkbox, Element 1 of clearSelections = deadline checkbox
                    if (clearSelections[0] && clearSelections[1]) {
                        // Clear both calendar and deadlines
                        if (monthlyExpensesMapping.containsKey(currentMonth)) {
                            monthlyExpensesMapping.get(currentMonth).clear();
                            initializeRecyclerView();
                        }
                        deadlines.clear();
                        AsyncTask.execute(new Runnable() {
                            @Override
                            public void run() {
                                CalendarEventsDAO calendarDAO = db.calendarEventsDAO();
                                DeadlineEventsDAO deadlineDAO = db.deadlineEventsDAO();
                                ((MainActivity) getContext()).clearDeadlineAlarms(deadlineDAO);
                                calendarDAO.clearCalendarEvents();
                                deadlineDAO.clearDeadlineEvents();
                            }
                        });
                    } else if (clearSelections[0]) {
                        // Clear only calendar events
                        if (monthlyExpensesMapping.containsKey(currentMonth)) {
                            monthlyExpensesMapping.get(currentMonth).clear();
                            initializeRecyclerView();
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
                                ((MainActivity) getContext()).clearDeadlineAlarms(dao);
                                dao.clearDeadlineEvents();
                                Log.i("Clearing", "Deadline Events, success");
                            }
                        });
                    }
                }

                // User has created a CalendarEvent, initialize objects accordingly
                if (data != null) {
                    int deadline_time_selections[] = result.getIntArray("time_selection");
                    LocalDate date = LocalDate.of(currentYear, currentMonth, currentDay);

                    // Deadline Event
                    if (deadlineDescription != null) {
                        Log.i("Deadline Description", deadlineDescription);
                        DeadlineEvent deadline = new DeadlineEvent(data[0], data[1], date, deadlineDescription);
                        if (deadline_time_selections != null) {
                            // Set the hour, hour type, and minute for the deadline
                            Log.i("Calendar Fragment", "Received hour selection=" + deadline_time_selections[0]
                                    + ". Minute=" + deadline_time_selections[2] +
                                    "AM/PM=" + deadline_time_selections[1]);
                            deadline.setHour(deadline_time_selections[0]);
                            deadline.setAmOrPm(deadline_time_selections[1]);
                            deadline.setMinute(deadline_time_selections[2]);
                        }
                        CalendarHelper.insertEvent(deadlines, deadline, getContext());

                        // After every calendar and deadline event addition, we have to update the database accordingly
                        AsyncTask.execute(new Runnable() {
                            @Override
                            public void run() {
                                // Since the autogenerated id key is used in setAlarmForDeadline, we have to make sure it is called after the DeadlineEvent has been inserted into the db
                                DeadlineEventsDAO deadlineDAO = db.deadlineEventsDAO();
                                DeadlineEventsEntity entity = new DeadlineEventsEntity();
                                entity.information = deadlineDescription;
                                entity.expense = data[0];
                                entity.day = date.getDayOfMonth();
                                entity.month = date.getMonthValue();
                                entity.year = date.getYear();
                                entity.hour = deadline.getHour();
                                entity.hour_type = deadline.getAmOrPm();
                                entity.minute = deadline.getMinute();
                                deadlineDAO.insert(entity);
                                Log.i("Database insertion", "Inserted deadline!");
                                ((MainActivity) getActivity()).setAlarmForDeadline(deadline);
                            }
                        });
                    }

                    // Expenses Event
                    else if (data[1] == 0) {
                        int spinnerIndex = result.getInt("category_index", -1);
                        ExpensesEvent event = new ExpensesEvent(data[0], data[1], date);
                        Log.i("Dialog Data", "Type: Expense. Amount: " + data[0]);
                        boolean defaultSelected = false;

                        if (spinnerIndex < 0) {
                            Toast.makeText(getContext(), "Error creating ExpensesEvent (invalid index). Aborting...", Toast.LENGTH_LONG).show();
                            return;
                        }
                        // "Other" category was selected
                        else if (spinnerIndex >= expenseCategories.size()) {
                            ExpenseCategory defaultCategory = new ExpenseCategory("Other");
                            event.setCategory(defaultCategory);
                            defaultSelected = true;
                        // A user-created category was selected
                        } else {
                            ExpenseCategory selectedCategory = expenseCategories.get(spinnerIndex);
                            event.setCategory(selectedCategory);
                        }

                        int categoryInfo[] = categoryMap.get(event.getCategory());
                        List<CalendarEvent> dataset = mAdapter.getEvents();
                        int pos = categoryInfo[0];
                        int count = categoryInfo[1];
                        int index = (pos + (categoryMap.size() * count));
                        dataset.set(index, event);
                        Log.i("EVENT INSERTION", "INSERTED EVENT AT INDEX=" + index);
                        mAdapter.setDataset(dataset);
                        CalendarHelper.insertEvent(monthlyExpensesMapping, event, getContext());
                        categoryInfo[1]++;
                        categoryMap.put(event.getCategory(), categoryInfo);

                        boolean finalDefaultSelected = defaultSelected;

                        // Insert the ExpensesEvent data into the database
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

                                if (finalDefaultSelected) {
                                    ExpenseCategory defaultCategory = new ExpenseCategory("Other");
                                    ExpenseCategoryDAO expenseCategoryDAO = db.expenseCategoryDAO();

                                    // "Other" category already exists within the database, do not create another one
                                    if (!expenseCategoryDAO.getCategory("Other").isEmpty()) {
                                        int category_id = expenseCategoryDAO.getCategoryId("Other");
                                        entity.category_id = category_id;

                                        // Create the "Other" category if it does not yet exist within the database
                                        // This only happens the first time the user selects the Other category for an ExpenseEvent
                                    } else {
                                        expenseCategoryDAO.createCategory(defaultCategory);
                                        entity.category_id = expenseCategoryDAO.getCategoryId("Other");
                                    }
                                } else {
                                    ExpenseCategory selectedCategory = expenseCategories.get(spinnerIndex);
                                    ExpenseCategoryDAO expenseCategoryDAO = db.expenseCategoryDAO();
                                    int category_id = expenseCategoryDAO.getCategoryId(selectedCategory.getName());
                                    entity.category_id = category_id;
                                }
                                dao.insert(entity);
                                Log.i("Database insertion", "Expense event inserted!");
                            }
                        });

                        // Income Event
                    } else if (data[0] == 0) {
                        IncomeEvent event = new IncomeEvent(data[0], data[1], date);
                        CalendarHelper.insertEvent(monthlyExpensesMapping, event, getContext());
                        int[] categoryInfo = categoryMap.get(new ExpenseCategory("Income"));
                        int pos = categoryInfo[0];
                        int count = categoryInfo[1];
                        int index = pos + (categoryMap.size() * count);
                        List<CalendarEvent> dataset = mAdapter.getEvents();
                        dataset.set(index, event);
                        mAdapter.setDataset(dataset);

                        // Insert the IncomeEvent data into the database
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
                                entity.category_id = -1;
                                dao.insert(entity);
                                Log.i("Database insertion", "Income event inserted!");
                            }
                        });
                    }
                }

                // User has clicked on the categories button, and created a new category
                if (category_name != null) {
                    Log.i("CATEGORY NAME", "Name=" + category_name);
                    ExpenseCategory newCategory = new ExpenseCategory(category_name);
                    if (expenseCategories.indexOf(newCategory) >= 0) {
                        Toast.makeText(getContext(), "Expense category already exists!", Toast.LENGTH_LONG).show();
                        return;
                    } else {
                        expenseCategories.add(newCategory);
                        categoryMap.put(newCategory, new int[]{expenseCategories.indexOf(newCategory), 1});
                        int categoryInfo[] = categoryMap.get(new ExpenseCategory("Income"));
                        categoryInfo[0] = expenseCategories.size();
                        categoryMap.put(new ExpenseCategory("Income"), categoryInfo);
                        initializeRecyclerView();
                        AsyncTask.execute(new Runnable() {
                            @Override
                            public void run() {
                                ExpenseCategoryDAO dao = db.expenseCategoryDAO();
                                dao.createCategory(newCategory);
                            }
                        });
                        Toast.makeText(getContext(), "Successfully created new expense category", Toast.LENGTH_LONG).show();
                    }
                }

                // User has clicked on the categories button, and deleted an existing category
                else if (deletedCategoryIndex >= 0) {
                    ExpenseCategory categoryToRemove = expenseCategories.get(deletedCategoryIndex);
                    expenseCategories.remove(deletedCategoryIndex);
                    categoryMap.remove(categoryToRemove);
                    int[] categoryInfo = categoryMap.get(new ExpenseCategory("Income"));
                    categoryInfo[0] = expenseCategories.size();
                    categoryMap.put(new ExpenseCategory("Income"), categoryInfo);
                    initializeRecyclerView();
                    Log.i("DELETE CATEGORY", "Deleted category at index=" + deletedCategoryIndex);

                    AsyncTask.execute(new Runnable() {
                        @Override
                        public void run() {
                            ExpenseCategoryDAO dao = db.expenseCategoryDAO();
                            dao.deleteCategory(categoryToRemove.getName());
                        }
                    });
                    Toast.makeText(getContext(), "Successfully deleted expense category", Toast.LENGTH_LONG).show();
                }

                if (clear_categories) {
                    expenseCategories.clear();
                    AsyncTask.execute(new Runnable() {
                        @Override
                        public void run() {
                            ExpenseCategoryDAO dao = db.expenseCategoryDAO();
                            dao.clearCategories();
                        }
                    });
                    Toast.makeText(getContext(), "Successfully deleted all expense categories", Toast.LENGTH_LONG).show();
                }

                // After every onFragmentResult, call the onCalendarDataPassed callback in MainActivity
                // Effectively, this is updating the monthlyExpensesMapping and deadlines data structures to equal the CalendarFragment, syncing the values between the two classes
                calendarIsUpToDate[currentMonth - 1] = false;
                dataPasser.onCalendarDataPassed(monthlyExpensesMapping, deadlines);

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
        categoryBtn = v.findViewById(R.id.categoryBtn);
        calendar = v.findViewById(R.id.calendarView);
        list = v.findViewById(R.id.recyclerView);

        // Setting the initial highlighted date to the current date
        calendar.setDateSelected(CalendarDay.today(), true);
        calendar.setHeaderTextAppearance(R.style.CalendarWidgetHeader);
        calendar.setDateTextAppearance(R.style.CalendarWidgetText);

        initializeRecyclerView();

        // Setting click listeners for buttons
        exitBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                // When exit or back button is clicked, pop this CalendarFragment from the back stack, which removes all the view associated with the CalendarFragment layout
                // Bring back all of the views in MainActivity to be visible and calls sumEventsInCurrentMonth to calculate updated budget information
                getParentFragmentManager().popBackStack();
                ((MainActivity) getActivity()).unhideMainUI();
                ((MainActivity) getActivity()).sumEventsInCurrentMonth();
            }
        });
        deadlineBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                // When the deadline button is clicked, create a DeadlineDialog instance and display it to the user
                DeadlineDialog dialog = new DeadlineDialog(deadlines);
                dialog.show(getParentFragmentManager(), "Deadlines");
            }
        });

        calendar.setOnDateChangedListener(new OnDateSelectedListener() {
            @Override
            public void onDateSelected(@NonNull MaterialCalendarView widget, @NonNull CalendarDay date, boolean selected) {
                // Every time the user selects a date in the calendar, set current month, year, and day to the user-selected date
                currentMonth = date.getMonth();
                Log.i("Month", "Selected Month: " + currentMonth);
                currentYear = date.getYear();
                currentDay = date.getDay();

                // Create a CalendarDialogFragment dialog and display it
                // The CalendarDialogFragment will provide a dialog box where the user can set the type of CalendarEvent they wish to create, amount, and information of the event
                CalendarDialogFragment dialog = new CalendarDialogFragment(expenseCategories);
                dialog.show(getParentFragmentManager(), "Add additional expenses/income");
            }
        });
        calendar.setOnMonthChangedListener(new OnMonthChangedListener() {
            @Override
            public void onMonthChanged(MaterialCalendarView widget, CalendarDay date) {
                // When the month changes from the user swiping left or right, update the current month variable
                // Also, refresh the RecyclerView list of views to display the new current month's CalendarEvents
                currentMonth = date.getMonth();
                Log.i("Month Change", "Current Month=" + currentMonth);
                currentDay = 1;

                categoryMap.clear();
                categoryMap.put(new ExpenseCategory("Income"), new int[]{expenseCategories.size(), 1});
                for (int i = 0; i < expenseCategories.size(); i++) {
                    categoryMap.put(expenseCategories.get(i), new int[]{i, 1});
                }

                HashMap<LocalDate, ArrayList<CalendarEvent>> eventsInMonth = monthlyExpensesMapping.get(currentMonth);
                List<CalendarEvent> dataset = mAdapter.clearDataset();
                CalendarHelper.categoryMapOnMonthChanged(categoryMap, eventsInMonth, expenseCategories, dataset);
                mAdapter.setDataset(dataset);
            }
        });
        clearBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                // When clear button is clicked, create and display a ClearDialog instance
                // The ClearDialog has checkboxes for CalendarEvents and DeadlineEvents, allowing users which types of events they wish to clear
                ClearDialog clearDialog = new ClearDialog();
                clearDialog.show(getParentFragmentManager(), "Clear");
            }
        });
        categoryBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                ExpenseCategoryDialog dialog = new ExpenseCategoryDialog(expenseCategories, monthlyExpensesMapping);
                dialog.show(getParentFragmentManager(), "CREATE_CATEGORY");
            }
        });
        return v;
    }

    @Override
    public void onAttach(Context context) {
        super.onAttach(context);
        dataPasser = (CalendarDataPass) context;
    }

    // Setter method for ArrayList of deadlines, used for updating CalendarFragment's deadline ArrayList from MainActivity
    public void setDeadlines(ArrayList<DeadlineEvent> deadlines) { this.deadlines = deadlines; }


    // Initializes and updates the RecyclerView that displays all calendar events
    @SuppressLint("NewApi")
    public void initializeRecyclerView() {
        if (monthlyExpensesMapping != null) {
            if (!monthlyExpensesMapping.isEmpty() && monthlyExpensesMapping.containsKey(currentMonth)) {
                // Idea is to basically iterate through the hashmap of the current month, and displaying all of the LocalDate-ArrayList<CalendarEvent> pairs in the RecyclerView
                // When the user swipes to a different month, check if the updated month has any existing calendar events
                // If so, display them
                HashMap<LocalDate, ArrayList<CalendarEvent>> events = monthlyExpensesMapping.get(currentMonth);
                int datasetSize = events.size();

                // If the total number of events in the current month is greater than 0
                if (datasetSize > 0) {
                    // Obtain an ArrayList of all LocalDates in the month with an event, and then sort this ArrayList by LocalDate chronological order

                    // Obtain a sorted ArrayList of all the LocalDates with events for this month
                    dates = CalendarHelper.getDatesWithEvents(events);

                    mDataset = new String[datasetSize];
                    int datasetCounter = 0;
                    for (int i = 0; i < dates.size(); i++) {
                        ArrayList<CalendarEvent> eventsOnDay = events.get(dates.get(i));
                        if (eventsOnDay != null && !eventsOnDay.isEmpty()) {
                            // dayInfo indexes:
                            // Element 0: Net Additional Budget
                            // Element 1: Net Expenses
                            // Element 2: Net Income
                            // Element 3: Number of ExpenseEvents
                            // Element 4: Number of IncomeEvents
                            ArrayList<Double> dayInfo = CalendarHelper.calculateTotalBudget(eventsOnDay);

                            double totalBudget = dayInfo.get(0);
                            int numberOfExpenses = dayInfo.get(3).intValue();
                            int numberOfIncome = dayInfo.get(4).intValue();
                            if (totalBudget < 0) {
                                mDataset[datasetCounter] = eventsOnDay.get(0).getMonth() + "/" + eventsOnDay.get(0).getDay() + "/"
                                        + eventsOnDay.get(0).getYear() + ": " + eventsOnDay.size() +
                                        " events (" + numberOfExpenses + " expenses, " + numberOfIncome +
                                        " income)" + " Budget: -$" + Math.abs(totalBudget);
                            } else {
                                mDataset[datasetCounter] = eventsOnDay.get(0).getMonth() + "/" + eventsOnDay.get(0).getDay() + "/"
                                        + eventsOnDay.get(0).getYear() + ": " + eventsOnDay.size() +
                                        " events (" + numberOfExpenses + " expenses, " + numberOfIncome +
                                        " income)" + " Budget: $" + totalBudget;
                            }
                            datasetCounter++;
                        }
                    }
                    // If there are no calendar events in the current month, display an empty RecyclerView list
                } else {
                    mDataset = new String[1];
                    mDataset[0] = "";
                }
            } else {
                // If there are no calendar events in the current month, display an empty RecyclerView list
                mDataset = new String[1];
                mDataset[0] = "";
            }
            mLayoutManager = new GridLayoutManager(getContext(), expenseCategories.size() + 1);
            List<CalendarEvent> monthlyDataset = CalendarHelper.obtainCurrentMonthEvents(monthlyExpensesMapping.get(currentMonth), dates);
            totalEventsInMonth = CalendarHelper.translateListIndices(monthlyDataset, categoryMap);
            mAdapter = new CalendarRecyclerAdapter(totalEventsInMonth, expenseCategories);
            list.setLayoutManager(mLayoutManager);
            list.setAdapter(mAdapter);
        }
    }
}