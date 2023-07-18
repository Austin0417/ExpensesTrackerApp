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

import com.example.expensestracker.MainActivity;
import com.example.expensestracker.R;
import com.example.expensestracker.helpers.CreateEventFromImage;

public class AmountConfirmationDialog extends DialogFragment {
    private double extractedTotal;
    private EditText amount;
    private CreateEventFromImage createEvent;

    public AmountConfirmationDialog(double extractedTotal) {
        this.extractedTotal = extractedTotal;
    }

    @Override
    public Dialog onCreateDialog(Bundle savedInstanceState) {
        createEvent = (MainActivity) getContext();
        LayoutInflater inflater = LayoutInflater.from(getContext());
        View v = inflater.inflate(R.layout.amount_confirmation, null);
        amount = v.findViewById(R.id.totalAmount);
        amount.setText(Double.toString(extractedTotal));
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
                        createEvent.createEvent(Double.parseDouble(amount.getText().toString()));
                    }
                })
                .setView(v)
                .create();

    }
}
