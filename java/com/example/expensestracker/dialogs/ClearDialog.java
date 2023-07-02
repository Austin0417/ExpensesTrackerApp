package com.example.expensestracker.dialogs;

import android.app.AlertDialog;
import android.app.Dialog;
import android.content.DialogInterface;
import android.os.Bundle;
import android.util.SparseBooleanArray;

import androidx.fragment.app.DialogFragment;

public class ClearDialog extends DialogFragment {
    // Boolean array representing the status of the calendar and deadline checkboxes (index 0 and 1 respectively)
    private boolean[] selection = {false, false};
    @Override
    public Dialog onCreateDialog(Bundle savedInstanceState) {
        String[] choices = {"Calendar", "Deadlines"};

        return new AlertDialog.Builder(requireContext())
                .setTitle("Clear Events")
                .setPositiveButton("OK", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        Bundle result = new Bundle();
                        result.putBooleanArray("buttons_selected", selection);
                        getParentFragmentManager().setFragmentResult("fragment_data", result);
                    }
                })
                .setMultiChoiceItems(choices, null, new DialogInterface.OnMultiChoiceClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which, boolean isChecked) {
                        if (which == 0 && isChecked) {
                            // If the Calendar checkbox is clicked, and current status is checked
                            selection[0] = true;
                        } else if (which == 1 && isChecked) {
                            // If the deadline checkbox is clicked, and current status is checked
                            selection[1] = true;
                        } else if (which == 0 && !isChecked) {
                            // If the Calendar checkbox is clicked, and current status is not checked
                            selection[0] = false;
                        } else if (which == 1 && !isChecked) {
                            // If the deadline checkbox is clicked, and current status is not checked
                            selection[1] = false;
                        }
                    }
                })
                .setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        dismiss();
                    }
                }).create();
    }
}
