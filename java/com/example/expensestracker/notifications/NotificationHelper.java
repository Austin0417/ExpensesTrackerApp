package com.example.expensestracker.notifications;

import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.content.Context;
import android.os.Build;

import androidx.core.app.NotificationManagerCompat;

import com.example.expensestracker.R;

import java.util.UUID;

public class NotificationHelper {
    public static final String CHANNEL_ID = "111";
    public static int notificationID = UUID.randomUUID().hashCode();


    public static void generateRandomID(int notificationID) {
        notificationID = UUID.randomUUID().hashCode();
    }
    public static NotificationManagerCompat createNotificationChannel(Context context) {
        NotificationManagerCompat manager = null;
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            String name = context.getString(R.string.channel_name);
            String description = context.getString(R.string.channel_description);
            int importance = NotificationManager.IMPORTANCE_HIGH;
            NotificationChannel channel = new NotificationChannel(CHANNEL_ID, name, importance);
            channel.setDescription(description);
            channel.enableVibration(true);

            manager = NotificationManagerCompat.from(context);
            manager.createNotificationChannel(channel);
        }
        return manager;
    }
}
