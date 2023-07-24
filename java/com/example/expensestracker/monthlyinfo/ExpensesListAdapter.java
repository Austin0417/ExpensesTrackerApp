package com.example.expensestracker.monthlyinfo;

import android.content.Context;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.example.expensestracker.MainActivity;
import com.example.expensestracker.R;

public class ExpensesListAdapter extends RecyclerView.Adapter<ExpensesListAdapter.ViewHolder> {
    private String[] localDataset;

    public static class ViewHolder extends RecyclerView.ViewHolder {
        private final TextView textView;
        PassMonthlyData passMonthlyData;

        public ViewHolder(View view) {
            super(view);
            textView = view.findViewById(R.id.expenseItem);
            Context context = view.getContext();
            passMonthlyData = (MainActivity) context;
            textView.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    String parsedExpenseText[] = textView.getText().toString().split(": ");
                    passMonthlyData.openExpenseDialog(new MonthlyExpense(parsedExpenseText[0], Double.parseDouble(parsedExpenseText[1].substring(1))));
                }
            });
        }

        public TextView getTextView() { return textView; }
    }

    public ExpensesListAdapter(String[] dataset) {
        localDataset = dataset;
    }

    @Override
    public ViewHolder onCreateViewHolder(ViewGroup viewGroup, int viewType) {
        LayoutInflater inflater = LayoutInflater.from(viewGroup.getContext());
        View v = inflater.inflate(R.layout.expenses_list_text_view, null);

        return new ViewHolder(v);
    }

    @Override
    public void onBindViewHolder(ViewHolder viewHolder, final int position) {
        viewHolder.getTextView().setText(localDataset[position]);
    }

    @Override
    public int getItemCount() { return localDataset.length; }
}
