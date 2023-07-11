package com.example.expensestracker.dialogs;

import android.app.AlertDialog;
import android.app.Dialog;
import android.content.DialogInterface;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;

import androidx.fragment.app.DialogFragment;

import com.example.expensestracker.R;
import com.example.expensestracker.calendar.CalendarEvent;
import com.example.expensestracker.calendar.EditEvent;
import com.example.expensestracker.calendar.ExpensesEvent;

import java.util.ArrayList;

public class EditEventDialog extends DialogFragment {
    private EditText amount;
    private Button deleteBtn;

    // ArrayList of CalendarEvent objects that is initialized in the constructor of EditEventDialog
    // This ArrayList represents the events associated with a particular day, that the user has selected from the RecyclerView
    private ArrayList<CalendarEvent> events;

    // Interface object that will allow us to initiate the callback within MainActivity
    private EditEvent editEvent;

    // Integer variable to keep track of the index of the currently selected option
    // This will be useful for obtaining the correct CalendarEvent object from the events array corresponding to the selected choice
    private int currentSelectedIndex;

    public EditEventDialog(ArrayList<CalendarEvent> events) {
        this.events = events;
    }

    @Override
    public Dialog onCreateDialog(Bundle savedInstancedState) {
        // Obtaining the view for the dialog
        View v = LayoutInflater.from(requireContext()).inflate(R.layout.edit_event, null, false);

        // Initializing the EditEvent object by casting the MainActivity as an EditEvent
        // Also initializing UI elements such as the delete button and amount edit field
        editEvent = (EditEvent) getContext();
        deleteBtn = v.findViewById(R.id.deleteBtn);
        amount = v.findViewById(R.id.amountText);

        // Before the user selects an option (only happens once, when the dialog first appears)
        // We want the amount field and delete button to be invisible
        amount.setVisibility(View.GONE);
        deleteBtn.setVisibility(View.GONE);

        deleteBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                AlertDialog dialog = new AlertDialog.Builder(getContext())
                        .setTitle("Delete Event Confirmation")
                        .setMessage("Are you sure you want to delete the selected event?")
                        .setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog, int which) {
                                dismiss();
                            }
                        })
                        .setPositiveButton("Confirm", new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog, int which) {
                                getDialog().dismiss();
                                CalendarEvent eventToDelete = events.get(currentSelectedIndex);
                                events.remove(currentSelectedIndex);
                                editEvent.deleteCalendarEvent(eventToDelete);
                            }
                        }).create();
                dialog.show();
            }
        });

        // Creating a String array that will serve as the options for the SingleChoiceItems
        // We want to create as many options as there are events on this particular day, which is why we define the size of the array to equal events.size()
        String[] selection = new String[events.size()];
        for (int i = 0; i < events.size(); i++) {
            if (events.get(i) instanceof ExpensesEvent) {
                selection[i] = "Expense #" + (i + 1) + ": $" + events.get(i).getExpenses();
            } else {
                selection[i] = "Income #" + (i + 1) + ": $" + events.get(i).getIncome();
            }
        }
        return new AlertDialog.Builder(requireContext())
                .setTitle("Edit Event")
                .setSingleChoiceItems(selection, -1, new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        // Whenever the user clicks a different choice, we need to update the index tracker variable
                        currentSelectedIndex = which;

                        // Display the amount associated with the selected event in the EditText view
                        amount.setText(Double.toString(Math.abs((events.get(currentSelectedIndex).getIncome() - events.get(currentSelectedIndex).getExpenses()))));
                        amount.setVisibility(View.VISIBLE);
                        deleteBtn.setVisibility(View.VISIBLE);
                    }
                })
                .setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        dismiss();
                    }
                })
                .setPositiveButton("Save", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        // When the user clicks save, we update any changes to the amount that the user might've made
                        // We use the currentSelectedIndex variable to obtain a reference to the CalendarEvent that has been modified by the user
                        // Call the updateAmount method on the object to update the object in memory, however we still need to translate this change to the local database as well
                        CalendarEvent selectedEvent = events.get(currentSelectedIndex);
                        double newAmount = Double.parseDouble(amount.getText().toString());
                        selectedEvent.setAmount(newAmount);

                        // Interface method is called, which will be received as a callback in MainActivity's newAmount override method
                        // As arguments, we pass the event object that was modified, and the new amount
                        // In MainActivity's callback, we will use the database handle in MainActivity to make the corresponding query to update the target event
                        editEvent.modifyCalendarEvent(selectedEvent, Double.parseDouble(amount.getText().toString()));
                    }
                })
                .setView(v)
                .create();
    }
}
