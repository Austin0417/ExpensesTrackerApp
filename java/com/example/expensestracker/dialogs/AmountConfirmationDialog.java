package com.example.expensestracker.dialogs;

import android.app.AlertDialog;
import android.app.Dialog;
import android.content.DialogInterface;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.EditText;
import android.widget.Spinner;
import android.widget.Toast;

import androidx.fragment.app.DialogFragment;

import com.example.expensestracker.MainActivity;
import com.example.expensestracker.R;
import com.example.expensestracker.calendar.ExpenseCategory;
import com.example.expensestracker.helpers.CreateEventFromImage;

import java.util.List;

public class AmountConfirmationDialog extends DialogFragment implements AdapterView.OnItemSelectedListener {
    private double extractedTotal;
    private EditText amount;
    private Spinner categorySelection;
    private CreateEventFromImage createEvent;
    private List<ExpenseCategory> categories;
    private int currentSelectionIndex;

    public AmountConfirmationDialog(double extractedTotal, List<ExpenseCategory> categories) {
        this.extractedTotal = extractedTotal;
        this.categories = categories;
    }

    public View initialize() {
        createEvent = (MainActivity) getContext();
        LayoutInflater inflater = LayoutInflater.from(getContext());
        View v = inflater.inflate(R.layout.amount_confirmation, null);
        amount = v.findViewById(R.id.totalAmount);
        amount.setText(Double.toString(extractedTotal));
        categorySelection = v.findViewById(R.id.confirmCategory);

        String[] selections = new String[categories.size() + 1];
        selections[selections.length - 1] = "Other";
        for (int i = 0; i < categories.size(); i++) {
            selections[i] = categories.get(i).toString();
        }
        ArrayAdapter<String> adapter = new ArrayAdapter<String>(getContext(), android.R.layout.simple_spinner_item, selections);
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        categorySelection.setAdapter(adapter);
        categorySelection.setOnItemSelectedListener(this);


        return v;
    }

    @Override
    public Dialog onCreateDialog(Bundle savedInstanceState) {
        View v = initialize();

        return new AlertDialog.Builder(getContext())
                .setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {

                    }
                })
                .setPositiveButton("Confirm", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        if (amount.getText().toString().isEmpty()) {
                            Toast.makeText(getContext(), "Inputted amount was empty. Aborting...", Toast.LENGTH_LONG).show();
                            return;
                        }
                        ExpenseCategory selectedCategory;
                        if (currentSelectionIndex < categories.size()) {
                            selectedCategory = categories.get(currentSelectionIndex);
                        } else {
                            selectedCategory = new ExpenseCategory("Other");
                        }
                        createEvent.createEvent(Double.parseDouble(amount.getText().toString()), selectedCategory);
                    }
                })
                .setView(v)
                .create();

    }

    @Override
    public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
        currentSelectionIndex = position;
    }

    @Override
    public void onNothingSelected(AdapterView<?> parent) {
        currentSelectionIndex = 0;
    }
}
