package com.example.expensestracker.calendar;

import android.content.Context;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.recyclerview.widget.RecyclerView;

import com.example.expensestracker.MainActivity;
import com.example.expensestracker.R;
import com.example.expensestracker.helpers.ViewType;

import java.util.ArrayList;
import java.util.List;


public class CustomAdapter extends RecyclerView.Adapter<CustomAdapter.ViewHolder> {
    private ViewType viewType;
    private List<ExpenseCategory> categories;
    private String[] localDataSet;
    private int datasetCounter = 0;
    public static class ViewHolder extends RecyclerView.ViewHolder {
        private final TextView textView;
        private EditEvent eventPasser;
        private Context calendarContext;
        public ViewHolder(View view) {
            super(view);
            calendarContext = view.getContext();
            eventPasser = (EditEvent) calendarContext;
            textView = view.findViewById(R.id.listTextView);
            view.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    // On click listener for all views within the RecyclerView
                    // We parse the text at the clicked element, and obtain the month, day, and year of the CalendarEvent
                    if (textView.getText().toString() != null && !textView.getText().toString().isEmpty()) {
                        String s = textView.getText().toString();
                        String splitString[] = s.split(": ");
                        String formattedDate[] = splitString[0].split("/");

                        int month = Integer.parseInt(formattedDate[0]);
                        int day = Integer.parseInt(formattedDate[1]);
                        int year = Integer.parseInt(formattedDate[2]);
                        // Pass this back to MainActivity via a callback of EditEvent
                        eventPasser.sendCalendarEventDate(month, year, day);
                    } else {
                        Log.i("RecyclerView", "User click on invalid View");
                    }
                }
            });
        }
        public TextView getTextView() {
            return textView;
        }
    }

    public CustomAdapter(String[] dataset, List<ExpenseCategory> categories) {
        localDataSet = dataset;
        this.categories = categories;
        Log.i("Dataset", "Dataset initialized!" + returnDataset());
    }
    public void setDataset(String[] dataset) {
        localDataSet = dataset;
    }
    @Override
    public ViewHolder onCreateViewHolder(ViewGroup viewGroup, int viewType) {
        View view = LayoutInflater.from(viewGroup.getContext()).inflate(R.layout.recycler_view_layout, viewGroup, false);
        return new ViewHolder(view);
    }
    @Override
    public void onBindViewHolder(ViewHolder holder, int position) {
//        if (position < categories.size()) {
//            holder.getTextView().setText(categories.get(position).getName());
//        } else {
//            holder.getTextView().setText(localDataSet[datasetCounter]);
//            datasetCounter++;
//        }
        holder.getTextView().setText(localDataSet[position]);
    }
    @Override
    public int getItemCount() {
        return localDataSet.length;
    }

//    @Override
//    public int getItemViewType(int position) {
//        if (position == 0) {
//            return VIEW_TYPE_CATEGORY;
//        } else {
//            return VIEW_TYPE_EVENT;
//        }
//    }


    public String returnDataset() {
        String res = "";
        for (int i = 0; i < localDataSet.length; i++) {
            res += localDataSet[i] + " ";
        }
        return res;
    }
}