package com.example.expensestracker.pie;

import com.github.mikephil.charting.formatter.ValueFormatter;

import java.text.DecimalFormat;

public class PieFormatter extends ValueFormatter {
    private DecimalFormat format;
    public PieFormatter() {
        format = new DecimalFormat("###,###,##0.00");
    }

    @Override
    public String getFormattedValue(float value) {
        return format.format(value) + "%";
    }
}
