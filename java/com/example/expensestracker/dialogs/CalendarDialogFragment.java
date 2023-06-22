package com.example.expensestracker;

import android.app.AlertDialog;
import android.app.Dialog;
import android.content.Context;
import android.content.DialogInterface;
import android.os.Bundle;

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

/**
 * A simple {@link Fragment} subclass.
 * Use the {@link CalendarDialogFragment#newInstance} factory method to
 * create an instance of this fragment.
 */
public class CalendarDialogFragment extends DialogFragment {

    // TODO: Rename parameter arguments, choose names that match
    // the fragment initialization parameters, e.g. ARG_ITEM_NUMBER
    private static final String ARG_PARAM1 = "param1";
    private static final String ARG_PARAM2 = "param2";

    // TODO: Rename and change types of parameters
    private String mParam1;
    private String mParam2;
    private EditText additionalExpenses;
    private EditText additionalIncome;
    private EditText deadlineText;
    private EditText deadlineDescription;
    private View alertView;

    private String type;

    public CalendarDialogFragment() {
        // Required empty public constructor
    }

    @Override
    public Dialog onCreateDialog(Bundle savedInstanceState) {
        super.onCreateDialog(savedInstanceState);
        AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
        LayoutInflater inflater = requireActivity().getLayoutInflater();
        View dialogView = inflater.inflate(R.layout.fragment_calendar_dialog, null, false);
        additionalIncome = dialogView.findViewById(R.id.incomeInput);
        additionalExpenses = dialogView.findViewById(R.id.expensesInput);
        deadlineText = dialogView.findViewById(R.id.deadlineInput);
        deadlineDescription = dialogView.findViewById(R.id.deadlineInfo);

        deadlineDescription.setVisibility(View.GONE);
        additionalIncome.setVisibility(View.GONE);
        additionalExpenses.setVisibility(View.GONE);
        deadlineText.setVisibility(View.GONE);

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
            public void onClick(DialogInterface dialog, int which) {
                if (which == 0) {
                    additionalExpenses.setVisibility(View.VISIBLE);
                    additionalIncome.setVisibility(View.GONE);
                    deadlineText.setVisibility(View.GONE);
                    deadlineDescription.setVisibility(View.GONE);
                    type = "expenses";
                } else if (which == 1) {
                    additionalIncome.setVisibility(View.VISIBLE);
                    additionalExpenses.setVisibility(View.GONE);
                    deadlineText.setVisibility(View.GONE);
                    deadlineDescription.setVisibility(View.GONE);
                    type = "income";
                } else {
                    deadlineText.setVisibility(View.VISIBLE);
                    deadlineDescription.setVisibility(View.VISIBLE);
                    additionalIncome.setVisibility(View.GONE);
                    additionalExpenses.setVisibility(View.GONE);
                    type = "deadline";
                }
            }
        });
                return builder.create();
    }
    public static CalendarDialogFragment newInstance(String param1, String param2) {
        CalendarDialogFragment fragment = new CalendarDialogFragment();
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
        LayoutInflater inflater = requireActivity().getLayoutInflater();
        additionalExpenses = inflater.inflate(R.layout.fragment_calendar_dialog, null).findViewById(R.id.expensesInput);
        additionalIncome = inflater.inflate(R.layout.fragment_calendar_dialog, null).findViewById(R.id.incomeInput);
    }

    @Override
    public void onAttach(Context context) {
        super.onAttach(context);
    }


}