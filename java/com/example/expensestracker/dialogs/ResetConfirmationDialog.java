package com.example.expensestracker;

import android.app.AlertDialog;
import android.app.Dialog;
import android.content.DialogInterface;
import android.os.Bundle;
import android.util.Log;

import androidx.fragment.app.DialogFragment;

public class ResetConfirmationDialog extends DialogFragment {

    @Override
    public Dialog onCreateDialog(Bundle savedInstanceState) {
        AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
        builder.setMessage("Reset all calendar and monthly data?").setPositiveButton("Yes", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialogInterface, int i) {
                Bundle result = new Bundle();
                result.putInt("reset", 1);
                getParentFragmentManager().setFragmentResult("reset", result);
            }
        }).setNegativeButton("No", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialogInterface, int i) {
                Bundle result = new Bundle();
                result.putInt("reset", 0);
                getParentFragmentManager().setFragmentResult("reset", result);
            }
        });
                return builder.create();
    }
}
