package com.example.expensestracker.notifications;

import android.annotation.SuppressLint;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.media.RingtoneManager;
import android.util.Log;

import androidx.core.app.NotificationCompat;
import androidx.core.app.NotificationManagerCompat;

import com.example.expensestracker.R;

public class ExpenseNotificationReceiver extends BroadcastReceiver {
    @Override
    @SuppressLint("MissingPermission")
    public void onReceive(Context context, Intent intent) {
        int eventHashCode = intent.getIntExtra("hash_code", -1);
        int daysBeforeAlert = intent.getIntExtra("days_before_alert", -1);
        String category = intent.getStringExtra("category");
        String description = intent.getStringExtra("description");
        Log.i("EXPENSE NOTIFICATION RECEIVER", "Category=" + category + " Description=" + description);

        NotificationManagerCompat manager = NotificationHelper.createNotificationChannel(context);

        NotificationCompat.Builder builder = new NotificationCompat.Builder(context, "222")
                .setSmallIcon(R.drawable.budget_limit_alert)
                .setContentTitle("Expense Alert: " + category)
                .setContentText(description)
                .setPriority(NotificationCompat.PRIORITY_HIGH)
                .setAutoCancel(true)
                .setVibrate(new long[]{0, 500, 500, 500})
                .setSound(RingtoneManager.getDefaultUri(RingtoneManager.TYPE_NOTIFICATION));
        int notificationId = -1;
        NotificationHelper.generateRandomID(notificationId);
        manager.notify(notificationId, builder.build());
    }
}
