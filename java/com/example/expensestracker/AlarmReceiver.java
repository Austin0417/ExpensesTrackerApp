package com.example.expensestracker;

import android.annotation.SuppressLint;
import android.app.NotificationManager;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.media.RingtoneManager;
import android.os.Bundle;
import android.text.Html;
import android.text.SpannableString;
import android.util.Log;

import androidx.annotation.NonNull;
import androidx.core.app.NotificationCompat;
import androidx.core.app.NotificationManagerCompat;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.FirebaseApp;
import com.google.firebase.messaging.FirebaseMessaging;
import com.google.firebase.messaging.RemoteMessage;

import org.json.JSONException;
import org.json.JSONObject;

public class AlarmReceiver extends BroadcastReceiver {
    @Override
    @SuppressLint("MissingPermission")
    public void onReceive(Context context, Intent intent) {
        Log.i("Alarm", "Alarm successfully activated!");
        if (intent.getExtras() != null) {
            // TODO Find a way to send payload to FCM in this method
            JSONObject payload = new JSONObject();
            int currentYear = intent.getIntExtra("year", -1);
            int currentMonth = intent.getIntExtra("month", -1);
            int currentDay = intent.getIntExtra("day", -1);
            double amount = intent.getDoubleExtra("amount", -1);
            String information = intent.getStringExtra("information");
            String notificationText = "Date: " + currentMonth + "/" + currentDay + "/" + currentYear + "<br>Amount: " + amount + "<br>" + information;
            SpannableString formattedText = new SpannableString(Html.fromHtml(notificationText, Html.FROM_HTML_MODE_LEGACY));
            NotificationCompat.Builder builder = new NotificationCompat.Builder(context, NotificationHelper.CHANNEL_ID)
                    .setContentTitle("Deadline Alert")
                    .setContentText(formattedText)
                    .setStyle(new NotificationCompat.BigTextStyle().bigText(formattedText).setBigContentTitle("Deadline Alert"))
                    .setPriority(NotificationCompat.PRIORITY_HIGH)
                    .setSmallIcon(R.drawable.budget_limit_alert)
                    .setAutoCancel(true)
                    .setVibrate(new long[]{0, 500, 500, 500})
                    .setSound(RingtoneManager.getDefaultUri(RingtoneManager.TYPE_NOTIFICATION));
            NotificationManagerCompat manager = NotificationHelper.createNotificationChannel(context);
            manager.notify(NotificationHelper.notificationID, builder.build());
            NotificationHelper.generateRandomID(NotificationHelper.notificationID);
            FirebaseMessaging.getInstance().getToken().addOnCompleteListener(new OnCompleteListener<String>() {
                @Override
                public void onComplete(@NonNull Task<String> task) {
                    if (!task.isSuccessful()) {
                        return;
                    }
                    DeadlineMessagingService.DEVICE_TOKEN = task.getResult();
                    try {
                        JSONObject notificationData = new JSONObject();
                        JSONObject payloadData = new JSONObject();
                        notificationData.put("title", "Upcoming deadline");
                        notificationData.put("body", "Alert, a deadline is approaching!");
                        payloadData.put("amount", amount);
                        payloadData.put("year", currentYear);
                        payloadData.put("month", currentMonth);
                        payloadData.put("day", currentDay);
                        payload.put("notification", notificationData);
                        payload.put("data", payloadData);
                        if (DeadlineMessagingService.DEVICE_TOKEN != null) {
                            Log.i("Device Token", DeadlineMessagingService.DEVICE_TOKEN);
                            payload.put("to", DeadlineMessagingService.DEVICE_TOKEN);
                        }
                    } catch (JSONException e) {
                        throw new RuntimeException(e);
                    }
                    FirebaseMessaging.getInstance().send(new RemoteMessage.Builder(CalendarFragment.SENDER_ID + "@fcm.googleapis.com")
                            .addData("payload", payload.toString())
                            .build());
                }
            });
        }
    }
}
