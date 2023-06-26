package com.example.expensestracker.calendar;

import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

public class DeadlineViewAdapter extends RecyclerView.Adapter<DeadlineViewAdapter.ViewHolder>{
    private String[] localDataset;
    public static class ViewHolder extends RecyclerView.ViewHolder {
        private TextView textView;
        public ViewHolder(View view) {
            super(view);

        }
        public TextView getTextView() { return textView; }
    }
    @NonNull
    @Override
    public DeadlineViewAdapter.ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        return null;
    }

    @Override
    public void onBindViewHolder(@NonNull DeadlineViewAdapter.ViewHolder holder, int position) {

    }

    @Override
    public int getItemCount() {
        return localDataset.length;
    }
}
