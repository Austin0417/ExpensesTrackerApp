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
import com.example.expensestracker.calendar.ExpenseCategory;
import com.example.expensestracker.calendar.ExpensesEvent;
import com.example.expensestracker.helpers.ExpenseCategoryCallback;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class ExpenseCategoryDialog extends DialogFragment implements AdapterView.OnItemSelectedListener {
    private EditText categoryInput;
    private Spinner categorySelections;
    private Button clearCategoriesBtn;

    private HashMap<Integer, HashMap<LocalDate, ArrayList<CalendarEvent>>> totalEvents;

    // String names of the existing categories created by the user
    private String[] existingCategories;

    // Int variable to keep track of the index of the current selected choice in the single item choices
    private int currentSelectedChoice = -1;

    // Int variable that keeps track of the index of the current selection in the Spinner dropdown
    private int indexOfCategoryToDelete = 0;


    public ExpenseCategoryDialog(List<ExpenseCategory> categories, HashMap<Integer, HashMap<LocalDate, ArrayList<CalendarEvent>>> totalEvents) {
        existingCategories = new String[categories.size()];
        for (int i = 0; i < categories.size(); i++) {
            existingCategories[i] = categories.get(i).toString();
        }
        this.totalEvents = totalEvents;
    }

    @Override
    public void onItemSelected(AdapterView<?> parent, View view, int pos, long id) {
        indexOfCategoryToDelete = pos;
        Log.i("DELETE CATEGORY", "Pos=" + pos);
    }

    @Override
    public void onNothingSelected(AdapterView<?> parent) {

    }

    public List<CalendarEvent> canDeleteCategory(ExpenseCategoryCallback callback, ExpenseCategory category) {
        return callback.canDeleteCategory(totalEvents, category);
    }


    @Override
    public Dialog onCreateDialog(Bundle savedInstanceState) {
        LayoutInflater inflater = LayoutInflater.from(getContext());
        View v = inflater.inflate(R.layout.expense_category_dialog, null);
        categoryInput = v.findViewById(R.id.categoryName);
        categorySelections = v.findViewById(R.id.categoriesList);
        clearCategoriesBtn = v.findViewById(R.id.clearCategoriesBtn);

        ArrayAdapter<String> categorySelectionsAdapter = new ArrayAdapter<String>(getContext(),
                android.R.layout.simple_spinner_item, existingCategories);
        categorySelectionsAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        categorySelections.setAdapter(categorySelectionsAdapter);
        categorySelections.setOnItemSelectedListener(this);

        categoryInput.setVisibility(View.GONE);
        categorySelections.setVisibility(View.GONE);
        clearCategoriesBtn.setVisibility(View.GONE);


        clearCategoriesBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                AlertDialog dialog = new AlertDialog.Builder(getContext())
                        .setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog, int which) {
                                dismiss();
                            }
                        })
                        .setMessage("Confirm deletion of all existing categories?")
                        .setPositiveButton("Confirm", new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog, int which) {
                                Bundle result = new Bundle();
                                result.putBoolean("clear_categories", true);
                                getParentFragmentManager().setFragmentResult("fragment_data", result);
                                getDialog().dismiss();
                            }
                        })
                        .create();
                dialog.show();
            }
        });

        return new AlertDialog.Builder(getContext())
                .setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        dismiss();
                    }
                })
                .setView(v)
                .setPositiveButton("Confirm", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        switch (currentSelectedChoice) {
                            // If the user has opted to create a new category
                            case 0:
                                if (categoryInput.getText().toString().isEmpty()) {
                                    Toast.makeText(getContext(), "Category name cannot be empty! Please try again", Toast.LENGTH_LONG).show();
                                    return;
                                }
                                Bundle nameOfCategory = new Bundle();
                                nameOfCategory.putString("category_name", categoryInput.getText().toString());
                                getParentFragmentManager().setFragmentResult("fragment_data", nameOfCategory);
                                break;

                            // If the user has opted to delete an existing category
                            case 1:
                                // Before deleting the ExpenseCategory, check if there are any existing ExpenseEvents
                                // with the ExpenseCategory to delete, set as its category
                                ExpenseCategory categoryToDelete = new ExpenseCategory(existingCategories[indexOfCategoryToDelete]);
                                List<CalendarEvent> eventsWithSelectedCategory = canDeleteCategory(new ExpenseCategoryCallback() {
                                    @Override
                                    public List<CalendarEvent> canDeleteCategory(HashMap<Integer, HashMap<LocalDate, ArrayList<CalendarEvent>>> totalEvents, ExpenseCategory category) {
                                        List<CalendarEvent> eventsWithCategory = new ArrayList<CalendarEvent>();
                                        for (Map.Entry<Integer, HashMap<LocalDate, ArrayList<CalendarEvent>>> month: totalEvents.entrySet()) {
                                            HashMap<LocalDate, ArrayList<CalendarEvent>> eventsInMonth = month.getValue();
                                            for (Map.Entry<LocalDate, ArrayList<CalendarEvent>> monthlyEvents: eventsInMonth.entrySet()) {
                                                ArrayList<CalendarEvent> events = monthlyEvents.getValue();
                                                for (int i = 0; i < events.size(); i++) {
                                                    CalendarEvent event = events.get(i);
                                                    if (event instanceof ExpensesEvent) {
                                                        if (((ExpensesEvent) event).getCategory().equals(category)) {
                                                            eventsWithCategory.add(event);
                                                        }
                                                    }
                                                }
                                            }
                                        }
                                        return eventsWithCategory;
                                        }
                                    }
                                , categoryToDelete);

                                // If the resulting list of events with the category to delete is empty, we can safely delete the category
                                if (eventsWithSelectedCategory.isEmpty()) {
                                    Bundle deleteCategoryIndex = new Bundle();
                                    deleteCategoryIndex.putInt("deleted_category_index", indexOfCategoryToDelete);
                                    getParentFragmentManager().setFragmentResult("fragment_data", deleteCategoryIndex);
                                // The list is not empty, this means there are some events using the category the user wishes to delete
                                } else {
                                    Toast.makeText(getContext(), "Could not delete category, category in use with existing events", Toast.LENGTH_LONG).show();

                                    // Creating an array of String to set as the list for the Single Choice Items
                                    // Array is populated with events whose categories are conflicting with the selected category to delete
                                    String[] eventsList = new String[eventsWithSelectedCategory.size()];
                                    for (int i = 0; i < eventsWithSelectedCategory.size(); i++) {
                                        eventsList[i] = eventsWithSelectedCategory.get(i).toString();
                                    }
                                    AlertDialog eventsListDialog = new AlertDialog.Builder(getContext())
                                            .setMessage("Could not delete category, " + eventsWithSelectedCategory.size() + " events (shown below) are currently using the category." +
                                                    "Change the events' categories or delete the events and try again." +
                                                    "\n\n\n" + String.join("\n", eventsList))
                                            .setSingleChoiceItems(eventsList, -1, new DialogInterface.OnClickListener() {
                                                @Override
                                                public void onClick(DialogInterface dialog, int which) {
                                                    Log.i("DELETE CATEGORY", eventsWithSelectedCategory.get(which).toString());
                                                }
                                            })
                                            .setPositiveButton("OK", new DialogInterface.OnClickListener() {
                                                @Override
                                                public void onClick(DialogInterface dialog, int which) {
                                                    // TODO If the user made any changes to the list of events, handle them here
                                                }
                                            })
                                            .create();
                                    eventsListDialog.show();
                                }
                        }
                    }
                })
                .setSingleChoiceItems(new String[]{"Create Category", "Delete Category"}, currentSelectedChoice, new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        currentSelectedChoice = which;
                        Log.i("CATEGORY CHOICE", "Index=" + currentSelectedChoice);
                        switch (which) {
                            // Create Category option is selected
                            case 0:
                                categoryInput.setVisibility(View.VISIBLE);
                                categorySelections.setVisibility(View.GONE);
                                clearCategoriesBtn.setVisibility(View.GONE);
                                break;

                            // Delete Category option is selected
                            case 1:
                                categorySelections.setVisibility(View.VISIBLE);
                                clearCategoriesBtn.setVisibility(View.VISIBLE);
                                categoryInput.setVisibility(View.GONE);
                                break;
                        }
                    }
                })
                .create();
    }
}
