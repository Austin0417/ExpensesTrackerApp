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
                    String deadlineText = textView.getText().toString(); // 7/11/2023 (6:30 PM): $88.0 - dgf
                    Log.i("View Click", deadlineText);
                    String unparsedDate[] = deadlineText.split(": "); // [7/11/2023 (6:30 PM), $88.0 - dgf]
                    String dateAndTime = unparsedDate[0]; // 7/11/2023 (6:30 PM)
                    String amountAndDescription[] = unparsedDate[1].split(" - "); // [$88.0, dgf]

                    String date[] = dateAndTime.substring(0, dateAndTime.indexOf(" (")).split("/"); // [7, 11, 2023]
                    String time[] = dateAndTime.substring(dateAndTime.indexOf("(") + 1, dateAndTime.indexOf(")")).split(" "); // [6:30, PM]
                    String hourAndMinute[] = time[0].split(":"); // [6, 30]

                    int month = Integer.parseInt(date[0]); // 7
                    int day = Integer.parseInt(date[1]); // 11
                    int year = Integer.parseInt(date[2]); // 2023
                    int hour = Integer.parseInt(hourAndMinute[0]); // 6
                    int minute = Integer.parseInt(hourAndMinute[1]); // 30
                    String hourType = time[1]; // PM
                    double amount = Double.parseDouble(amountAndDescription[0].substring(1)); // 88.0
                    String description = amountAndDescription[1]; // dgf

//                    String splitString[] = deadlineText.split(": ");
//                    String formattedDate = splitString[0];
//                    String amountAndInfo = splitString[1];
//                    String regex = "(\\d{1,2})/(\\d{1,2})/(\\d{4})";
//
//
//                    String[] dateSubstring = formattedDate.split("/");
//                    String[] amountAndInfoSubstring = amountAndInfo.substring(1).split(" - ");
//
//                    double amount = Double.parseDouble(amountAndInfoSubstring[0]);
//                    String information = amountAndInfoSubstring[1];
//                    int month = Integer.parseInt(dateSubstring[0]);
//                    int day = Integer.parseInt(dateSubstring[1]);
//                    int year = Integer.parseInt(dateSubstring[2]);

                    // Callback to MainActivity's sendDeadlineEventDate, we send the parsed information back to MainActivity's implemented callback
                    editEvent.sendDeadlineEventDate(amount, description, month, year, day, hour, minute, hourType);
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
