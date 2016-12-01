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

import org.w3c.dom.Text;

import java.util.ArrayList;
import java.util.Calendar;


/**
 * Created by Jordan on 11/29/2016.
 */

public class MainActivity extends AppCompatActivity implements SensorEventListener {
    //I'd like to hide this in a menu, but i'm worried a user might not ultimately find it,
    //so we go with bold and noticeable since we have the screen real estate available
    private int notificationId = 0;
    private float lastLoggedStepAmount = -1;
    private float stepCelebrationNumber = 1000;
    private Button signoutButton;
    private Button walkReminderButton;
    private TextView currentUserTextView;
    private TextView stepsTodayTextView;
    private TextView staticsticsForDayTextView;
    private TextView topWalker1Textview;
    private TextView topWalker2Textview;
    private TextView topWalker3Textview;
    TextView averageStepsPerHourTextView;
    TextView averageStepsPerMinuteTextView;
    private boolean activityRunning;
    private boolean remindersOn = false;
    private String LOG_TAG = "MainActivity";
    private SensorManager sensorManager;
    private FitnessAppNotificationPublisher notificationPublisher = new FitnessAppNotificationPublisher();
    private PendingIntent pendingNotificationIntent = null;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        UserInfoManager.UserInfo currentUser = UserInfoManager.getInstance().getActiveUser();
        signoutButton = (Button) findViewById(R.id.signout_button);
        stepsTodayTextView = (TextView) findViewById(R.id.steps_today_textview);
        walkReminderButton = (Button) findViewById(R.id.walk_reminders_button);
        currentUserTextView = (TextView) findViewById(R.id.current_user_textview);
        staticsticsForDayTextView = (TextView) findViewById(R.id.statistics_for_day_textview);
        averageStepsPerHourTextView = (TextView) findViewById(R.id.average_steps_per_hour_textview);
        averageStepsPerMinuteTextView = (TextView) findViewById(R.id.average_steps_per_minute_textview);
        topWalker1Textview = (TextView) findViewById(R.id.top_walker_1_textview);
        topWalker2Textview = (TextView) findViewById(R.id.top_walker_2_textview);
        topWalker3Textview = (TextView) findViewById(R.id.top_walker_3_textview);

        currentUserTextView.setText("Current User: " + currentUser.userName);
        stepsTodayTextView.setText("Steps Today: " + currentUser.numSteps);

        staticsticsForDayTextView.setText("Statistics For Day: " + (Calendar.getInstance().get(Calendar.MONTH)+1) + "/" + Calendar.getInstance().get(Calendar.DATE));
        updateStatsAndLeaderboardTextViews();

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
        alarmManager.setRepeating(AlarmManager.ELAPSED_REALTIME_WAKEUP,SystemClock.elapsedRealtime() + AlarmManager.INTERVAL_HOUR,AlarmManager.INTERVAL_HOUR,pendingNotificationIntent);
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
        UserInfoManager.getInstance().saveUserInfo(this);
    }

    //sensormanager reference: https://github.com/theelfismike/android-step-counter/blob/master/src/com/starboardland/pedometer/CounterActivity.java
    @Override
    public void onSensorChanged(SensorEvent event){
        if(activityRunning){
            //Increment the numSteps for the active user
            //If we've been asleep and we're just now updating give us credit for all the steps we took in the inbetween
            boolean triggeredCelebration = false;
            if(lastLoggedStepAmount < 0){
                lastLoggedStepAmount = event.values[0];
            } else {
                float stepsToAward = event.values[0] - lastLoggedStepAmount;
                lastLoggedStepAmount = event.values[0];
                if(((UserInfoManager.getInstance().getActiveUser().numSteps % stepCelebrationNumber) + stepsToAward) >= stepCelebrationNumber){
                    triggeredCelebration = true;
                }
                UserInfoManager.getInstance().getActiveUser().numSteps+= stepsToAward;
                updateStatsAndLeaderboardTextViews();
            }
            //if the user has surpassed the celebratory number of steps spawn a popup dialogue telling them GOOD JERB
            if(triggeredCelebration){
                new AlertDialog.Builder(this)
                        .setTitle("Congratulations")
                        .setMessage("Congratulations you just walked " + stepCelebrationNumber + " steps.  Keep up the good work!")
                        .setPositiveButton(android.R.string.yes, new DialogInterface.OnClickListener() {
                            public void onClick(DialogInterface dialog, int which) {
                                // continue with delete
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
            stepsTodayTextView.setText("Steps Today: " + UserInfoManager.getInstance().getActiveUser().numSteps);
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

    private void updateStatsAndLeaderboardTextViews(){
        UserInfoManager.UserInfo currentUser = UserInfoManager.getInstance().getActiveUser();
        String stepsPerHour = String.format("%.2f",  currentUser.numSteps / 24.0f);
        String stepsPerMinute = String.format("%.2f",(currentUser.numSteps / 24.0f) / 60.0f);
        averageStepsPerHourTextView.setText("Avg. Steps Per Hour: " + stepsPerHour);
        averageStepsPerMinuteTextView.setText("Avg. Steps Per Minute: " + stepsPerMinute);

        //This becomes less efficient the more users we have but this works for demo purposes
        ArrayList<UserInfoManager.UserInfo> top3Walkers = UserInfoManager.getInstance().getTop3Walkers();
        if(top3Walkers.size() > 0){
            topWalker1Textview.setText("1: " + top3Walkers.get(0).userName + " with " + top3Walkers.get(0).numSteps + " steps");
        }
        if(top3Walkers.size() > 1){
            topWalker2Textview.setText("2: " + top3Walkers.get(1).userName + " with " + top3Walkers.get(1).numSteps + " steps");
        }
        if(top3Walkers.size() > 2){
            topWalker3Textview.setText("3: " + top3Walkers.get(2).userName + " with " + top3Walkers.get(2).numSteps + " steps");
        }
    }

    private void logout(){
        UserInfoManager.getInstance().saveUserInfo(this);
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