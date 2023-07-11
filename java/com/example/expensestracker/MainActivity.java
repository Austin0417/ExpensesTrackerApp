package com.example.expensestracker;

import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.fragment.app.FragmentManager;
import androidx.fragment.app.FragmentTransaction;
import androidx.lifecycle.LiveData;
import androidx.lifecycle.Observer;
import androidx.localbroadcastmanager.content.LocalBroadcastManager;
import androidx.room.Room;

import android.Manifest;
import android.annotation.SuppressLint;
import android.app.AlarmManager;
import android.app.PendingIntent;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.graphics.Color;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.SystemClock;
import android.text.SpannableString;
import android.text.SpannableStringBuilder;
import android.text.Spanned;
import android.text.style.BackgroundColorSpan;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.FrameLayout;
import android.widget.TextView;

import com.example.expensestracker.calendar.CalendarDataPass;
import com.example.expensestracker.calendar.CalendarEvent;
import com.example.expensestracker.calendar.CalendarFragment;
import com.example.expensestracker.calendar.DeadlineEvent;
import com.example.expensestracker.calendar.EditEvent;
import com.example.expensestracker.calendar.ExpensesEvent;
import com.example.expensestracker.calendar.IncomeEvent;
import com.example.expensestracker.dialogs.DeadlineDialog;
import com.example.expensestracker.dialogs.EditDeadlineDialog;
import com.example.expensestracker.dialogs.EditEventDialog;
import com.example.expensestracker.monthlyinfo.MonthlyInfoEntity;
import com.example.expensestracker.monthlyinfo.MonthlyInfoFragment;
import com.example.expensestracker.notifications.AlarmReceiver;
import com.google.firebase.FirebaseApp;
import com.example.expensestracker.monthlyinfo.PassMonthlyData;


import java.time.LocalDate;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.CountDownLatch;

public class MainActivity extends AppCompatActivity implements FragmentManager.OnBackStackChangedListener, PassMonthlyData, CalendarDataPass, EditEvent {
    private static final int NOTIFICATION_STATUS_CODE = 1;

    // UI Elements of the main page
    FrameLayout addMonthlyInfoFragment;
    private Button addBtn;
    private Button initializeBtn;
    private FragmentManager manager;
    private TextView overview;
    private TextView dashboardLabel;

    // Unique notification ID for sending notifications
    private int notificationId = 1;

    // Number of deadlines the user has added, obtained from the size of DeadlineEvent ArrayList
    private int deadlineCount = 0;

    // User initialized expenses, income, and budget, which is calculated from subtracting expenses from income
    private double income = 0;
    private double expenses = 0;
    private double budget = 0;

    // Total net gain from all calendar events for both expense and income
    private double netExpenseFromCalendar = 0;
    private double netIncomeFromCalendar = 0;

    // The additional expense/income gain from calendar events if the user last added any new events
    private double additionalExpensesFromCalendar = 0;
    private double additionalIncomeFromCalendar = 0;

    // Boolean variables to keep track of the current fragment that is active, if any. This is used for the onBackPressed listener.
    private boolean monthlyInfoFragmentActive, calendarEventFragmentActive;

    // Data structs for storing all user added events in the current month
    // Nested Hashmap<LocalDate, ArrayList<CalendarEvent>> to support multiple events on a single day
    private HashMap<Integer, HashMap<LocalDate, ArrayList<CalendarEvent>>> monthlyMapping;

    // Simple ArrayList<DeadlineEvent> which stores all user added deadlines, regardless of month
    private ArrayList<DeadlineEvent> deadlines;

    // HashMap data structure that holds the specific id and Intent object for each deadline, will be used for cancelling alarms
    // The id (key) is the integer value passed into the 'requestCode' argument of PendingIntent, and Intent (value) is the intent object used in the PendingIntent
    private HashMap<Integer, Intent> deadlineIntentMapping = new HashMap<Integer, Intent>();

    private boolean upToDate = true;
    private boolean notificationsEnabled = true;
    private ExpensesTrackerDatabase db;
    private AlarmManager alarmManager;

    // References to child fragments/dialogs of MainActivity
    // Allows us to call methods such as updating UI and syncing data from the MainActivity
    private CalendarFragment calendar;
    private DeadlineDialog deadlineDialog;
    private MonthlyInfoFragment monthlyInfo = new MonthlyInfoFragment();


    @Override
    public void onRequestPermissionsResult(int requestCode, String[] permissions, int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if (requestCode == 1) {
            if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                notificationsEnabled = true;
            } else {
                notificationsEnabled = false;
            }
        }
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main2);

        // Initializing all UI elements and obtaining a reference to them
        addBtn = findViewById(R.id.addBtn);
        initializeBtn = findViewById(R.id.initializeBtn);
        overview = findViewById(R.id.overviewText);
        dashboardLabel = findViewById(R.id.dashboardLabel);
        addMonthlyInfoFragment = findViewById(R.id.monthlyInfoFragment);
        manager = getSupportFragmentManager();
        manager.addOnBackStackChangedListener(this);


        alarmManager = (AlarmManager) getSystemService(Context.ALARM_SERVICE);

        Log.i("Current Month", String.valueOf(Calendar.getInstance().get(Calendar.MONTH) + 1));

        // Setting on click listeners for the add and initialize expenses buttons
        addBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                calendar = new CalendarFragment();
                calendar.setDatabase(db);
                hideMainUI();
                calendarEventFragmentActive = true;
                monthlyInfoFragmentActive = false;
                FragmentTransaction transaction = manager.beginTransaction();
                transaction.replace(R.id.calendarFragment, calendar);
                transaction.addToBackStack("Calendar");
                transaction.commit();
            }
        });
        initializeBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                hideMainUI();
                monthlyInfoFragmentActive = true;
                calendarEventFragmentActive = false;
                FragmentTransaction transaction = manager.beginTransaction();
                transaction.replace(addMonthlyInfoFragment.getId(), monthlyInfo);
                transaction.addToBackStack("Monthly Information");
                transaction.commit();
            }
        });
        // Requesting notification permissions if permissions are not allowed
        if (ActivityCompat.checkSelfPermission(this, android.Manifest.permission.POST_NOTIFICATIONS) != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.POST_NOTIFICATIONS}, NOTIFICATION_STATUS_CODE);
            return;
        }
        FirebaseApp.initializeApp(this);
        initializeDatabase();
    }

    @Override
    public void onBackPressed() {
        super.onBackPressed();
        if (monthlyInfoFragmentActive) {
            monthlyInfoFragmentActive = false;
            double expenses = monthlyInfo.getExpenses();
            double income = monthlyInfo.getIncome();
            if (expenses >= 0 && income >= 0) {
                monthlyInfo.getMonthlyDataPasser().onDataPassed(monthlyInfo.getExpenses(), monthlyInfo.getIncome());
            }
            getSupportFragmentManager().popBackStack();
            unhideMainUI();
        } else if (calendarEventFragmentActive) {
            calendarEventFragmentActive = false;
            unhideMainUI();
            sumEventsInCurrentMonth();
            getSupportFragmentManager().popBackStack();
        }
    }


    public void initializeDatabase() {
        db = Room.databaseBuilder(getApplicationContext(), ExpensesTrackerDatabase.class, "ExpensesTracker").build();
        Log.i("Database creation", "Success!");

        // CountDownLatch for synchronization to ensure that retrieving deadlines from the database is completed first, before checking for pending DeadlineEvents in deletePassedDeadlines
        CountDownLatch latch = new CountDownLatch(3);
        AsyncTask.execute(new Runnable() {
            @Override
            public void run() {
                retrieveMonthlyInfoFromDatabase(latch);
                retrieveCalendarEventsFromDatabase(latch);
                retrieveDeadlinesFromDatabase(latch);
            }
        });
        try {
            latch.await();
        } catch (InterruptedException e) {
            e.printStackTrace();
            throw new RuntimeException(e);
        }

        deletePassedDeadlines();
    }

    public void setDeadlineDialog(DeadlineDialog dialog) {
        deadlineDialog = dialog;
    }

    // Grabs monthly expense and income value from previous state
    public void retrieveMonthlyInfoFromDatabase(CountDownLatch latch) {
        MonthlyInfoDAO monthlyInfoDAO = new MonthlyInfoDAO_Impl(db);
        List<MonthlyInfoEntity> monthlyInfo = monthlyInfoDAO.getMonthlyInfo();
        if (monthlyInfo != null && !monthlyInfo.isEmpty()) {
            Log.i("Monthly Info", "Fecthing saved data: Expenses = " + monthlyInfo.get(0).expenses + " Income = " + monthlyInfo.get(0).income);
            income = monthlyInfo.get(0).income;
            expenses = monthlyInfo.get(0).expenses;
            double additionalBudgetFromCalendar = Math.round((additionalIncomeFromCalendar - additionalExpensesFromCalendar) * 100.0) / 100.0;
            budget = (Math.round((income - expenses) * 100.0) / 100.0) + additionalBudgetFromCalendar;
        } else {
            Log.i("Monthly Info", "No valid info to fetch!");
        }
        latch.countDown();
    }
    // Grabs all deadline events from previous state
    @SuppressLint("NewAPI")
    public void retrieveDeadlinesFromDatabase(CountDownLatch latch) {
        DeadlineEventsDAO dao = new DeadlineEventsDAO_Impl(db);
        List<DeadlineEventsEntity> deadlinesInDatabase = dao.getDeadlineEvents();
        if (deadlinesInDatabase != null && !deadlinesInDatabase.isEmpty()) {
            Log.i("Deadlines", "Fetching deadlines...");
            deadlines = new ArrayList<DeadlineEvent>();
            for (int i = 0; i < deadlinesInDatabase.size(); i++) {
                int day = deadlinesInDatabase.get(i).day;
                int month = deadlinesInDatabase.get(i).month;
                int year = deadlinesInDatabase.get(i).year;
                int id = deadlinesInDatabase.get(i).key;
                int hour = deadlinesInDatabase.get(i).hour;
                int hour_type = deadlinesInDatabase.get(i).hour_type;
                int minute = deadlinesInDatabase.get(i).minute;
                LocalDate date = LocalDate.of(year, month, day);
                double expense = deadlinesInDatabase.get(i).expense;
                String information = deadlinesInDatabase.get(i).information;
                DeadlineEvent deadline = new DeadlineEvent(expense, 0, date, information);
                deadline.setHour(hour);
                deadline.setAmOrPm(hour_type);
                deadline.setMinute(minute);
                deadlines.add(deadline);

                // Reconstruct all of the Intent objects associated with the PendingIntents for each alarm from the previous state
                // We then add add the key-value pair to the Intent mapping, with the queried id as the key, and Intent object as the value
                Intent intent = new Intent(MainActivity.this, AlarmReceiver.class);
                intent.setAction("com.example.expensestracker.ACTION_TRIGGER_ALARM");
                intent.putExtra("year", year);
                intent.putExtra("month", month);
                intent.putExtra("day", day);
                intent.putExtra("information", information);
                intent.putExtra("amount", expense);
                deadlineIntentMapping.put(id, intent);
            }
            deadlineCount = deadlines.size();
            //overview.setText(String.join("", generateUpdatedText(expenses, income, budget, deadlineCount)));
        } else {
            Log.i("Deadlines", "Could not fetch deadlines");
        }
        latch.countDown();
    }

    // Grabs all calendar events from previous state, and initializes local HashMap data struct with the data
    @SuppressLint("NewAPI")
    public void retrieveCalendarEventsFromDatabase(CountDownLatch latch) {
        CalendarEventsDAO dao = new CalendarEventsDAO_Impl(db);
        List<CalendarEventsEntity> calendarEvents = dao.getCalendarEvents();
        if (calendarEvents != null) {
            monthlyMapping = new HashMap<Integer, HashMap<LocalDate, ArrayList<CalendarEvent>>>();
            for (int i = 0; i < calendarEvents.size(); i++) {
                int year = calendarEvents.get(i).year;
                int month = calendarEvents.get(i).month;
                int day = calendarEvents.get(i).day;
                double expenses = calendarEvents.get(i).expense;
                double income = calendarEvents.get(i).income;
                LocalDate date = LocalDate.of(year, month, day);
                netExpenseFromCalendar += expenses;
                netIncomeFromCalendar += income;
                if (monthlyMapping.containsKey(month)) {
                    HashMap<LocalDate, ArrayList<CalendarEvent>> monthlyEvents = monthlyMapping.get(month);
                    if (monthlyEvents.containsKey(date)) {
                        if (income == 0) {
                            monthlyEvents.get(date).add(new ExpensesEvent(expenses, income, date));
                        } else {
                            monthlyEvents.get(date).add(new IncomeEvent(expenses, income, date));
                        }
                    } else {
                        ArrayList<CalendarEvent> dayEvents = new ArrayList<CalendarEvent>();
                        if (income == 0) {
                            dayEvents.add(new ExpensesEvent(expenses, income, date));
                        } else {
                            dayEvents.add(new IncomeEvent(expenses, income,date));
                        }
                        monthlyEvents.put(date, dayEvents);
                        monthlyMapping.put(month, monthlyEvents);
                    }
                } else {
                    HashMap<LocalDate, ArrayList<CalendarEvent>> eventsInMonth = new HashMap<LocalDate, ArrayList<CalendarEvent>>();
                    ArrayList<CalendarEvent> dayEvents = new ArrayList<CalendarEvent>();
                    if (income == 0) {
                        dayEvents.add(new ExpensesEvent(expenses, income, date));
                    } else {
                        dayEvents.add(new IncomeEvent(expenses, income, date));
                    }
                    eventsInMonth.put(date, dayEvents);
                    monthlyMapping.put(month, eventsInMonth);
                }
            }
            additionalExpensesFromCalendar = netExpenseFromCalendar - additionalExpensesFromCalendar;
            additionalIncomeFromCalendar = netIncomeFromCalendar - additionalIncomeFromCalendar;
            budget += Math.round((additionalIncomeFromCalendar - additionalExpensesFromCalendar) * 100.0) / 100.0;
            if (deadlines != null && !deadlines.isEmpty()) {
                overview.setText(String.join("", generateUpdatedText(expenses, income, budget, deadlines.size())));
            } else {
                overview.setText(String.join("", generateUpdatedText(expenses, income, budget, 0)));
            }
        }
        latch.countDown();
    }

    @SuppressLint("NewApi")
    // Method to check for any DeadlineEvents pending deletion (this happens when the deadline alarm has gone off)
    // Delete the DeadlineEvent from the database upon app startup
    public void deletePassedDeadlines() {
        // TODO Add functionality for multiple pending deadlines, try adding some counter into SharedPreferences keeping track of the number of pending DeadlineEvents
        // TODO Seems that the LiveData of DeadlineEventEntities onChanged is never called
        if (deadlines != null) {
            SharedPreferences sharedPreferences = getSharedPreferences("deadline_to_remove", Context.MODE_PRIVATE);
            int numberOfDeadlines = sharedPreferences.getInt("number_of_deadlines", 0);
            for (int i = 0; i < numberOfDeadlines; i++) {
                int day = sharedPreferences.getInt("day" + (i + 1), -1);
                int month = sharedPreferences.getInt("month" + (i + 1), -1);
                int year = sharedPreferences.getInt("year" + (i + 1), -1);
                String information = sharedPreferences.getString("information" + (i + 1), "");
                double amount = Double.longBitsToDouble(sharedPreferences.getLong("amount" + (i + 1), Double.doubleToLongBits(-1)));

                if (day < 0 || month < 0 || year < 0 || information.equals("") || amount < 0) {
                    Log.i("Pending Deadlines", "Could not retrieve info for deadline pending deletion");
                    continue;
                }
                AsyncTask.execute(new Runnable() {
                    @Override
                    public void run() {
                        DeadlineEventsDAO dao = new DeadlineEventsDAO_Impl(db);
                        dao.deleteDeadlineEvent(amount, information, month, day, year);
                    }
                });
                int index = deadlines.indexOf(new DeadlineEvent(amount, 0, LocalDate.of(year, month, day), information));
                if (index >= 0) {
                    deadlines.remove(index);
                }
            }
            SharedPreferences.Editor editor = sharedPreferences.edit();
            editor.clear();
            editor.apply();
            overview.setText(String.join("", generateUpdatedText(expenses, income, budget, deadlines.size())));
        } else {
            Log.i("Deadlines", "No deadlines were set");
        }
    }

    // Helper function to synchronize the ArrayList of deadlines across 3 class instances
    // Syncs deadlines between MainActivity, CalendarFragment, and DeadlineDialog
    // Necessary because otherwise, changes made in MainActivity's ArrayList won't be reflected in the other classes
    public void synchronizeDeadlines() {
        if (calendar != null) {
            calendar.setDeadlines(deadlines);
        }
        if (deadlineDialog != null) {
            deadlineDialog.setDeadlines(deadlines);
            deadlineDialog.initializeDeadlinesView();
        }
    }

    @SuppressLint("NewAPI")
    // Method to set an alarm for a deadline. Automatically called by CalendarFragment after successfully adding a DeadlineEvent
    public void setAlarmForDeadline(DeadlineEvent deadline) {
        AsyncTask.execute(new Runnable() {
            @Override
            public void run() {
                // Create the intent object, and insert the necessary extras into it
                Intent alarmIntent = new Intent(MainActivity.this, AlarmReceiver.class);
                alarmIntent.setAction("com.example.expensestracker.ACTION_TRIGGER_ALARM");
                alarmIntent.putExtra("year", deadline.getYear());
                alarmIntent.putExtra("month", deadline.getMonth());
                alarmIntent.putExtra("day", deadline.getDay());
                alarmIntent.putExtra("information", deadline.getInformation());
                alarmIntent.putExtra("amount", deadline.getExpenses());

                // Query the database, and obtain the id for the corresponding deadline
                DeadlineEventsDAO dao = new DeadlineEventsDAO_Impl(db);
                int id = dao.getUUID(deadline.getAmount(), deadline.getInformation(), deadline.getMonth(), deadline.getDay(), deadline.getYear());
                Log.i("Deadline ID", "ID=" + id);

                // Use the id we obtained to insert a key-value pair of the id (as the key) and intent object (as the value)
                deadlineIntentMapping.put(id, alarmIntent);

                // Pass id as an argument into PendingIntent. Since the id is autogenerated in the database, this ensures a unique PendingIntent for each DeadlineEvent
                // This allows us to easily remove an alarm for a deadline if the user chooses to remove a specific DeadlineEvent
                PendingIntent pIntent = PendingIntent.getBroadcast(MainActivity.this, id, alarmIntent, PendingIntent.FLAG_UPDATE_CURRENT);

                Calendar endDate = Calendar.getInstance();
                endDate.set(deadline.getYear(), deadline.getMonth() - 1, deadline.getDay());
                if (deadline.getAmOrPm() == CalendarEvent.AM) {
                    if (deadline.getHour() == 12) {
                        endDate.set(Calendar.HOUR_OF_DAY, deadline.getHour() + 12);
                    } else {
                        endDate.set(Calendar.HOUR_OF_DAY, deadline.getHour());
                    }
                } else {
                    if (deadline.getHour() == 12) {
                        endDate.set(Calendar.HOUR_OF_DAY, deadline.getHour());
                    } else {
                        endDate.set(Calendar.HOUR_OF_DAY, deadline.getHour() + 12);
                    }
                }
                endDate.set(Calendar.MINUTE, deadline.getMinute());

                long triggerTime = endDate.getTimeInMillis() - System.currentTimeMillis();
                Log.i("Trigger time", "Trigger time=" + triggerTime);

                alarmManager.set(AlarmManager.ELAPSED_REALTIME_WAKEUP, SystemClock.elapsedRealtime() + 10000, pIntent);
            }
        });

    }

    @Override
    // This is called everytime the user returns to the main page.
    // We want to update the information every time the main page is returned to.
    public void onBackStackChanged() {
        overview.setText(String.join("", generateUpdatedText(this.expenses, this.income, this.budget, deadlineCount)));
    }


    @Override
    // Abstract interface method onDataPassed is called when returning from the MonthlyInfoFragment (popBackStack() is called)
    // MonthlyInfoFragment is where the user initially sets their monthly expenses and income
    public void onDataPassed(double expenses, double income) {
        setMonthlyInfo(expenses + additionalExpensesFromCalendar, income + additionalIncomeFromCalendar);
        overview.setText(String.join("", generateUpdatedText(this.expenses, this.income, this.budget, deadlineCount)));

        AsyncTask.execute(new Runnable() {
            @Override
            public void run() {
                // Updating monthly_info table in the database with the new information
                // monthly_info has only one row at any given time, with two columns representing the monthly expense and monthly income
                MonthlyInfoDAO monthlyDAO = db.monthlyInfoDAO();
                monthlyDAO.clearMonthlyInfo();
                MonthlyInfoEntity newMonthlyData = new MonthlyInfoEntity();
                newMonthlyData.income = income + additionalIncomeFromCalendar;
                newMonthlyData.expenses = expenses + additionalExpensesFromCalendar;
                monthlyDAO.insert(newMonthlyData);
                Log.i("Monthly Info DAO", "Successfully inserted new data");
            }
        });
    }

    @Override
    // Same idea as onDataPassed, but for CalendarFragment instead
    // This passes back the hashmap that keeps track of events in all of the months back to the main page
    public void onCalendarDataPassed(HashMap<Integer, HashMap<LocalDate, ArrayList<CalendarEvent>>> mapping, ArrayList<DeadlineEvent> deadlines) {
        monthlyMapping = mapping;
        this.deadlines = deadlines;
        if (this.deadlines != null) {
            deadlineCount = this.deadlines.size();
        }
        else {
            Log.i("Failure", "Could not process mapping data");
        }
    }
    @Override
    @SuppressLint("NewAPI")
    // This method is called via a callback from an EditEvent object
    // In this case, an EditEvent object in the CustomAdapter for the RecyclerView in CalendarFragment has called sendCalendarEventDate
    // The EditEvent object calls this method after the user has clicked an element within the RecyclerView list
    public void sendCalendarEventDate(int month, int year, int day) {
        Log.i("Send Date", month + "/" + day + "/" + year);

        // Using the month value, we obtain the hashmap for that specific month from monthlyMapping
        // We obtain the ArrayList of all events on that particular day by using the month, year, and day to query the particular month HashMap by using a LocalDate with those parameters
        HashMap<LocalDate, ArrayList<CalendarEvent>> currentMonthEvents = monthlyMapping.get(month);
        ArrayList<CalendarEvent> events = currentMonthEvents.get(LocalDate.of(year, month, day));

        // Create an EditEventDialog, passing the obtained ArrayList of CalendarEvents to the dialog
        // Show the dialog
        EditEventDialog dialog = new EditEventDialog(events);
        dialog.show(getSupportFragmentManager(), "Edit Event");
    }

    @SuppressLint("NewAPI")
    @Override
    // Using the arguments passed from clicking on an element in DeadlineDialog's RecyclerView, make a DeadlineEvent object
    // Compare this object with the rest of the DeadlineEvent objects in the DeadlineEvent ArrayList
    public void sendDeadlineEventDate(double amount, String information, int month, int year, int day, int hour, int minute, String hourType) {
        DeadlineEvent targetDeadline = new DeadlineEvent(amount, 0, LocalDate.of(year, month, day), information);
        targetDeadline.setHour(hour);
        targetDeadline.setMinute(minute);
        if (hourType.equals("AM")) {
            targetDeadline.setAmOrPm(CalendarEvent.AM);
        } else {
            targetDeadline.setAmOrPm(CalendarEvent.PM);
        }
        for (int i = 0; i < deadlines.size(); i++) {
            if (deadlines.get(i).equals(targetDeadline)) {
                // DeadlineEvent object is equal to an object in the ArrayList
                // Proceed with making an EditDeadlineDialog and display this to the user
                // We also pass the DeadlineEvent object within the ArrayList to the constructor of EditDeadlineDialog and the index in the ArrayList
                // The object argument provides a reference so that any changes we make to this object is reflected back in the ArrayList, and the index is for in the case of deleting
                EditDeadlineDialog dialog = new EditDeadlineDialog(i, deadlines);
                dialog.show(getSupportFragmentManager(), "Edit Deadline");
                return;
            }
        }
        Log.i("Not Found", "Couldn't locate deadline in current list");
    }

    @Override
    // Called from EditEventDialog when the user confirms an edit to an existing CalendarEvent after selecting a particular date
    // In MainActivity, we simply update the information in the database, while the CalendarEvent object that was modified was modified in EditEventDialog
    public void modifyCalendarEvent(CalendarEvent targetEvent, double amount) {
        Log.i("New Amount", targetEvent.getMonth() + "/" + targetEvent.getDay() + "/" + targetEvent.getYear() + ": $" + amount);

        // This simply updates the RecyclerView to display the new amount set by the user
        if (calendar != null) {
            calendar.updateRecyclerView();
        }
        AsyncTask.execute(new Runnable() {
            @Override
            public void run() {
                // Here we utilize a custom query to update the event stored in the database with the new amount
                // Two separate methods, depending on whether the event is of class ExpensesEvent or IncomeEvent
                CalendarEventsDAO dao = new CalendarEventsDAO_Impl(db);
                if (targetEvent instanceof ExpensesEvent) {
                    dao.updateExpenseEvent(amount, targetEvent.getMonth(), targetEvent.getDay(), targetEvent.getYear());
                } else {
                    dao.updateIncomeEvent(amount, targetEvent.getMonth(), targetEvent.getDay(), targetEvent.getYear());
                }
            }
        });
    }

    @Override
    // Called from EditDeadlineDialog via callback when the user confirms an edit to an existing DeadlineEvent
    // When the user modifies a deadline, we will actually create a new deadline with the updated info, and delete the old deadline alarm
    // This is done because there is no way to update the information in an Intent object after it has been bound to the PendingIntent, so we have to create a new one entirely
    public void modifyDeadlineEvent(DeadlineEvent targetDeadline, String previousInformation, double previousAmount, int index) {
        synchronizeDeadlines();

        // Database operation updating DeadlineEvent information
        AsyncTask.execute(new Runnable() {
            @Override
            public void run() {
                // First step is to cancel the old deadline alarm with the outdated information
                // Get the id of the particular DeadlineEvent, use this id to obtain the specific Intent object from the HashMap
                // With the id and Intent object, create the exact PendingIntent object that was used to set the alarm, to cancel the alarm
                DeadlineEventsDAO dao = new DeadlineEventsDAO_Impl(db);
                cancelDeadlineAlarm(targetDeadline, dao);

                // Database query to update the DeadlineEvent in the database with updated information
                // Call setAlarmForDeadline method to set a new alarm with a new Intent object that contains the updated information as extras
                dao.updateDeadlineEvent(targetDeadline.getAmount(),
                        targetDeadline.getInformation(),
                        targetDeadline.getMonth(),
                        targetDeadline.getDay(),
                        targetDeadline.getYear(),
                        targetDeadline.getHour(),
                        targetDeadline.getMinute(),
                        targetDeadline.getAmOrPm(),
                        previousInformation);
                setAlarmForDeadline(targetDeadline);
                Log.i("Deadline Update", "Success");
            }
        });
    }

    @Override
    @SuppressLint("NewAPI")
    // Called from EditDeadlineDialog when the user deletes an existing CalendarEvent
    public void deleteCalendarEvent(CalendarEvent selectedEvent) {
        // Check and see if after deletion of selected CalendarEvent, there are no remaining events for this day
        // If so, remove this key-value pair from the HashMap
        if (monthlyMapping.containsKey(selectedEvent.getMonth())) {
            HashMap<LocalDate, ArrayList<CalendarEvent>> eventsOnDay = monthlyMapping.get(selectedEvent.getMonth());
            LocalDate targetDate = LocalDate.of(selectedEvent.getYear(), selectedEvent.getMonth(), selectedEvent.getDay());
            if (eventsOnDay.containsKey(targetDate)) {
                ArrayList<CalendarEvent> events = eventsOnDay.get(targetDate);
                if (events.isEmpty()) {
                    eventsOnDay.remove(targetDate);
                }
            }
        }

        // We then update the RecyclerView in CalendarFragment after removing the specified DeadlineEvent from the ArrayList
        if (calendar != null) {
            calendar.updateRecyclerView();
        }

        // Database operation deleting CalendarEvent
        AsyncTask.execute(new Runnable() {
            @Override
            public void run() {
                CalendarEventsDAO dao = new CalendarEventsDAO_Impl(db);
                if (selectedEvent.isExpense()) {
                    dao.deleteExpenseEvent(selectedEvent.getExpenses(), selectedEvent.getDay(), selectedEvent.getMonth(), selectedEvent.getYear());
                } else {
                    dao.deleteIncomeEvent(selectedEvent.getIncome(), selectedEvent.getDay(), selectedEvent.getMonth(), selectedEvent.getYear());
                }
            }
        });
    }

    @Override
    // Called when the user deletes an existing DeadlineEvent
    public void deleteDeadlineEvent(int index, DeadlineEvent targetDeadline) {
        // Database operation deleting DeadlineEvent
        AsyncTask.execute(new Runnable() {
            @Override
            public void run() {
                // Obtain the id (from the database) associated with the DeadlineEvent to be deleted.
                // This is the same integer value that was used to initially set the alarm for the deadline
                // Also access the HashMap<Integer, Intent> using the given id as key, this gives the exact intent that was associated with the PendingIntent for setting the alarm

                DeadlineEventsDAO dao = new DeadlineEventsDAO_Impl(db);
                cancelDeadlineAlarm(targetDeadline, dao);
                dao.deleteDeadlineEvent(targetDeadline.getAmount(), targetDeadline.getInformation(), targetDeadline.getMonth(), targetDeadline.getDay(), targetDeadline.getYear());
            }
        });

        deadlines.remove(index);
        deadlineCount = deadlines.size();
        synchronizeDeadlines();
    }

    public void cancelDeadlineAlarm(DeadlineEvent deadline, DeadlineEventsDAO dao) {
        int id = dao.getUUID(deadline.getAmount(), deadline.getInformation(), deadline.getMonth(), deadline.getDay(), deadline.getYear());
        if (deadlineIntentMapping.containsKey(id)) {
            Intent intent = deadlineIntentMapping.get(id);
            PendingIntent pIntent = PendingIntent.getBroadcast(this, id, intent, PendingIntent.FLAG_UPDATE_CURRENT);
            alarmManager.cancel(pIntent);
            Log.i("Delete Deadline", "Canceled alarm with id=" + id);
        } else {
            Log.i("Cancel Deadline", "Couldn't cancel deadline alarm (invalid deadline)");
        }
    }

    public void clearDeadlineAlarms(DeadlineEventsDAO dao) {
        for (Map.Entry<Integer, Intent> element : deadlineIntentMapping.entrySet()) {
            int id = element.getKey();
            Intent intent = element.getValue();
            PendingIntent pIntent = PendingIntent.getBroadcast(MainActivity.this, id, intent, PendingIntent.FLAG_UPDATE_CURRENT);
            alarmManager.cancel(pIntent);
        }
    }

    // We iterate through the hashmap entry for the current month, and find the sum of all of the expenses and income that the user added for the current month
    // Update the information displayed on the main page accordingly
    public void sumEventsInCurrentMonth() {
        if (monthlyMapping != null && !monthlyMapping.isEmpty()) {
            double totalAdditionalMonthlyExpenses = 0;
            double totalAdditionalMonthlyIncome = 0;
            Calendar calendar = Calendar.getInstance();
            int currentMonth = calendar.get(Calendar.MONTH) + 1;
            HashMap<LocalDate, ArrayList<CalendarEvent>> currentMonthEvents = monthlyMapping.get(currentMonth);
            if (currentMonthEvents != null && !currentMonthEvents.isEmpty()) {
                for (Map.Entry<LocalDate, ArrayList<CalendarEvent>> entry : currentMonthEvents.entrySet()) {
                    ArrayList<CalendarEvent> events = entry.getValue();
                    ArrayList<Double> dayExpensesAndIncome = CalendarFragment.calculateTotalBudget(events);
                    for (int i = 0; i < events.size(); i++) {
                        notificationsEnabled = true;
                        if (events.get(i) instanceof ExpensesEvent) {
                            totalAdditionalMonthlyExpenses += events.get(i).getExpenses();
                        } else if (events.get(i) instanceof IncomeEvent) {
                            totalAdditionalMonthlyIncome += events.get(i).getIncome();
                        }
                    }
                }
            }
            // Difference between net and additional:
            // Net is the total additional expense/income from the given month
            // Additional is the amount that was added since the last user-added event
            // Ex. If a user adds 4 events: 2 income, and 2 expense event.
            // The expense event is worth $50 (-50) each, and the income event is worth $100 (+100) each
            // Net expense would be -$100, and net income would +$200.
            // But assume that the user added these events not all at once, but in two separate sessions
            // In the first session, they add 1 expense/income, and the second, they add the remaining 1 expense/income
            // After first session, net expense = additionalMonthlyExpenses = 50, net income = additionalMonthlyIncome = 100
            // After second session, net expense = 100, net income = 200. AdditionalMonthlyExpenses = (net expense) - 50 = 50. AdditionalMonthlyIncome = (net income) - 100 = 100
            // Thus, additional represents the amount gained since the last calendar event update

            additionalExpensesFromCalendar = totalAdditionalMonthlyExpenses - netExpenseFromCalendar;
            additionalIncomeFromCalendar = totalAdditionalMonthlyIncome - netIncomeFromCalendar;
            netExpenseFromCalendar = totalAdditionalMonthlyExpenses;
            netIncomeFromCalendar = totalAdditionalMonthlyIncome;
            budget += Math.round((additionalIncomeFromCalendar - additionalExpensesFromCalendar) * 100.0) / 100.0;
            Log.i("New Budget", String.valueOf(budget));
        }
        overview.setText(String.join("",generateUpdatedText(expenses, income, budget, deadlineCount)));
    }


    // Helper method to easily update the main page text with updated information
    public String[] generateUpdatedText(double expenses, double income, double budget, int deadlineCount) {
        String[] res = new String[7];
        res[0] = "Monthly Expenses: $" + expenses;
        res[1] = "\nMonthly Income: $" + income;
        res[2] = "\nAdditional Expenses From Calendar: $" + netExpenseFromCalendar;
        res[3] = "\nAdditional Income From Calendar: $" + netIncomeFromCalendar;
        res[4] = "\nAdditional Budget From Calendar: $" + Math.round((netIncomeFromCalendar - netExpenseFromCalendar) * 100.0) / 100.0;
        res[5] = "\nTotal Available Budget: $" + budget;
        res[6] = "\n" + deadlineCount + " Upcoming Deadlines";
        return res;
    }

    public void hideMainUI() {
        overview.setVisibility(View.INVISIBLE);
        addBtn.setVisibility(View.INVISIBLE);
        initializeBtn.setVisibility(View.INVISIBLE);
        dashboardLabel.setVisibility(View.INVISIBLE);
    }

    public void unhideMainUI() {
        overview.setVisibility(View.VISIBLE);
        addBtn.setVisibility(View.VISIBLE);
        initializeBtn.setVisibility(View.VISIBLE);
        dashboardLabel.setVisibility(View.VISIBLE);
    }

    // Helper method to encapsulate updating data members with new values
    public void setMonthlyInfo(double expenses, double income) {
        this.expenses = expenses;
        this.income = income;
        double additionalBudgetFromCalendar = Math.round((additionalIncomeFromCalendar - additionalExpensesFromCalendar) * 100.0) / 100.0;
        this.budget = (Math.round((income - expenses) * 100.0) / 100.0) + additionalBudgetFromCalendar;
    }

    public HashMap<Integer, HashMap<LocalDate, ArrayList<CalendarEvent>>> getMonthlyMapping() { return monthlyMapping; }

    public ArrayList<DeadlineEvent> getDeadlines() {
        return deadlines;
    }

// Method to clear all data, both monthly and calendar
    public void resetInfo() {
        if (monthlyMapping != null && !monthlyMapping.isEmpty()) {
            monthlyMapping.clear();
        }
        if (deadlines != null && !deadlines.isEmpty()) {
            deadlines.clear();
        }

        // Database operation that wipes information from all 3 tables (monthly_info, calendar_events, deadline_events)
        // Essentially a full reset of all user-set info
        AsyncTask.execute(new Runnable() {
            @Override
            public void run() {
                // TODO Iterate through deadlineIntentMapping and cancel all existing alarms
                MonthlyInfoDAO monthlyInfoDAO = db.monthlyInfoDAO();
                CalendarEventsDAO calendarDAO = db.calendarEventsDAO();
                DeadlineEventsDAO deadlineDAO = db.deadlineEventsDAO();

                // Loop through the deadline id and intent HashMap, cancel all alarms set by alarmManager
                clearDeadlineAlarms(deadlineDAO);
                deadlineIntentMapping.clear();

                // Wipe all data from all 3 tables in the Room Database
                monthlyInfoDAO.clearMonthlyInfo();
                calendarDAO.clearCalendarEvents();
                deadlineDAO.clearDeadlineEvents();
            }
        });

        income = 0;
        expenses = 0;
        budget = 0;
        deadlineCount = 0;
        additionalExpensesFromCalendar = 0;
        additionalIncomeFromCalendar = 0;
        netExpenseFromCalendar = 0;
        netIncomeFromCalendar = 0;
        Log.i("Main Info", "Expenses: " + expenses + "\nIncome: " + income + "\nBudget: " + budget);
        overview.setText(String.join("", generateUpdatedText(expenses, income, budget, 0)));
        unhideMainUI();
    }
}