package com.example.expensestracker.dialogs;

import android.app.Dialog;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Spinner;

import androidx.fragment.app.DialogFragment;

import com.example.expensestracker.R;
import com.example.expensestracker.calendar.ExpenseCategory;
import com.example.expensestracker.calendar.ExpensesEvent;

import java.util.List;

public class PieExpenseDialog extends DialogFragment {
    private EditText amountInput;
    private Spinner eventCategory;
    private Button deleteBtn;

    private List<ExpenseCategory> existingCategories;
    private List<ExpensesEvent> events;


    public PieExpenseDialog(List<ExpenseCategory> existingCategories, List<ExpensesEvent> events) {
        this.existingCategories = existingCategories;
        this.events = events;
    }

    @Override
    public Dialog onCreateDialog(Bundle savedInstanceState) {
        LayoutInflater inflater = LayoutInflater.from(getContext());
        View v = inflater.inflate(R.layout.edit_event, null);
        initialize(v);
        String[] choices = createSingleItemChoiceOptions();
        return null;
    }

    private void initialize(View view) {
        amountInput = view.findViewById(R.id.amountText);
        eventCategory = view.findViewById(R.id.selectedExpenseCategory);
        deleteBtn = view.findViewById(R.id.deleteBtn);

        amountInput.setVisibility(View.GONE);
        eventCategory.setVisibility(View.GONE);
        deleteBtn.setVisibility(View.GONE);
    }

    private String[] createSingleItemChoiceOptions() {
        String[] choices = new String[events.size()];
        for (int i = 0; i < choices.length; i++) {
            choices[i] = events.get(i).toString();
        }
        return choices;
    }

    private void setSpinnerOptions() {
        String[] spinnerOptions = new String[existingCategories.size()];
        for (int i = 0; i < spinnerOptions.length; i++) {
            spinnerOptions[i] = existingCategories.get(i).getName();
        }
        ArrayAdapter<String> adapter = new ArrayAdapter<String>(getContext(), android.R.layout.simple_spinner_item, spinnerOptions);
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        eventCategory.setAdapter(adapter);
    }
}
