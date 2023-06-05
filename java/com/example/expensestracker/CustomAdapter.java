package com.example.expensestracker;

import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.recyclerview.widget.RecyclerView;


public class CustomAdapter extends RecyclerView.Adapter<CustomAdapter.ViewHolder> {
    private String[] localDataSet;
    public static class ViewHolder extends RecyclerView.ViewHolder {
        private final TextView textView;
        public ViewHolder(View view) {
            super(view);
            textView = (TextView) view.findViewById(R.id.listTextView);
            view.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    Log.i("Click", "Element" + getAdapterPosition() + " clicked");
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
