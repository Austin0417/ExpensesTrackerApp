package com.example.expensestracker.notifications;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.util.Log;

public class ExpenseNotificationReceiver extends BroadcastReceiver {
    @Override
    public void onReceive(Context context, Intent intent) {
        int eventHashCode = intent.getIntExtra("hash_code", -1);
        int daysBeforeAlert = intent.getIntExtra("days_before_alert", -1);
        String category = intent.getStringExtra("category");
        String description = intent.getStringExtra("description");
        Log.i("EXPENSE NOTIFICATION RECEIVER", "Category=" + category + " Description=" + description);
    }
}
