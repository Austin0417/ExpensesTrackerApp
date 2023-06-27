package com.example.expensestracker.dialogs;

import android.app.AlertDialog;
import android.app.Dialog;
import android.content.DialogInterface;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;

import androidx.fragment.app.DialogFragment;

import com.example.expensestracker.R;
import com.example.expensestracker.calendar.DeadlineEvent;
import com.example.expensestracker.calendar.EditEvent;

public class EditDeadlineDialog extends DialogFragment {
    private final DeadlineEvent targetDeadline;
    private final int deadlineIndex;
    private final String previousInformation;
    private EditText deadlineInfoText;
    private EditText deadlineAmountText;
    private Button deleteBtn;
    private EditEvent editEvent;


    public EditDeadlineDialog(int index, DeadlineEvent targetDeadline) {
        this.targetDeadline = targetDeadline;
        deadlineIndex = index;
        previousInformation = targetDeadline.getInformation();
    }

    @Override
    public Dialog onCreateDialog(Bundle savedInstanceState) {
        View v = LayoutInflater.from(getContext()).inflate(R.layout.edit_deadline, null);
        deadlineInfoText = v.findViewById(R.id.deadlineInfoText);
        deadlineAmountText = v.findViewById(R.id.deadlineAmountText);
        deleteBtn = v.findViewById(R.id.deleteDeadlineBtn);
        editEvent = (EditEvent) getContext();

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
                        targetDeadline.setAmount(Double.parseDouble(deadlineAmountText.getText().toString()));
                        targetDeadline.setInformation(deadlineInfoText.getText().toString());
                        editEvent.modifyDeadlineEvent(targetDeadline, previousInformation);
                    }
                }).create();
    }
}
