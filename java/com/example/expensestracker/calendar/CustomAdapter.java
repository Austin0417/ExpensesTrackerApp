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


public class CustomAdapter extends RecyclerView.Adapter<CustomAdapter.ViewHolder> {
    private String[] localDataSet;
    public static class ViewHolder extends RecyclerView.ViewHolder {
        private final TextView textView;
        private EditEvent eventPasser;
        private Context calendarContext;
        public ViewHolder(View view) {
            super(view);
            calendarContext = view.getContext();
            eventPasser = (EditEvent) calendarContext;
            textView = view.findViewById(R.id.listTextView);
            boolean instance = calendarContext instanceof MainActivity;
            Log.i("Context", "Context Instance: " + instance);
            view.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    String substr = textView.getText().toString().substring(0,9);
                    String[] formattedDate = substr.split("/");
                    int month = Integer.parseInt(formattedDate[0]);
                    int day = Integer.parseInt(formattedDate[1]);
                    int year = Integer.parseInt(formattedDate[2]);
                    eventPasser.sendDate(month, year, day);
                }
            });
        }
        public TextView getTextView() {
            return textView;
        }
    }
    public CustomAdapter(String[] dataset) {
        localDataSet = dataset;
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
        holder.getTextView().setText(localDataSet[position]);
    }
    @Override
    public int getItemCount() {
        return localDataSet.length;
    }

    public String returnDataset() {
        String res = "";
        for (int i = 0; i < localDataSet.length; i++) {
            res += localDataSet[i] + " ";
        }
        return res;
    }
}
