package com.example.expensestracker.notifications;

import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.content.Context;
import android.os.Build;

import androidx.core.app.NotificationManagerCompat;

import com.example.expensestracker.R;

import java.util.UUID;

public class NotificationHelper {
    public static final String DEADLINE_CHANNEL_ID = "111";
    public static final String EXPENSE_CHANNEL_ID = "222";
    public static int notificationID = UUID.randomUUID().hashCode();


    public static void generateRandomID(int notificationID) {
        notificationID = UUID.randomUUID().hashCode();
    }
    public static NotificationManagerCompat createNotificationChannel(Context context) {
        NotificationManagerCompat manager = null;
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            String[] channelNames = new String[]{context.getString(R.string.expense_channel_name), context.getString(R.string.deadline_channel_name)};
            String[] channelDescriptions = new String[]{context.getString(R.string.expense_channel_description), context.getString(R.string.deadline_channel_description)};
            String[] channelId = new String[]{EXPENSE_CHANNEL_ID, DEADLINE_CHANNEL_ID};

            for (int i = 0; i < channelNames.length; i++) {
                String name = channelNames[i];
                String description = channelDescriptions[i];
                String id = channelId[i];
                NotificationChannel channel = new NotificationChannel(id, name, NotificationManager.IMPORTANCE_HIGH);
                channel.setDescription(description);
                channel.enableVibration(true);
                manager = NotificationManagerCompat.from(context);
                manager.createNotificationChannel(channel);
            }
//            String name = context.getString(R.string.deadline_channel_name);
//            String description = context.getString(R.string.deadline_channel_description);
//            int importance = NotificationManager.IMPORTANCE_HIGH;
//            NotificationChannel channel = new NotificationChannel(DEADLINE_CHANNEL_ID, name, importance);
//            channel.setDescription(description);
//            channel.enableVibration(true);
//
//            manager = NotificationManagerCompat.from(context);
//            manager.createNotificationChannel(channel);
        }
        return manager;
    }
}
