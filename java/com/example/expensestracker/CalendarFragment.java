package com.example.expensestracker;

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

import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.HashMap;

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
    private HashMap<Integer, ArrayList<CalendarEvent>> monthlyExpensesMapping = new HashMap<Integer, ArrayList<CalendarEvent>>();


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
            public void onFragmentResult(@NonNull String requestKey, @NonNull Bundle result) {
                double data[] = result.getDoubleArray("calendarevent");
                if (data != null) {
                    Log.i("Dialog data", "Expenses: " + data[0] + " Income: " + data[1]);
                    Calendar calendar = Calendar.getInstance();
                    calendar.set(currentYear, currentMonth, currentDay);
                    Date date = calendar.getTime();
                    if (monthlyExpensesMapping.containsKey(currentMonth)) {
                        monthlyExpensesMapping.get(currentMonth).add(new CalendarEvent(data[0], data[1], date));
                    } else {
                        ArrayList<CalendarEvent> events = new ArrayList<CalendarEvent>();
                        monthlyExpensesMapping.put(currentMonth, events);
                        monthlyExpensesMapping.get(currentMonth).add(new CalendarEvent(data[0], data[1], date));
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
//        mLayoutManager = new LinearLayoutManager(getActivity());
//        mAdapter = new CustomAdapter(mDataset);
//        list.setLayoutManager(mLayoutManager);
//        list.setAdapter(mAdapter);

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
    public void initializeData() {
        if (monthlyExpensesMapping != null && !monthlyExpensesMapping.isEmpty() && monthlyExpensesMapping.containsKey(currentMonth)) {
            ArrayList<CalendarEvent> eventsInMonth = monthlyExpensesMapping.get(currentMonth);
            mDataset = new String[eventsInMonth.size()];
            for (int i = 0; i < eventsInMonth.size(); i++) {
                mDataset[i] = eventsInMonth.get(i).getMonth() + "/" + eventsInMonth.get(i).getDay() + "/" + eventsInMonth.get(i).getYear() + "  - Net additional budget: " + Math.round((eventsInMonth.get(i).getIncome() - eventsInMonth.get(i).getExpenses()) * 100.0) / 100.0;
            }
            mLayoutManager = new LinearLayoutManager(getActivity());
            mAdapter = new CustomAdapter(mDataset);
            list.setLayoutManager(mLayoutManager);
            list.setAdapter(mAdapter);
        }
    }
//    public void updateDataset(@NonNull ArrayList<CalendarEvent> events) {
//        mDataset = new String[events.size()];
//        for (int i = 0; i < events.size(); i++) {
//            mDataset[i] = events.get(i).getMonth() + "/" + events.get(i).getDay() + "/" + events.get(i).getYear() + "  - Net additional budget: " + Math.round((events.get(i).getIncome() - events.get(i).getExpenses()) * 100.0) / 100.0;
//        }
//
//    }
}