package com.example.expensestracker.dialogs;

import android.app.AlertDialog;
import android.app.Dialog;
import android.content.DialogInterface;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.EditText;
import android.widget.Toast;

import androidx.fragment.app.DialogFragment;

import com.example.expensestracker.R;

// Implementation for the dialog that pops up when the user clicks the "Add Expense" button
public class AddExpenseDialog extends DialogFragment {
    private EditText expensesNameInput;
    private EditText expensesAmountInput;

    @Override
    public Dialog onCreateDialog(Bundle savedInstanceState) {

        LayoutInflater inflater = LayoutInflater.from(getContext());
        View v = inflater.inflate(R.layout.add_expense_dialog, null);
        expensesNameInput = v.findViewById(R.id.expenseNameInput);
        expensesAmountInput = v.findViewById(R.id.expenseAmountInput);

        return new AlertDialog.Builder(requireContext())
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
                        String description = expensesNameInput.getText().toString();
                        String amount = expensesAmountInput.getText().toString();
                        if (description.isEmpty() || amount.isEmpty()) {
                            Toast.makeText(getContext(), "One or more fields empty. Please try again.", Toast.LENGTH_LONG).show();
                            return;
                        }
                        Bundle result = new Bundle();
                        result.putString("expense_description", description);
                        result.putDouble("expense_amount", Double.parseDouble(amount));
                        getParentFragmentManager().setFragmentResult("monthly_info", result);
                        dismiss();
                    }
                })
                .setTitle("Add Monthly Expense")
                .create();
    }
}
