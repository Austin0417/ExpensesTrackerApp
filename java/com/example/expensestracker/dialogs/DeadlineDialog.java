package com.example.expensestracker.dialogs;

import android.app.AlertDialog;
import android.app.Dialog;
import android.content.DialogInterface;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;

import androidx.fragment.app.DialogFragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.expensestracker.MainActivity;
import com.example.expensestracker.R;
import com.example.expensestracker.calendar.CalendarEvent;
import com.example.expensestracker.calendar.DeadlineEvent;
import com.example.expensestracker.calendar.DeadlineViewAdapter;

import java.util.ArrayList;

// Dialog that is shown when the user has clicked the "Deadlines" button located in the CalendarFragment layout
public class DeadlineDialog extends DialogFragment {
    private RecyclerView recyclerView;
    private DeadlineViewAdapter mAdapter;
    private RecyclerView.LayoutManager mLayoutManager;
    private ArrayList<DeadlineEvent> deadlines;
    MainActivity mainActivity;

    // Deadlines ArrayList passed from CalendarFragment
    public DeadlineDialog(ArrayList<DeadlineEvent> deadlines) {
        this.deadlines = deadlines;
    }
    public void setDeadlines(ArrayList<DeadlineEvent> deadlines) { this.deadlines = deadlines;}

    @Override
    public Dialog onCreateDialog(Bundle savedInstanceState) {
        // Initializing reference back to the MainActivity instance, and calling its setDeadlineDialog method to set this DeadlineDialog
        mainActivity = (MainActivity) getActivity();
        mainActivity.setDeadlineDialog(this);
        View v = LayoutInflater.from(getContext()).inflate(R.layout.deadline_info_layout, null, false);
        recyclerView = v.findViewById(R.id.deadlineRecyclerView);
        initializeDeadlinesView();

        return new AlertDialog.Builder(getContext())
                .setTitle("Deadlines")
                .setView(v)
                .setPositiveButton("OK", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        dialog.dismiss();
                    }
                })
                .create();
    }
    public void populateDataset(String[] dataset) {
        String hourType;
        for (int i = 0; i < deadlines.size(); i++) {
            if (deadlines.get(i).getAmOrPm() == CalendarEvent.AM) {
                hourType = "AM";
            } else {
                hourType = "PM";
            }
            dataset[i] = deadlines.get(i).getMonth() +
                    "/" + deadlines.get(i).getDay() +
                    "/" + deadlines.get(i).getYear() +
                    " (" + deadlines.get(i).getHour() +
                    ":" + deadlines.get(i).getMinute() + " " + hourType +
                    "): $" + deadlines.get(i).getAmount() +
                    " - " + deadlines.get(i).getInformation();
        }
    }
    public void initializeDeadlinesView() {
        String[] dataset = new String[deadlines.size()];
        populateDataset(dataset);
        mLayoutManager = new LinearLayoutManager(getContext());
        mAdapter = new DeadlineViewAdapter(dataset);
        recyclerView.setLayoutManager(mLayoutManager);
        recyclerView.setAdapter(mAdapter);
    }
    public void updateDeadlinesView() {
        if (mAdapter != null) {
            mAdapter.notifyDataSetChanged();
        }
    }
    @Override
    public void onDismiss(DialogInterface dialog) {
        super.onDismiss(dialog);
        mainActivity.setDeadlineDialog(null);
    }

}
