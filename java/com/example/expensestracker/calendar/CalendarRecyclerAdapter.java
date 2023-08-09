package com.example.expensestracker.calendar;

import android.content.Context;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.recyclerview.widget.RecyclerView;

import com.example.expensestracker.R;
import com.example.expensestracker.helpers.ViewType;

import java.util.List;


public class CalendarRecyclerAdapter extends RecyclerView.Adapter<CalendarRecyclerAdapter.ViewHolder> {
    private ViewType viewType;
    private List<ExpenseCategory> categories;
    private List<CalendarEvent> events;
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
                        try {
                            String s = textView.getText().toString();
                            String splitString[] = s.split(": ");
                            String formattedDate[] = splitString[0].split("/");

                            int month = Integer.parseInt(formattedDate[0]);
                            int day = Integer.parseInt(formattedDate[1]);
                            int year = Integer.parseInt(formattedDate[2]);
                            // Pass this back to MainActivity via a callback of EditEvent
                            eventPasser.sendCalendarEventDate(month, year, day, getAdapterPosition());
                        } catch (NumberFormatException e) {
                            return;
                        } finally {
                            Log.i("VIEW CLICK","Adapter Pos=" + getAdapterPosition());
                        }
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

    public CalendarRecyclerAdapter(List<CalendarEvent> events, List<ExpenseCategory> categories) {
        this.events = events;
        this.categories = categories;
    }

    public void setDataset(List<CalendarEvent> events) {
        this.events = events;
        notifyDataSetChanged();
    }

    public void setCategories(List<ExpenseCategory> categories) {
        this.categories = categories;
        notifyDataSetChanged();
    }

    @Override
    public ViewHolder onCreateViewHolder(ViewGroup viewGroup, int viewType) {
        View view = LayoutInflater.from(viewGroup.getContext()).inflate(R.layout.recycler_view_layout, viewGroup, false);
        return new ViewHolder(view);
    }
    @Override
    public void onBindViewHolder(ViewHolder holder, int position) {
        if (position < categories.size()) {
            holder.getTextView().setText(categories.get(position).getName());
        }
        else if (position >= categories.size() && position < categories.size() + 1) {
            holder.getTextView().setText("Income");
        }
        else {
            if (position < events.size() && events.get(position) != null) {
                holder.getTextView().setText(events.get(position).toString());
            } else {
                holder.getTextView().setText("");
            }
        }
        //holder.getTextView().setText(events.get(position).toString());
    }
    @Override
    public int getItemCount() {
        return events.size() + categories.size() - 2;
    }

    public List<CalendarEvent> clearDataset() {
        for (int i = 0; i < events.size(); i++) {
            if (events.get(i) != null) {
                events.set(i, null);
            }
        }
        return events;
    }

    public List<CalendarEvent> getEvents() { return events; }
    public List<ExpenseCategory> getCategories() { return categories; }

}