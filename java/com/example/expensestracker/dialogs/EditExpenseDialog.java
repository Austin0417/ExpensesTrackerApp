package com.example.expensestracker.dialogs;

import android.app.AlertDialog;
import android.app.Dialog;
import android.content.DialogInterface;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;

import androidx.fragment.app.DialogFragment;

import com.example.expensestracker.MainActivity;
import com.example.expensestracker.R;
import com.example.expensestracker.monthlyinfo.MonthlyExpense;
import com.example.expensestracker.monthlyinfo.PassMonthlyData;

// Implementation for the dialog that pops up after a user clicks on a particular MonthlyExpense in the RecyclerView
public class EditExpenseDialog extends DialogFragment {
    private final MonthlyExpense selectedExpense;
    private final int index;
    private EditText descriptionInput;
    private EditText amountInput;
    private Button deleteBtn;
    private PassMonthlyData passMonthlyData;

    public EditExpenseDialog(MonthlyExpense expense, int index) {
        selectedExpense = expense;
        this.index = index;
    }

    @Override
    public Dialog onCreateDialog(Bundle savedInstanceState) {
        LayoutInflater inflater = LayoutInflater.from(getContext());
        View v = inflater.inflate(R.layout.edit_expense, null);

        descriptionInput = v.findViewById(R.id.descriptionInput);
        amountInput = v.findViewById(R.id.amountInput);
        deleteBtn = v.findViewById(R.id.editExpenseDeleteBtn);
        passMonthlyData = (MainActivity) getContext();

        descriptionInput.setText(selectedExpense.getDescription());
        amountInput.setText(Double.toString(selectedExpense.getAmount()));

        // When the user clicks on the delete button within the pop-up dialog, show another pop-up dialog asking for confirmation
        deleteBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                AlertDialog dialog = new AlertDialog.Builder(getContext())
                        .setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog, int which) {
                                dismiss();
                            }
                        })
                        .setMessage("Delete selected monthly expense?")
                        .setPositiveButton("Confirm", new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog, int which) {
                                passMonthlyData.deleteExpense(index);
                                getDialog().dismiss();
                            }
                        })
                        .create();
                dialog.show();
            }
        });

        return new AlertDialog.Builder(getContext())
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
                        String previousDescription = selectedExpense.getDescription();
                        double previousAmount = selectedExpense.getAmount();
                        Log.i("MONTHLY EXPENSE", "Old Expense: " + selectedExpense);

                        // If confirm was pressed and there were no changes to the MonthlyExpense, return early
                        if (descriptionInput.getText().toString().equals(previousDescription)
                                && Double.parseDouble(amountInput.getText().toString()) == previousAmount) {
                            return;
                        }
                        passMonthlyData.updateExpense(descriptionInput.getText().toString(),
                                Double.parseDouble(amountInput.getText().toString()), selectedExpense, index);
                    }
                })
                .create();
    }
}
