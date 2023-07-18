package com.example.expensestracker.helpers;

import java.util.ArrayList;
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

    public static Double get_total(List<Double> values) {
        double max = values.get(0);
        for (int i = 1; i < values.size(); i++) {
            if (values.get(i) > max) {
                max = values.get(i);
            }
        }
        return max;
    }
}
