package com.example.expensestracker.calendar;

import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.example.expensestracker.R;

// Overridden RecyclerView adapter for the RecyclerView within DeadlineDialog
public class DeadlineViewAdapter extends RecyclerView.Adapter<DeadlineViewAdapter.ViewHolder>{
    private String[] localDataset;
    public static class ViewHolder extends RecyclerView.ViewHolder {
        private TextView textView;
        private EditEvent editEvent;
        public ViewHolder(View view) {
            super(view);
            textView = view.findViewById(R.id.deadlineText);
            editEvent = (EditEvent) view.getContext();
            view.setOnClickListener(new View.OnClickListener() {
                // On click listener for all TextViews within the RecyclerView
                // When a user selects a specific TextView, which represents an individual deadline, obtain the raw text within that TextView
                // Parse the string data from the TextView to obtain the month, day, year, amount, and information
                @Override
                public void onClick(View v) {
                    String deadlineText = textView.getText().toString();
                    Log.i("View Click", deadlineText);
                    String[] dateSubstring = deadlineText.substring(0, 9).split("/");
                    String[] amountAndInfoSubstring = deadlineText.substring(12).split(" - ");

                    double amount = Double.parseDouble(amountAndInfoSubstring[0]);
                    String information = amountAndInfoSubstring[1];
                    int month = Integer.parseInt(dateSubstring[0]);
                    int day = Integer.parseInt(dateSubstring[1]);
                    int year = Integer.parseInt(dateSubstring[2]);

                    // Callback to MainActivity's sendDeadlineEventDate, we send the parsed information back to MainActivity's implemented callback
                    editEvent.sendDeadlineEventDate(amount, information, month, year, day);
                }
            });
        }
        public TextView getTextView() { return textView; }
    }

    public DeadlineViewAdapter(String[] dataset) {
        localDataset = dataset;
    }

    @NonNull
    @Override
    public DeadlineViewAdapter.ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        // Rendering the layout for each specific View element in the RecyclerView. In this case, it is a single TextView element
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.deadline_info_text_layout, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull DeadlineViewAdapter.ViewHolder holder, int position) {
        holder.getTextView().setText(localDataset[position]);
    }

    @Override
    public int getItemCount() {
        return localDataset.length;
    }
}
