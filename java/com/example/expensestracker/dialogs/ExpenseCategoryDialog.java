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

public class ExpenseCategoryDialog extends DialogFragment {
    private EditText categoryInput;

    @Override
    public Dialog onCreateDialog(Bundle savedInstanceState) {
        LayoutInflater inflater = LayoutInflater.from(getContext());
        View v = inflater.inflate(R.layout.expense_category_dialog, null);
        categoryInput = v.findViewById(R.id.categoryName);

        return new AlertDialog.Builder(getContext())
                .setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        dismiss();
                    }
                })
                .setView(v)
                .setPositiveButton("Create category", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        if (categoryInput.getText().toString().isEmpty()) {
                            Toast.makeText(getContext(), "Category name cannot be empty! Please try again", Toast.LENGTH_LONG).show();
                            return;
                        }
                        Bundle result = new Bundle();
                        result.putString("category_name", categoryInput.getText().toString());
                        getParentFragmentManager().setFragmentResult("fragment_data", result);
                    }
                })
                .create();
    }
}
