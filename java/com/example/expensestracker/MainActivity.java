package com.example.expensestracker;

import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.app.NotificationCompat;
import androidx.core.app.NotificationManagerCompat;
import androidx.core.content.ContextCompat;
import androidx.fragment.app.FragmentManager;
import androidx.fragment.app.FragmentTransaction;
import androidx.lifecycle.LiveData;
import androidx.lifecycle.Observer;
import androidx.room.Room;

import android.Manifest;
import android.annotation.SuppressLint;
import android.app.Activity;
import android.app.AlarmManager;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.pm.PackageManager;
import android.graphics.Color;
import android.os.AsyncTask;
import android.os.Bundle;
import android.text.SpannableString;
import android.text.SpannableStringBuilder;
import android.text.Spanned;
import android.text.style.BackgroundColorSpan;
import android.text.style.ForegroundColorSpan;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.FrameLayout;
import android.widget.TextView;

import com.google.firebase.FirebaseApp;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class MainActivity extends AppCompatActivity implements FragmentManager.OnBackStackChangedListener, PassMonthlyData, CalendarDataPass {
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
    // The net gain from calendar events if the user last added any new events
    private double additionalExpensesFromCalendar = 0;
    private double additionalIncomeFromCalendar = 0;
    private MonthlyInfoFragment monthlyInfo = new MonthlyInfoFragment();
    // Data structs for storing all user added events in the current month
    // Nested Hashmap<LocalDate, ArrayList<CalendarEvent>> to support multiple events on a single day
    private HashMap<Integer, HashMap<LocalDate, ArrayList<CalendarEvent>>> monthlyMapping;
    // Simple ArrayList<DeadlineEvent> which stores all user added deadlines, regardless of month
    private ArrayList<DeadlineEvent> deadlines;
    boolean upToDate = true;
    private boolean notificationsEnabled = true;
    private ExpensesTrackerDatabase db;
    private AlarmManager alarmManager;
    private AlarmReceiver alarmReceiver;
    private Intent alarmIntent;
    private PendingIntent pendingIntent;



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
        alarmIntent = new Intent(this, AlarmReceiver.class);
        alarmIntent.setAction("com.example.expensestracker.ACTION_TRIGGER_ALARM");
        //pendingIntent = PendingIntent.getBroadcast(this, 0, alarmIntent, PendingIntent.FLAG_UPDATE_CURRENT);


        Log.i("Current Month", String.valueOf(Calendar.getInstance().get(Calendar.MONTH) + 1));

        // Setting on click listeners for the add and initialize expenses buttons
        addBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                CalendarFragment calendar = new CalendarFragment();
                calendar.setDatabase(db);
                hideMainUI();
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

    public void initializeDatabase() {
        db = Room.databaseBuilder(getApplicationContext(), ExpensesTrackerDatabase.class, "ExpensesTracker").build();
        Log.i("Database creation", "Success!");
        retrieveMonthlyInfoFromDatabase();
        retrieveDeadlinesFromDatabase();
        retrieveCalendarEventsFromDatabase();

    }
    // Grabs monthly expense and income value from previous state
    public void retrieveMonthlyInfoFromDatabase() {
        MonthlyInfoDAO monthlyInfoDAO = new MonthlyInfoDAO_Impl(db);
        LiveData<List<MonthlyInfoEntity>> monthlyInfo = monthlyInfoDAO.getMonthlyInfo();

        List<MonthlyInfoEntity> monthlyValues = monthlyInfo.getValue();
        monthlyInfo.observe(this, new Observer<List<MonthlyInfoEntity>>() {
            @Override
            public void onChanged(List<MonthlyInfoEntity> monthlyInfoEntities) {
                if (monthlyInfoEntities != null && !monthlyInfoEntities.isEmpty()) {
                    Log.i("Monthly Info", "Fecthing saved data: Expenses = " + monthlyInfoEntities.get(0).expenses + " Income = " + monthlyInfoEntities.get(0).income);
                    income = monthlyInfoEntities.get(0).income;
                    expenses = monthlyInfoEntities.get(0).expenses;
                    double additionalBudgetFromCalendar = Math.round((additionalIncomeFromCalendar - additionalExpensesFromCalendar) * 100.0) / 100.0;
                    budget = (Math.round((income - expenses) * 100.0) / 100.0) + additionalBudgetFromCalendar;
                    overview.setText(String.join("", generateUpdatedText(expenses, income, budget, deadlineCount)));
                } else {
                    Log.i("Monthly Info", "No valid data to fetch");
                }
            }
        });
    }
    // Grabs all deadline events from previous state
    @SuppressLint("NewAPI")
    public void retrieveDeadlinesFromDatabase() {
        DeadlineEventsDAO dao = new DeadlineEventsDAO_Impl(db);
        LiveData<List<DeadlineEventsEntity>> deadlinesInDatabase = dao.getDeadlineEvents();
        deadlinesInDatabase.observe(this, new Observer<List<DeadlineEventsEntity>>() {
            @Override
            public void onChanged(List<DeadlineEventsEntity> deadlineEventsEntities) {
                if (deadlineEventsEntities != null && !deadlineEventsEntities.isEmpty()) {

                    Log.i("Deadlines", "Fetching deadlines...");
                    deadlines = new ArrayList<DeadlineEvent>();
                    for (int i = 0; i < deadlineEventsEntities.size(); i++) {
                        int day = deadlineEventsEntities.get(i).day;
                        int month = deadlineEventsEntities.get(i).month;
                        int year = deadlineEventsEntities.get(i).year;
                        LocalDate date = LocalDate.of(year, month, day);
                        double expense = deadlineEventsEntities.get(i).expense;
                        String information = deadlineEventsEntities.get(i).information;
                        deadlines.add(new DeadlineEvent(expense, 0, date, information));
                    }
                    overview.setText(String.join("", generateUpdatedText(expenses, income, budget, deadlines.size())));
                }
            }
        });
    }
    @SuppressLint("NewAPI")
    public void retrieveCalendarEventsFromDatabase() {
        CalendarEventsDAO dao = new CalendarEventsDAO_Impl(db);
        LiveData<List<CalendarEventsEntity>> calendarEvents = dao.getCalendarEvents();
        calendarEvents.observe(this, new Observer<List<CalendarEventsEntity>>() {
            @Override
            public void onChanged(List<CalendarEventsEntity> calendarEventsEntities) {
                if (calendarEventsEntities != null && !calendarEventsEntities.isEmpty()) {
                    monthlyMapping = new HashMap<Integer, HashMap<LocalDate, ArrayList<CalendarEvent>>>();
                    for (int i = 0; i < calendarEventsEntities.size(); i++) {
                        int year = calendarEventsEntities.get(i).year;
                        int month = calendarEventsEntities.get(i).month;
                        int day = calendarEventsEntities.get(i).day;
                        double expenses = calendarEventsEntities.get(i).expense;
                        double income = calendarEventsEntities.get(i).income;
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
            }
        });
    }

    @SuppressLint("NewAPI")
    public void setAlarmForDeadline(DeadlineEvent deadline) {

        alarmIntent.putExtra("year", deadline.getYear());
        alarmIntent.putExtra("month", deadline.getMonth());
        alarmIntent.putExtra("day", deadline.getDay());
        alarmIntent.putExtra("information", deadline.getInformation());
        alarmIntent.putExtra("amount", deadline.getExpenses());
        pendingIntent = PendingIntent.getBroadcast(this, 0, alarmIntent, PendingIntent.FLAG_UPDATE_CURRENT);

        Calendar endDate = Calendar.getInstance();
        endDate.set(deadline.getYear(), deadline.getMonth(), deadline.getDay());
        long triggerTime = endDate.getTimeInMillis() - System.currentTimeMillis();
        alarmManager.set(AlarmManager.RTC_WAKEUP, 5000, pendingIntent);
    }
    // This is called everytime the user returns to the main page.
    // We want to update the information every time the main page is returned to.
    @Override
    public void onBackStackChanged() {
        overview.setText(String.join("", generateUpdatedText(this.expenses, this.income, this.budget, deadlineCount)));
    }

    // Abstract interface method onDataPassed is called when returning from the MonthlyInfoFragment (popBackStack() is called)
    // MonthlyInfoFragment is where the user initially sets their monthly expenses and income
    @Override
    public void onDataPassed(double expenses, double income) {
        setMonthlyInfo(expenses + additionalExpensesFromCalendar, income + additionalIncomeFromCalendar);
        overview.setText(String.join("", generateUpdatedText(this.expenses, this.income, this.budget, deadlineCount)));

        AsyncTask.execute(new Runnable() {
            @Override
            public void run() {
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

    // Same idea as onDataPassed, but for CalendarFragment instead
    // This passes back the hashmap that keeps track of events in all of the months back to the main page
    // We then iterate through the hashmap entry for the current month, and find the sum of all of the expenses and income that the user added for the current month
    // Update the information displayed on the main page accordingly
    @Override
    public void onCalendarDataPassed(HashMap<Integer, HashMap<LocalDate, ArrayList<CalendarEvent>>> mapping, ArrayList<DeadlineEvent> deadlines) {
        monthlyMapping = mapping;
        this.deadlines = deadlines;
        if (this.deadlines != null && !this.deadlines.isEmpty()) {
            deadlineCount = this.deadlines.size();
        }
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
                            } else {

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
            expenses += additionalExpensesFromCalendar;
            income += additionalIncomeFromCalendar;
            budget += Math.round((additionalIncomeFromCalendar - additionalExpensesFromCalendar) * 100.0) / 100.0;
            Log.i("New Budget", String.valueOf(budget));

            updateTextColorStatus();
        } else {
            Log.i("Failure", "Could not process mapping data");
        }
    }

    @SuppressLint("MissingPermission")
    public void updateTextColorStatus() {
        if (budget <= 150) {
            if (notificationsEnabled) {
                NotificationCompat.Builder notificationBuilder = new NotificationCompat.Builder(this, NotificationHelper.CHANNEL_ID)
                        .setContentTitle("Budget Alert")
                        .setContentText("Warning: Approaching budget limit")
                        .setPriority(NotificationCompat.PRIORITY_HIGH)
                        .setAutoCancel(true)
                        .setSmallIcon(R.drawable.budget_limit_alert)
                        .setTimeoutAfter(5000);
                NotificationManager manager = NotificationHelper.createNotificationChannel(this);
                //NotificationManagerCompat notificationManager = NotificationManagerCompat.from(this);
                if (manager != null) {
                    manager.notify(notificationId, notificationBuilder.build());
                    notificationId++;
                    notificationsEnabled = false;
                }
            }

            String[] fullText = generateUpdatedText(expenses, income, budget, deadlineCount);
            SpannableStringBuilder spannableStringBuilder = new SpannableStringBuilder(fullText[0] + fullText[1]);
            SpannableString budgetText = new SpannableString(fullText[2]);
            int color = ContextCompat.getColor(MainActivity.this, R.color.red);
            BackgroundColorSpan colorSpan = new BackgroundColorSpan(Color.RED);

            budgetText.setSpan(colorSpan,0 , budgetText.length() - 1, Spanned.SPAN_EXCLUSIVE_EXCLUSIVE);
            spannableStringBuilder.append(budgetText);
            overview.setText(spannableStringBuilder);
        } else {
            overview.setText(String.join("", generateUpdatedText(expenses, income, budget, deadlineCount)));
        }
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

    public void setMonthlyInfo(double expenses, double income) {
        this.expenses = expenses;
        this.income = income;
        double additionalBudgetFromCalendar = Math.round((additionalIncomeFromCalendar - additionalExpensesFromCalendar) * 100.0) / 100.0;
        this.budget = (Math.round((income - expenses) * 100.0) / 100.0) + additionalBudgetFromCalendar;
    }

    public HashMap<Integer, HashMap<LocalDate, ArrayList<CalendarEvent>>> getMonthlyMapping() {
        return monthlyMapping;
    }
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
        AsyncTask.execute(new Runnable() {
            @Override
            public void run() {
                MonthlyInfoDAO monthlyInfoDAO = db.monthlyInfoDAO();
                CalendarEventsDAO calendarDAO = db.calendarEventsDAO();
                DeadlineEventsDAO deadlineDAO = db.deadlineEventsDAO();
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