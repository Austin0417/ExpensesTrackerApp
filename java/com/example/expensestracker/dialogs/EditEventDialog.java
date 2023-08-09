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

import androidx.fragment.app.DialogFragment;

import com.example.expensestracker.R;
import com.example.expensestracker.calendar.CalendarEvent;
import com.example.expensestracker.calendar.EditEvent;
import com.example.expensestracker.calendar.ExpenseCategory;
import com.example.expensestracker.calendar.ExpensesEvent;
import com.example.expensestracker.calendar.IncomeEvent;

import java.util.ArrayList;
import java.util.List;

public class EditEventDialog extends DialogFragment implements AdapterView.OnItemSelectedListener {
    private EditText amount;

    // TODO Populate the Spinner dropdown with all of the existing user-created ExpenseCategories
    private Spinner expenseCategories;

    private Button deleteBtn;

    // ArrayList of CalendarEvent objects that is initialized in the constructor of EditEventDialog
    // This ArrayList represents the events associated with a particular day, that the user has selected from the RecyclerView
    private CalendarEvent selectedEvent;
    //private ArrayList<CalendarEvent> events;
    private List<ExpenseCategory> categories;

    // Interface object that will allow us to initiate the callback within MainActivity
    private EditEvent editEvent;

    // Integer variable to keep track of the index of the currently selected option
    // This will be useful for obtaining the correct CalendarEvent object from the events array corresponding to the selected choice
    private int currentSelectedIndex;

    // Integer variable to track current selected index of the categories spinner
    private int categorySelectionIndex = -1;

    @Override
    public void onItemSelected(AdapterView<?> parent, View view, int pos, long id) {
        categorySelectionIndex = pos;
        Log.i("EDIT EVENT", "Category index=" + categorySelectionIndex);
    }

    @Override
    public void onNothingSelected(AdapterView<?> parent) {

    }


    public EditEventDialog(CalendarEvent selectedEvent, List<ExpenseCategory> categories) {
        this.selectedEvent = selectedEvent;
        this.categories = categories;
    }

    public View initialize() {
        // Obtaining the view for the dialog
        View v = LayoutInflater.from(requireContext()).inflate(R.layout.edit_event, null, false);

        // Initializing the EditEvent object by casting the MainActivity as an EditEvent
        // Also initializing UI elements such as the delete button and amount edit field
        editEvent = (EditEvent) getContext();
        expenseCategories = v.findViewById(R.id.selectedExpenseCategory);
        deleteBtn = v.findViewById(R.id.deleteBtn);
        amount = v.findViewById(R.id.amountText);

        String[] spinnerOptions = new String[categories.size() + 1];
        spinnerOptions[spinnerOptions.length - 1] = "Other";
        for (int i = 0; i < categories.size(); i++) {
            spinnerOptions[i] = categories.get(i).toString();
        }

        ArrayAdapter<String> categoriesAdapter = new ArrayAdapter<String>(getContext(), android.R.layout.simple_spinner_item, spinnerOptions);
        categoriesAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        expenseCategories.setAdapter(categoriesAdapter);
        expenseCategories.setOnItemSelectedListener(this);

        // Before the user selects an option (only happens once, when the dialog first appears)
        // We want the amount field and delete button to be invisible
        if (selectedEvent instanceof IncomeEvent) {
            expenseCategories.setVisibility(View.GONE);
        } else {
            expenseCategories.setSelection(categories.indexOf(((ExpensesEvent) selectedEvent).getCategory()));
        }
        amount.setText(Double.toString(selectedEvent.getAmount()));

        return v;
    }

    @Override
    public Dialog onCreateDialog(Bundle savedInstancedState) {
        View v = initialize();
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
                                editEvent.deleteCalendarEvent(selectedEvent);
                            }
                        }).create();
                dialog.show();
            }
        });

        // Creating a String array that will serve as the options for the SingleChoiceItems
        // We want to create as many options as there are events on this particular day, which is why we define the size of the array to equal events.size()
        return new AlertDialog.Builder(requireContext())
                .setTitle("Edit Event")
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
                        double newAmount = Double.parseDouble(amount.getText().toString());
                        selectedEvent.setAmount(newAmount);

                        if (selectedEvent instanceof ExpensesEvent) {
                            ExpenseCategory newCategory = categories.get(categorySelectionIndex);
                            ((ExpensesEvent) selectedEvent).setCategory(newCategory);
                        }

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
