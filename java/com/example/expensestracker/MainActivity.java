package com.example.expensestracker;

import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.app.NotificationCompat;
import androidx.core.app.NotificationManagerCompat;
import androidx.core.content.ContextCompat;
import androidx.fragment.app.FragmentManager;
import androidx.fragment.app.FragmentTransaction;

import android.Manifest;
import android.annotation.SuppressLint;
import android.app.Activity;
import android.app.NotificationManager;
import android.content.Context;
import android.content.pm.PackageManager;
import android.graphics.Color;
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

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;

public class MainActivity extends AppCompatActivity implements FragmentManager.OnBackStackChangedListener, PassMonthlyData, CalendarDataPass {
    private static final int NOTIFICATION_STATUS_CODE = 1;
    FrameLayout addMonthlyInfoFragment;
    private Button loginBtn;
    private Button addBtn;
    private Button initializeBtn;
    private FragmentManager manager;
    private TextView overview;
    private TextView dashboardLabel;
    private int notificationId = 1;
    private int deadlineCount = 0;
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
    private HashMap<Integer, HashMap<LocalDate, ArrayList<CalendarEvent>>> monthlyMapping;
    private ArrayList<DeadlineEvent> deadlines;
    boolean upToDate = true;
    private boolean notificationsEnabled = true;


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

        loginBtn = findViewById(R.id.loginBtn);
        addBtn = findViewById(R.id.addBtn);
        initializeBtn = findViewById(R.id.initializeBtn);
        overview = findViewById(R.id.overviewText);
        dashboardLabel = findViewById(R.id.dashboardLabel);
        addMonthlyInfoFragment = findViewById(R.id.monthlyInfoFragment);
        manager = getSupportFragmentManager();
        manager.addOnBackStackChangedListener(this);


        Log.i("Current Month", String.valueOf(Calendar.getInstance().get(Calendar.MONTH) + 1));

        addBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                hideMainUI();
                FragmentTransaction transaction = manager.beginTransaction();
                transaction.replace(R.id.calendarFragment, new CalendarFragment());
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
        if (ActivityCompat.checkSelfPermission(this, android.Manifest.permission.POST_NOTIFICATIONS) != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.POST_NOTIFICATIONS}, NOTIFICATION_STATUS_CODE);
            // TODO: Consider calling
            //    ActivityCompat#requestPermissions
            // here to request the missing permissions, and then overriding
            //   public void onRequestPermissionsResult(int requestCode, String[] permissions,
            //                                          int[] grantResults)
            // to handle the case where the user grants the permission. See the documentation
            // for ActivityCompat#requestPermissions for more details.
            return;
        }
    }

    @Override
    public void onBackStackChanged() {
        overview.setText(String.join("", generateUpdatedText(this.expenses, this.income, this.budget, deadlineCount)));
    }

    @Override
    public void onDataPassed(double expenses, double income) {
        this.income = income + additionalIncomeFromCalendar;
        this.expenses = expenses + additionalExpensesFromCalendar;
        double additionalBudgetFromCalendar = Math.round((additionalIncomeFromCalendar - additionalExpensesFromCalendar) * 100.0) / 100.0;
        this.budget = (Math.round((income - expenses) * 100.0) / 100.0) + additionalBudgetFromCalendar;
        overview.setText(String.join("", generateUpdatedText(this.expenses, this.income, this.budget, deadlineCount)));
    }

    @Override
    public void onCalendarDataPassed(HashMap<Integer, HashMap<LocalDate, ArrayList<CalendarEvent>>> mapping, ArrayList<DeadlineEvent> deadlines) {
        monthlyMapping = mapping;
        this.deadlines = deadlines;
        if (this.deadlines != null && !this.deadlines.isEmpty()) {
            deadlineCount = this.deadlines.size();
        }
        if (monthlyMapping != null && !monthlyMapping.isEmpty()) {
            double totalAdditionalMonthlyExpenses = additionalExpensesFromCalendar;
            double totalAdditionalMonthlyIncome = additionalIncomeFromCalendar;
            Calendar calendar = Calendar.getInstance();
            int currentMonth = calendar.get(Calendar.MONTH) + 1;
            HashMap<LocalDate, ArrayList<CalendarEvent>> currentMonthEvents = monthlyMapping.get(currentMonth);
            if (currentMonthEvents != null && !currentMonthEvents.isEmpty()) {
                for (Map.Entry<LocalDate, ArrayList<CalendarEvent>> entry : currentMonthEvents.entrySet()) {
                    ArrayList<CalendarEvent> events = entry.getValue();
                    ArrayList<Double> dayExpensesAndIncome = CalendarFragment.calculateTotalBudget(events);
                    for (int i = 0; i < events.size(); i++) {
                        if (!events.get(i).isMarked()) {
                            notificationsEnabled = true;
                            if (events.get(i) instanceof ExpensesEvent) {
                                totalAdditionalMonthlyExpenses += events.get(i).getExpenses();
                            } else if (events.get(i) instanceof IncomeEvent) {
                                totalAdditionalMonthlyIncome += events.get(i).getIncome();
                            } else {

                            }
                            events.get(i).setMarked(true);
                            upToDate = false;
                        }
                    }
                }
            }
            netExpenseFromCalendar = totalAdditionalMonthlyExpenses;
            netIncomeFromCalendar = totalAdditionalMonthlyIncome;
            additionalExpensesFromCalendar = totalAdditionalMonthlyExpenses - additionalExpensesFromCalendar;
            additionalIncomeFromCalendar = totalAdditionalMonthlyIncome - additionalIncomeFromCalendar;
            expenses += additionalExpensesFromCalendar;
            income += additionalIncomeFromCalendar;
            budget += Math.round((additionalIncomeFromCalendar - additionalExpensesFromCalendar) * 100.0) / 100.0;

            updateTextColorStatus();
        } else {
            Log.i("Failure", "Could not process mapping data");
        }
    }

    public void hideMainUI() {
        overview.setVisibility(View.INVISIBLE);
        loginBtn.setVisibility(View.INVISIBLE);
        addBtn.setVisibility(View.INVISIBLE);
        initializeBtn.setVisibility(View.INVISIBLE);
        dashboardLabel.setVisibility(View.INVISIBLE);
    }

    public void unhideMainUI() {
        overview.setVisibility(View.VISIBLE);
        loginBtn.setVisibility(View.VISIBLE);
        addBtn.setVisibility(View.VISIBLE);
        initializeBtn.setVisibility(View.VISIBLE);
        dashboardLabel.setVisibility(View.VISIBLE);
    }

    public HashMap<Integer, HashMap<LocalDate, ArrayList<CalendarEvent>>> getMonthlyMapping() {
        return monthlyMapping;
    }
    public ArrayList<DeadlineEvent> getDeadlines() {
        return deadlines;
    }

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

    public void resetInfo() {
        if (monthlyMapping != null && !monthlyMapping.isEmpty()) {
            monthlyMapping.clear();
        }
        if (deadlines != null && !deadlines.isEmpty()) {
            deadlines.clear();
        }
        income = 0;
        expenses = 0;
        budget = 0;
        deadlineCount = 0;
        additionalExpensesFromCalendar = 0;
        additionalIncomeFromCalendar = 0;
        Log.i("Main Info", "Expenses: " + expenses + "\nIncome: " + income + "\nBudget: " + budget);

        overview.setText(String.join("", generateUpdatedText(expenses, income, budget, 0)));
        unhideMainUI();
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
            //overview.setText(generateUpdatedText(expenses, income, budget));
        } else {
            overview.setText(String.join("", generateUpdatedText(expenses, income, budget, deadlineCount)));
        }
    }
}