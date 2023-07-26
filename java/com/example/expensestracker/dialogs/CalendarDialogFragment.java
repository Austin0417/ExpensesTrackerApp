package com.example.expensestracker.dialogs;

import android.app.AlertDialog;
import android.app.Dialog;
import android.content.Context;
import android.content.DialogInterface;
import android.os.Bundle;

import androidx.constraintlayout.widget.ConstraintSet;
import androidx.fragment.app.DialogFragment;
import androidx.fragment.app.Fragment;

import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.EditText;
import android.widget.Spinner;
import android.widget.TextView;

import com.example.expensestracker.R;
import com.example.expensestracker.calendar.CalendarEvent;
import com.example.expensestracker.calendar.ExpenseCategory;

import java.util.List;


public class CalendarDialogFragment extends DialogFragment implements AdapterView.OnItemSelectedListener {

    // TODO: Rename parameter arguments, choose names that match
    // the fragment initialization parameters, e.g. ARG_ITEM_NUMBER
    private static final String ARG_PARAM1 = "param1";
    private static final String ARG_PARAM2 = "param2";

    // TODO: Rename and change types of parameters
    private String mParam1;
    private String mParam2;
    // Input field for the user to enter in the amount if Expenses option was checked
    private EditText additionalExpenses;

    // Input field for  the user to enter in the amount if the Income option was checked
    private EditText additionalIncome;

    // Input fields for the user to enter in the amount and description if the Deadline option was checked
    private EditText deadlineText;
    private EditText deadlineDescription;

    private Spinner categorySelection;
    private Spinner hourSelection;
    private Spinner am_pm_selection;
    private Spinner minuteSelection;

    private View alertView;

    private String type;

    private List<ExpenseCategory> categories;

    // Current index of the selection within the spinner for setting an ExpenseEvent's category
    private int categorySelectionIndex;

    private int hourForDeadline;
    private int minuteForDeadline;
    private int am_pm_selection_for_deadline;

    public CalendarDialogFragment(List<ExpenseCategory> categories) {
        this.categories = categories;
    }

    @Override
    public void onItemSelected(AdapterView<?> parent, View view, int pos, long id) {
        switch (parent.getId()) {
            case R.id.hourSelection:
                String hour = (String) parent.getItemAtPosition(pos);
                hourForDeadline = Integer.parseInt(hour);
                Log.i("Deadline Hour", "Selected hour=" + hourForDeadline);
                break;
            case R.id.am_pm_selection:
                String selection = (String) parent.getItemAtPosition(pos);
                Log.i("Deadline Selection", "Selection=" + selection);
                if (selection.equals("AM")) {
                    am_pm_selection_for_deadline = CalendarEvent.AM;
                } else {
                    am_pm_selection_for_deadline = CalendarEvent.PM;
                }
                break;
            case R.id.minuteSelection:
                String minute = ((String) parent.getItemAtPosition(pos)).substring(1);
                minuteForDeadline = Integer.parseInt(minute);
                Log.i("Deadline Selection", "Minute=" + minuteForDeadline);
                break;
            case R.id.categorySelection:
                categorySelectionIndex = pos;
                Log.i("CATEGORY SELECTION", "Pos=" + categorySelectionIndex);
                break;
        }
    }

    @Override
    public void onNothingSelected(AdapterView<?> parent) {
        hourForDeadline = 10;
        am_pm_selection_for_deadline = CalendarEvent.AM;
        minuteForDeadline = 0;
    }

    @Override
    public Dialog onCreateDialog(Bundle savedInstanceState) {
        super.onCreateDialog(savedInstanceState);
        AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
        LayoutInflater inflater = LayoutInflater.from(getContext());
        View dialogView = inflater.inflate(R.layout.fragment_calendar_dialog, null, false);
        additionalIncome = dialogView.findViewById(R.id.incomeInput);
        additionalExpenses = dialogView.findViewById(R.id.expensesInput);
        deadlineText = dialogView.findViewById(R.id.deadlineInput);
        deadlineDescription = dialogView.findViewById(R.id.deadlineInfo);
        categorySelection = dialogView.findViewById(R.id.categorySelection);
        hourSelection = dialogView.findViewById(R.id.hourSelection);
        minuteSelection = dialogView.findViewById(R.id.minuteSelection);
        am_pm_selection = dialogView.findViewById(R.id.am_pm_selection);

        // Initializing selection options for the hour spinner
        ArrayAdapter<String> hourSelectionAdapter = new ArrayAdapter<String>(getContext(),
                android.R.layout.simple_spinner_item,
                new String[]{"1", "2", "3", "4", "5", "6", "7", "8", "9", "10", "11", "12"});
        hourSelectionAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        hourSelection.setAdapter(hourSelectionAdapter);

        // Initializing selection options for the minute spinner
        ArrayAdapter<String> minuteSelectionAdapter = new ArrayAdapter<String>(getContext(), android.R.layout.simple_spinner_item, new String[]{":00", ":15", ":30", ":45"});
        minuteSelection.setAdapter(minuteSelectionAdapter);

        // Initializing selection options for the AM/PM spinner
        ArrayAdapter<String> am_pm_adapter = new ArrayAdapter<String>(getContext(), android.R.layout.simple_spinner_item, new String[]{"AM", "PM"});
        am_pm_adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        am_pm_selection.setAdapter(am_pm_adapter);

        // Initializing selection options for category selection spinner
        String category_options[] = new String[categories.size() + 1];
        category_options[category_options.length - 1] = "Other";
        for (int i = 0; i < categories.size(); i++) {
            category_options[i] = categories.get(i).getName();
        }
        ArrayAdapter<String> categoryAdapter = new ArrayAdapter<String>(getContext(), android.R.layout.simple_spinner_item, category_options);
        categoryAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        categorySelection.setAdapter(categoryAdapter);

        hourSelection.setOnItemSelectedListener(this);
        am_pm_selection.setOnItemSelectedListener(this);
        minuteSelection.setOnItemSelectedListener(this);
        categorySelection.setOnItemSelectedListener(this);

        // Initial state when the CalendarDialog is created, we hide everything except the list of choices
        deadlineDescription.setVisibility(View.GONE);
        additionalIncome.setVisibility(View.GONE);
        additionalExpenses.setVisibility(View.GONE);
        deadlineText.setVisibility(View.GONE);
        categorySelection.setVisibility(View.GONE);
        hourSelection.setVisibility(View.GONE);
        minuteSelection.setVisibility(View.GONE);
        am_pm_selection.setVisibility(View.GONE);

        String[] choices = {"Additional Expenses", "Additional Income", "Deadline"};
        ArrayAdapter<String> adapter = new ArrayAdapter<>(requireContext(), android.R.layout.simple_spinner_dropdown_item);
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        int checkedItem = -1;

        builder.setView(dialogView)
                .setPositiveButton("Save", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialogInterface, int i) {
                Log.i("Additional info", additionalExpenses.getText().toString() + " " + additionalIncome.getText().toString());
                // Expenses option was chosen
                if (type == "expenses") {
                    if (!additionalExpenses.getText().toString().isEmpty()) {
                        double arr[] = {Double.parseDouble(additionalExpenses.getText().toString()), 0};
                        Bundle result = new Bundle();
                        result.putDoubleArray("calendarevent", arr);
                        result.putInt("category_index", categorySelectionIndex);
                        getParentFragmentManager().setFragmentResult("fragment_data", result);
                    }
                // Income option was chosen
                } else if (type == "income") {
                    if (!additionalIncome.getText().toString().isEmpty()) {
                        double arr[] = {0, Double.parseDouble(additionalIncome.getText().toString())};
                        Bundle result = new Bundle();
                        result.putDoubleArray("calendarevent", arr);
                        getParentFragmentManager().setFragmentResult("fragment_data", result);
                    }
                // Deadline option was chosen
                } else {
                    if (!deadlineText.getText().toString().isEmpty() && !deadlineDescription.getText().toString().isEmpty()) {
                        double arr[] = {Double.parseDouble(deadlineText.getText().toString()), 0};
                        Bundle result = new Bundle();
                        result.putDoubleArray("calendarevent", arr);
                        result.putIntArray("time_selection", new int[]{hourForDeadline, am_pm_selection_for_deadline, minuteForDeadline});
                        result.putString("deadline_description", deadlineDescription.getText().toString());
                        getParentFragmentManager().setFragmentResult("fragment_data", result);
                    }
                }
            }
        }).setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialogInterface, int i) {
                dismiss();
            }
        }).setSingleChoiceItems(choices, checkedItem, new DialogInterface.OnClickListener() {
            @Override
            // Choices for creating Expense, Income, or Deadline event
            public void onClick(DialogInterface dialog, int which) {
                if (which == 0) {
                    // If the user has selected ExpenseEvent
                    additionalExpenses.setVisibility(View.VISIBLE);
                    categorySelection.setVisibility(View.VISIBLE);
                    additionalIncome.setVisibility(View.GONE);
                    deadlineText.setVisibility(View.GONE);
                    deadlineDescription.setVisibility(View.GONE);
                    hourSelection.setVisibility(View.GONE);
                    am_pm_selection.setVisibility(View.GONE);
                    minuteSelection.setVisibility(View.GONE);
                    type = "expenses";
                } else if (which == 1) {
                    // If the user has selected IncomeEvent
                    additionalIncome.setVisibility(View.VISIBLE);
                    additionalExpenses.setVisibility(View.GONE);
                    deadlineText.setVisibility(View.GONE);
                    deadlineDescription.setVisibility(View.GONE);
                    hourSelection.setVisibility(View.GONE);
                    am_pm_selection.setVisibility(View.GONE);
                    minuteSelection.setVisibility(View.GONE);
                    categorySelection.setVisibility(View.GONE);
                    type = "income";
                } else {
                    // The user has selected DeadlineEvent
                    deadlineText.setVisibility(View.VISIBLE);
                    deadlineDescription.setVisibility(View.VISIBLE);
                    hourSelection.setVisibility(View.VISIBLE);
                    am_pm_selection.setVisibility(View.VISIBLE);
                    minuteSelection.setVisibility(View.VISIBLE);
                    additionalIncome.setVisibility(View.GONE);
                    additionalExpenses.setVisibility(View.GONE);
                    categorySelection.setVisibility(View.GONE);
                    type = "deadline";
                }
            }
        });
                return builder.create();
    }


    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (getArguments() != null) {
            mParam1 = getArguments().getString(ARG_PARAM1);
            mParam2 = getArguments().getString(ARG_PARAM2);
        }
        LayoutInflater inflater = requireActivity().getLayoutInflater();
        additionalExpenses = inflater.inflate(R.layout.fragment_calendar_dialog, null).findViewById(R.id.expensesInput);
        additionalIncome = inflater.inflate(R.layout.fragment_calendar_dialog, null).findViewById(R.id.incomeInput);
    }

    @Override
    public void onAttach(Context context) {
        super.onAttach(context);
    }


}