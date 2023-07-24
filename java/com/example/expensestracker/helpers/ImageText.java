package com.example.expensestracker.helpers;

import android.util.Log;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class ImageText {
    public static List<Double> processImageText(String text) {
        List<Double> res = new ArrayList<Double>();
        Pattern pattern = Pattern.compile("([0-9]+[.][0-9]+)");
        Matcher matcher = pattern.matcher(text);
        while (matcher.find()) {
            String decimal = matcher.group();
            res.add(Double.parseDouble(decimal));
        }
        return res;
    }

    public static Double get_total(List<Double> values, boolean takeSecondHighest) {
        if (values.size() < 2) {
            return (double) -1;
        }
        Collections.sort(values, Collections.reverseOrder());
        if (!takeSecondHighest) {
            Log.i("IMAGE TEXT", "No cash keyword found, selecting highest value...");
            return values.get(0);
        } else {
            Log.i("IMAGE TEXT", "Cash keyword found, selecting second highest...");
            return values.get(1);
        }
    }

    public static boolean shouldTakeSecondMax(String imageText) {
        if (imageText.toUpperCase().contains("CASH") || imageText.toUpperCase().contains("CHANGE")) {
            return true;
        } else {
            return false;
        }
    }
}
