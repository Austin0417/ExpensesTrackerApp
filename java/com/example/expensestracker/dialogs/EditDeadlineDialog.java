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

import com.example.expensestracker.R;
import com.example.expensestracker.calendar.DeadlineEvent;
import com.example.expensestracker.calendar.EditEvent;

import java.util.ArrayList;

public class EditDeadlineDialog extends DialogFragment {
    // Reference to the user-selected deadline
    private final ArrayList<DeadlineEvent> deadlines;
    private final DeadlineEvent targetDeadline;

    // Index of the selected deadline back in MainActivity's DeadlineEvent ArrayList
    private final int deadlineIndex;

    // Previous information of the selected deadline (before the user has made any changes to the information)
    // We store this for usage in a potential query later on

    private EditText deadlineInfoText;
    private EditText deadlineAmountText;
    private Button deleteBtn;
    private EditEvent editEvent;


    public EditDeadlineDialog(int index, ArrayList<DeadlineEvent> deadlines) {
        this.deadlines = deadlines;
        deadlineIndex = index;
        targetDeadline = deadlines.get(index);
    }

    @Override
    public Dialog onCreateDialog(Bundle savedInstanceState) {
        View v = LayoutInflater.from(getContext()).inflate(R.layout.edit_deadline, null);
        deadlineInfoText = v.findViewById(R.id.deadlineInfoText);
        deadlineAmountText = v.findViewById(R.id.deadlineAmountText);
        deleteBtn = v.findViewById(R.id.deleteDeadlineBtn);
        editEvent = (EditEvent) getContext();

        // Set the info and amount field to their initial values (e.g. the deadline amount and information before the user has changed anything)
        deadlineInfoText.setText(targetDeadline.getInformation());
        deadlineAmountText.setText(String.valueOf(targetDeadline.getAmount()));

        deleteBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                AlertDialog dialog = new AlertDialog.Builder(getContext())
                        .setTitle("Delete deadline")
                        .setMessage("Are you sure you want to delete the selected deadline?")
                        .setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog, int which) {
                                dismiss();
                            }
                        })
                        .setPositiveButton("Confirm", new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog, int which) {
                                // Callback to deleteDeadlineEvent in MainActivity
                                editEvent.deleteDeadlineEvent(deadlineIndex, targetDeadline);

                                getDialog().dismiss();
                                dismiss();
                            }
                        }).create();
                dialog.show();
            }
        });

        return new AlertDialog.Builder(getContext())
                .setTitle("Edit Deadline")
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
                        double previousAmount = targetDeadline.getAmount();
                        String previousInformation = targetDeadline.getInformation();
                        targetDeadline.setAmount(Double.parseDouble(deadlineAmountText.getText().toString()));
                        targetDeadline.setInformation(deadlineInfoText.getText().toString());
                        Log.i("Update", "New amount: " + targetDeadline.getAmount() + " New info: " + targetDeadline.getInformation());

                        // Callback to MainActivity's modifyDeadlineEvent implementation
                        editEvent.modifyDeadlineEvent(targetDeadline, previousInformation, previousAmount, deadlineIndex);
                    }
                }).create();
    }
}
