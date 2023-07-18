package com.example.expensestracker.monthlyinfo;

import android.content.Context;
import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;
import androidx.fragment.app.FragmentResultListener;

import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.TextView;

import com.example.expensestracker.R;
import com.example.expensestracker.MainActivity;
import com.example.expensestracker.dialogs.ResetConfirmationDialog;


/**
 * A simple {@link Fragment} subclass.
 * Use the {@link MonthlyInfoFragment#newInstance} factory method to
 * create an instance of this fragment.
 */
public class MonthlyInfoFragment extends Fragment {

    // TODO: Rename parameter arguments, choose names that match
    // the fragment initialization parameters, e.g. ARG_ITEM_NUMBER
    private static final String ARG_PARAM1 = "param1";
    private static final String ARG_PARAM2 = "param2";

    // TODO: Rename and change types of parameters
    private String mParam1;
    private String mParam2;

    private Button backBtn;
    private Button resetBtn;
    private TextView expensesInput;
    private TextView incomeInput;

    private final double expenses;
    private final double income;

    private PassMonthlyData monthlyData;
    public MainActivity mainActivity;


    public MonthlyInfoFragment(double expenses, double income) {
        super(R.layout.fragment_monthly_info);
        this.expenses = expenses;
        this.income = income;
        // Required empty public constructor
    }

    /**
     * Use this factory method to create a new instance of
     * this fragment using the provided parameters.
     *

     * @return A new instance of fragment monthlyInfo.
     */
    // TODO: Rename and change types and number of parameters
    public static MonthlyInfoFragment newInstance(double expenses, double income) {
        MonthlyInfoFragment fragment = new MonthlyInfoFragment(expenses, income);
        Bundle args = new Bundle();
        args.putDouble("expenses", expenses);
        args.putDouble("income", income);
        fragment.setArguments(args);
        return fragment;
    }

    public PassMonthlyData getMonthlyDataPasser() { return monthlyData; }

    public double getExpensesInput() {
        if (expensesInput.getText().toString().isEmpty()) {
            return -1;
        }
        return Double.parseDouble(expensesInput.getText().toString());
    }

    public double getIncomeInput() {
        if (incomeInput.getText().toString().isEmpty()) {
            return -1;
        }
        return Double.parseDouble(incomeInput.getText().toString());
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (getArguments() != null) {
            mParam1 = getArguments().getString(ARG_PARAM1);
            mParam2 = getArguments().getString(ARG_PARAM2);
        }
        getParentFragmentManager().setFragmentResultListener("reset", this, new FragmentResultListener() {
            @Override
            public void onFragmentResult(@NonNull String requestKey, @NonNull Bundle result) {
                int fragmentResult = result.getInt("reset");
                if (fragmentResult == 1) {
                    expensesInput.setText("");
                    incomeInput.setText("");
                    FragmentManager manager = getParentFragmentManager();
                    manager.popBackStack();
                    mainActivity.resetInfo();
                    Log.i("Reset", "Reset success!");
                }
            }
        });
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View view = inflater.inflate(R.layout.fragment_monthly_info, container, false);
        backBtn = view.findViewById(R.id.backBtn);
        resetBtn = view.findViewById(R.id.resetBtn);
        expensesInput = view.findViewById(R.id.monthlyExpenses);
        incomeInput = view.findViewById(R.id.monthlyIncome);

        expensesInput.setText(Double.toString(expenses));
        incomeInput.setText(Double.toString(income));

        backBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                FragmentManager manager = getParentFragmentManager();
                if (!expensesInput.getText().toString().isEmpty() && !incomeInput.getText().toString().isEmpty()) {
                    monthlyData.onDataPassed(Double.parseDouble(expensesInput.getText().toString()), Double.parseDouble(incomeInput.getText().toString()));
                    Log.i("Data pass", "Expenses: " + expensesInput.getText().toString() + "\nIncome: " + incomeInput.getText().toString());
                }
                manager.popBackStack();
                ((MainActivity) getActivity()).unhideMainUI();
            }
        });
        resetBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                ResetConfirmationDialog resetDialog = new ResetConfirmationDialog();
                resetDialog.show(getParentFragmentManager(), "Reset confirmation");

            }
        });
        return view;
    }
    @Override
    public void onAttach(Context context) {
        super.onAttach(context);
        monthlyData = (PassMonthlyData) context;
        if (getActivity() instanceof MainActivity) {
            mainActivity = (MainActivity) getActivity();
            Log.i("Success", "Parent activity reference successfully initialized");
        } else {
            Log.i("Monthly Info Fragment", "Couldn't obtain reference to parent activity!");
        }
    }
}