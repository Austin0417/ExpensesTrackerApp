package com.example.expensestracker;

import androidx.appcompat.app.AppCompatActivity;
import androidx.fragment.app.FragmentManager;
import androidx.fragment.app.FragmentTransaction;

import android.app.Activity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.FrameLayout;
import android.widget.TextView;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.HashMap;

public class MainActivity extends AppCompatActivity implements FragmentManager.OnBackStackChangedListener, PassMonthlyData, CalendarDataPass {
    FrameLayout addMonthlyInfoFragment;
    private Button loginBtn;
    private Button addBtn;
    private Button initializeBtn;
    private FragmentManager manager;
    private TextView overview;
    private TextView dashboardLabel;
    private double income = 0;
    private double expenses = 0;
    private double budget = 0;
    private MonthlyInfoFragment monthlyInfo = new MonthlyInfoFragment();
    private HashMap<Integer, ArrayList<CalendarEvent>> monthlyMapping;
    private HashMap<Integer, HashMap<Integer, ArrayList<CalendarEvent>>> yearlyMapping;

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
        initializeBtn.setOnClickListener( new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                hideMainUI();
                FragmentTransaction transaction = manager.beginTransaction();
                transaction.replace(addMonthlyInfoFragment.getId(), monthlyInfo);
                transaction.addToBackStack("Monthly Information");
                transaction.commit();
            }
        });
    }
    @Override
    public void onBackStackChanged() {
        overview.setText("Expenses: " + this.expenses + "\nIncome: " + this.income + "\nTotal available budget: " + budget);
    }
    @Override
    public void onDataPassed(double expenses, double income) {
        this.income = income;
        this.expenses = expenses;
        this.budget = Math.round((income - expenses) * 100.0) / 100.0;
        overview.setText("Expenses: " + this.expenses + "\nIncome: " + this.income + "\nTotal available budget: " + budget);
    }
    @Override
    public void onCalendarDataPassed(HashMap<Integer, ArrayList<CalendarEvent>> mapping) {
        monthlyMapping = mapping;
        if (monthlyMapping != null && !monthlyMapping.isEmpty()) {
            Calendar calendar = Calendar.getInstance();
            int currentMonth = calendar.get(Calendar.MONTH) + 1;
            ArrayList<CalendarEvent> currentMonthEvents = monthlyMapping.get(currentMonth);
            for (CalendarEvent event : currentMonthEvents) {
                if (!event.isMarked()) {
                    expenses += event.getExpenses();
                    income += event.getIncome();
                    event.setMarked(true);
                }
            }
            budget = Math.round((income - expenses) * 100.0) / 100.0;
            overview.setText(generateUpdatedText(expenses, income, budget));

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

    public HashMap<Integer, ArrayList<CalendarEvent>> getMonthlyMapping() {
        return monthlyMapping;
    }
    public String generateUpdatedText(double expenses, double income, double budget) {
        return ("Expenses: " + expenses + "\nIncome: " + income + "\nTotal available budget: " + budget);
    }
    public void resetInfo() {
        if (monthlyMapping != null && !monthlyMapping.isEmpty()) {
            monthlyMapping.clear();
        }
        income = 0;
        expenses = 0;
        budget = 0;
        Log.i("Main Info", "Expenses: " + expenses + "\nIncome: " + income + "\nBudget: " + budget);

        overview.setText(generateUpdatedText(expenses, income, budget));
    }
}