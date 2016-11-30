package com.jordan.jordanfitnessapp;

import android.app.AlarmManager;
import android.app.Notification;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;
import android.os.Bundle;
import android.os.SystemClock;
import android.provider.Settings;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.app.NotificationCompat;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;


/**
 * Created by Jordan on 11/29/2016.
 */

public class MainActivity extends AppCompatActivity implements SensorEventListener {
    //I'd like to hide this in a menu, but i'm worried a user might not ultimately find it,
    //so we go with bold and noticeable since we have the screen real estate available
    private Button signoutButton;
    private Button walkReminderButton;
    private SensorManager sensorManager;
    private boolean activityRunning;
    private TextView stepsTodayTextView;
    private boolean remindersOn = false;
    private int notificationId = 0;
    private String LOG_TAG = "MainActivity";
    private FitnessAppNotificationPublisher notificationPublisher = new FitnessAppNotificationPublisher();
    private PendingIntent pendingNotificationIntent = null;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        signoutButton = (Button) findViewById(R.id.signout_button);
        stepsTodayTextView = (TextView) findViewById(R.id.steps_today_textview);
        walkReminderButton = (Button) findViewById(R.id.walk_reminders_button);

        signoutButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                onBackPressed();
            }
        });

        walkReminderButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                remindersOn = !remindersOn;
                if(remindersOn){
                    walkReminderButton.setText(R.string.walk_reminder_on_button_text);
                    scheduleReminderNotification();
                } else {
                    walkReminderButton.setText(R.string.walk_reminder_off_button_text);
                    cancelReminderNotifications();
                }
            }
        });

        sensorManager = (SensorManager) getSystemService(Context.SENSOR_SERVICE);
        walkReminderButton.callOnClick();
    }

    //Notification Scheduling reference: http://stackoverflow.com/questions/36902667/how-to-schedule-notification-in-android
    private void scheduleReminderNotification(){
        Log.d(LOG_TAG,"Setting reminder notification to fire off");
        if(pendingNotificationIntent == null) {
            NotificationCompat.Builder builder = new NotificationCompat.Builder(this);
            builder.setContentTitle(getString(R.string.notification_title));
            builder.setContentText(getString(R.string.notification_message));
            builder.setAutoCancel(true);
            builder.setSmallIcon(R.drawable.notification_icon);
            builder.setVibrate(new long[]{1000, 1000});
            builder.setSound(Settings.System.DEFAULT_NOTIFICATION_URI);

            Intent intent = new Intent(this, MainActivity.class);
            PendingIntent activity = PendingIntent.getActivity(this, notificationId, intent, PendingIntent.FLAG_CANCEL_CURRENT);
            builder.setContentIntent(activity);

            Notification notification = builder.build();

            Intent notificationIntent = new Intent(this, FitnessAppNotificationPublisher.class);
            notificationIntent.putExtra(notificationPublisher.NOTIFICATION_ID, notificationId);
            notificationIntent.putExtra(notificationPublisher.NOTIFICATION, notification);
            pendingNotificationIntent = PendingIntent.getBroadcast(this, 0, notificationIntent, 0);
        }


        AlarmManager alarmManager = (AlarmManager) getSystemService(Context.ALARM_SERVICE);
        alarmManager.setRepeating(AlarmManager.ELAPSED_REALTIME_WAKEUP,SystemClock.elapsedRealtime() + 1000,AlarmManager.INTERVAL_HOUR,pendingNotificationIntent);
    }

    @Override
    protected void onResume(){
        super.onResume();
        activityRunning = true;
        //reference: https://developer.android.com/reference/android/hardware/Sensor.html#TYPE_STEP_COUNTER
        Sensor countSensor = sensorManager.getDefaultSensor(Sensor.TYPE_STEP_COUNTER);
        if(countSensor != null){
            sensorManager.registerListener(this, countSensor,SensorManager.SENSOR_DELAY_UI);
        } else {
            Toast.makeText(this, getString(R.string.count_sensor_unavailable_error_message), Toast.LENGTH_LONG).show();
        }
    }

    @Override
    protected void onPause(){
        super.onPause();
        activityRunning = false;
    }

    //sensormanager reference: https://github.com/theelfismike/android-step-counter/blob/master/src/com/starboardland/pedometer/CounterActivity.java
    @Override
    public void onSensorChanged(SensorEvent event){
        if(activityRunning){
            stepsTodayTextView.setText(String.valueOf(event.values[0]));
        }
    }

    @Override
    public void onAccuracyChanged(android.hardware.Sensor sensor, int i){

    }

    @Override
    public void onBackPressed() {
        new AlertDialog.Builder(this)
                .setTitle("Sign Out")
                .setMessage("Are you sure you want to sign out?")
                .setPositiveButton(android.R.string.yes, new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int which) {
                        logout();
                    }
                })
                .setNegativeButton(android.R.string.no, new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int which) {
                        // do nothing
                    }
                })
                .setIcon(android.R.drawable.ic_dialog_alert)
                .show();
    }

    private void logout(){
        Intent loginIntent = new Intent(MainActivity.this, LoginActivity.class);
        MainActivity.this.startActivity(loginIntent);
        sensorManager.unregisterListener(this);
    }

    private void cancelReminderNotifications(){
        NotificationManager mNotificationManager = (NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE);
        mNotificationManager.cancel(notificationId);
        AlarmManager alarmManager = (AlarmManager) getSystemService(Context.ALARM_SERVICE);
        alarmManager.cancel(pendingNotificationIntent);
    }
}