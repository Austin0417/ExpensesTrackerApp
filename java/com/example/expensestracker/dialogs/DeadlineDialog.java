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
import com.example.expensestracker.calendar.DeadlineEvent;
import com.example.expensestracker.calendar.DeadlineViewAdapter;

import java.util.ArrayList;

public class DeadlineDialog extends DialogFragment {
    private RecyclerView recyclerView;
    private DeadlineViewAdapter mAdapter;
    private RecyclerView.LayoutManager mLayoutManager;
    private ArrayList<DeadlineEvent> deadlines;

    public DeadlineDialog(ArrayList<DeadlineEvent> deadlines) {
        this.deadlines = deadlines;
    }

    @Override
    public Dialog onCreateDialog(Bundle savedInstanceState) {
        ((MainActivity) getActivity()).setDeadlineDialog(this);
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
        for (int i = 0; i < deadlines.size(); i++) {
            dataset[i] = deadlines.get(i).getMonth() + "/" + deadlines.get(i).getDay() + "/" + deadlines.get(i).getYear() + ": $" + deadlines.get(i).getAmount() + " - " + deadlines.get(i).getInformation();
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
}
