package com.example.expensestracker.notifications;

import android.util.Log;

import com.google.firebase.messaging.FirebaseMessagingService;
import com.google.firebase.messaging.RemoteMessage;

import java.util.Map;

public class DeadlineMessagingService extends FirebaseMessagingService {
    public static String DEVICE_TOKEN = null;
    @Override
    public void onNewToken(String token) {
        Log.i("Token", token);
        DEVICE_TOKEN = token;
    }
    @Override
    public void onMessageReceived(RemoteMessage message) {
        Map<String, String> data = message.getData();
        if (data != null) {
            Log.i("Received Firebase Message", "Deadline amount: " + data.get("amount") + ". Date: " + data.get("month") + "/" + data.get("day") + "/" + data.get("year"));

        }
    }
}
