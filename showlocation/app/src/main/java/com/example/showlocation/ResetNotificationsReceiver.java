package com.example.showlocation;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.util.Log;

import java.util.ArrayList;
import java.util.List;

public class ResetNotificationsReceiver extends BroadcastReceiver {
    @Override
    public void onReceive(Context context, Intent intent) {
        Log.d("ResetNotificationsReceiver", "Resetting notifications at 23:00");
        // Clear notifications in MapActivity
        List<String> notifications = new ArrayList<>(); // Clear the notification list
        List<String> receivedNotifications = new ArrayList<>(); // Clear the received notifications
    }
}
