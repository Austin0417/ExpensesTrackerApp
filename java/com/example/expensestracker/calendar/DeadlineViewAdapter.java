package com.example.expensestracker.calendar;

import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.example.expensestracker.R;

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
