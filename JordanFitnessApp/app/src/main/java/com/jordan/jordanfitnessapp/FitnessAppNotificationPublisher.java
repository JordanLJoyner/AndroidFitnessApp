package com.jordan.jordanfitnessapp;

import android.app.Notification;
import android.app.NotificationManager;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.util.Log;

/**
 * Created by Jordan on 11/30/2016.
 */

public class FitnessAppNotificationPublisher extends BroadcastReceiver {
    public String NOTIFICATION_ID = "notification_id";
    public String NOTIFICATION = "notification";

    @Override
    public void onReceive(final Context context, Intent intent) {
        Log.d("BroadcastReciever","Broadcast Receiver notified firing off notification message");
        NotificationManager notificationManager = (NotificationManager) context.getSystemService(Context.NOTIFICATION_SERVICE);

        Notification notification = intent.getParcelableExtra(NOTIFICATION);
        int notificationId = intent.getIntExtra(NOTIFICATION_ID, 0);
        notificationManager.notify(notificationId, notification);
    }
}