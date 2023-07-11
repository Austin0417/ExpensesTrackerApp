package com.example.expensestracker.dialogs;

import android.app.AlertDialog;
import android.app.Dialog;
import android.content.DialogInterface;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Spinner;
import android.widget.Toast;

import androidx.fragment.app.DialogFragment;

import com.example.expensestracker.R;
import com.example.expensestracker.calendar.CalendarEvent;
import com.example.expensestracker.calendar.DeadlineEvent;
import com.example.expensestracker.calendar.EditEvent;

import java.util.ArrayList;

public class EditDeadlineDialog extends DialogFragment implements AdapterView.OnItemSelectedListener {
    // Reference to the user-selected deadline
    private final ArrayList<DeadlineEvent> deadlines;
    private final DeadlineEvent targetDeadline;

    // Index of the selected deadline back in MainActivity's DeadlineEvent ArrayList
    private final int deadlineIndex;

    // Previous information of the selected deadline (before the user has made any changes to the information)
    // We store this for usage in a potential query later on

    private EditText deadlineInfoText;
    private EditText deadlineAmountText;
    private Button deleteBtn;
    private EditEvent editEvent;
    private Spinner deadlineHour;
    private Spinner deadlineHourType;
    private Spinner deadlineMinute;

    private int newHour;
    private int newHourType;
    private int newMinute;


    public EditDeadlineDialog(int index, ArrayList<DeadlineEvent> deadlines) {
        this.deadlines = deadlines;
        deadlineIndex = index;
        targetDeadline = deadlines.get(index);
    }

    @Override
    public void onItemSelected(AdapterView<?> parent, View view, int pos, long id) {
        switch(parent.getId()) {
            case R.id.editDeadlineHour:
                newHour = Integer.parseInt((String) parent.getItemAtPosition(pos));
                break;

            case R.id.editDeadlineMinute:
                newMinute = Integer.parseInt(((String) parent.getItemAtPosition(pos)).substring(1));
                break;

            case R.id.editDeadlineAMPM:
                String selection = (String) parent.getItemAtPosition(pos);
                if (selection.equals("AM")) {
                    newHourType = CalendarEvent.AM;
                } else {
                    newHourType = CalendarEvent.PM;
                }
                break;
        }
    }

    @Override
    public void onNothingSelected(AdapterView<?> parent) {
        newHour = targetDeadline.getHour();
        newMinute = targetDeadline.getMinute();
        newHourType = targetDeadline.getAmOrPm();
    }

    public void initializeViews(View v) {
        deadlineInfoText = v.findViewById(R.id.deadlineInfoText);
        deadlineAmountText = v.findViewById(R.id.deadlineAmountText);
        deleteBtn = v.findViewById(R.id.deleteDeadlineBtn);

        // Spinner views for hour, minute, and AM/PM
        deadlineHour = v.findViewById(R.id.editDeadlineHour);
        deadlineHourType = v.findViewById(R.id.editDeadlineAMPM);
        deadlineMinute = v.findViewById(R.id.editDeadlineMinute);
    }

    public void initializeSpinners() {
        ArrayAdapter<String> hourAdapter = new ArrayAdapter<String>(getContext(),
                android.R.layout.simple_spinner_item,
                new String[]{"1", "2", "3", "4", "5", "6", "7", "8", "9", "10", "11", "12"});
        ArrayAdapter<String> minuteAdapter = new ArrayAdapter<String>(getContext(),
                android.R.layout.simple_spinner_item,
                new String[]{":00", ":15", ":30", ":45"});
        ArrayAdapter<String> hourTypeAdapter = new ArrayAdapter<String>(getContext(),
                android.R.layout.simple_spinner_item,
                new String[]{"AM", "PM"});
        deadlineHour.setAdapter(hourAdapter);
        deadlineMinute.setAdapter(minuteAdapter);
        deadlineHourType.setAdapter(hourTypeAdapter);
        deadlineHour.setOnItemSelectedListener(this);
        deadlineMinute.setOnItemSelectedListener(this);
        deadlineHourType.setOnItemSelectedListener(this);

        // Setting initial values for spinners using selected deadline attributes
        deadlineHour.setSelection(targetDeadline.getHour() - 1);
        deadlineMinute.setSelection(targetDeadline.getMinute() / 15);
        if (targetDeadline.getAmOrPm() == CalendarEvent.AM) {
            deadlineHourType.setSelection(0);
        } else {
            deadlineHourType.setSelection(1);
        }
    }

    @Override
    public Dialog onCreateDialog(Bundle savedInstanceState) {
        View v = LayoutInflater.from(getContext()).inflate(R.layout.edit_deadline, null);
        initializeViews(v);
        initializeSpinners();

        editEvent = (EditEvent) getContext();

        // Set the info and amount field to their initial values (e.g. the deadline amount and information before the user has changed anything)
        deadlineInfoText.setText(targetDeadline.getInformation());
        deadlineAmountText.setText(String.valueOf(targetDeadline.getAmount()));

        deleteBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                AlertDialog dialog = new AlertDialog.Builder(getContext())
                        .setTitle("Delete deadline")
                        .setMessage("Are you sure you want to delete the selected deadline?")
                        .setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog, int which) {
                                dismiss();
                            }
                        })
                        .setPositiveButton("Confirm", new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog, int which) {
                                // Callback to deleteDeadlineEvent in MainActivity
                                editEvent.deleteDeadlineEvent(deadlineIndex, targetDeadline);

                                getDialog().dismiss();
                                dismiss();
                            }
                        }).create();
                dialog.show();
            }
        });

        return new AlertDialog.Builder(getContext())
                .setTitle("Edit Deadline")
                .setView(v)
                .setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        dismiss();
                    }
                })
                .setPositiveButton("Confirm", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        double previousAmount = targetDeadline.getAmount();
                        String previousInformation = targetDeadline.getInformation();
                        if (deadlineAmountText.getText().toString().isEmpty() || deadlineInfoText.getText().toString().isEmpty()) {
                            Toast.makeText(getContext(), "One or more fields empty, deadline update aborted", Toast.LENGTH_LONG).show();
                            return;
                        }

                        // Updating selected deadline with the new user-inputted data
                        targetDeadline.setAmount(Double.parseDouble(deadlineAmountText.getText().toString()));
                        targetDeadline.setInformation(deadlineInfoText.getText().toString());
                        targetDeadline.setHour(newHour);
                        targetDeadline.setMinute(newMinute);
                        targetDeadline.setAmOrPm(newHourType);
                        Log.i("Update", "New amount: " + targetDeadline.getAmount() + " New info: " + targetDeadline.getInformation());

                        // Callback to MainActivity's modifyDeadlineEvent implementation
                        editEvent.modifyDeadlineEvent(targetDeadline, previousInformation, previousAmount, deadlineIndex);
                    }
                }).create();
    }
}
