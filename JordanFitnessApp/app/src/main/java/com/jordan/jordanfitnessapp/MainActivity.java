package com.jordan.jordanfitnessapp;

import android.content.Context;
import android.content.Intent;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;


/**
 * Created by Jordan on 11/29/2016.
 */

//sensormanager reference: https://github.com/theelfismike/android-step-counter/blob/master/src/com/starboardland/pedometer/CounterActivity.java
public class MainActivity extends AppCompatActivity implements SensorEventListener {
    //I'd like to hide this in a menu, but i'm worried a user might not ultimately find it,
    //so we go with bold and noticeable since we have the screen real estate available
    private Button signoutButton;
    private Button walkReminderButton;
    private SensorManager sensorManager;
    private boolean activityRunning;
    private TextView stepsTodayTextView;
    private boolean remindersOn = true;

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
                logout();
            }
        });

        walkReminderButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                remindersOn = !remindersOn;
                if(remindersOn){
                    walkReminderButton.setText(R.string.walk_reminder_on_button_text);
                } else {
                    walkReminderButton.setText(R.string.walk_reminder_off_button_text);
                }
            }
        });

        sensorManager = (SensorManager) getSystemService(Context.SENSOR_SERVICE);
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

    @Override
    public void onSensorChanged(SensorEvent event){
        if(activityRunning){
            stepsTodayTextView.setText(String.valueOf(event.values[0]));
        }
    }

    @Override
    public void onAccuracyChanged(android.hardware.Sensor sensor, int i){

    }

    private void logout(){
        Intent loginIntent = new Intent(MainActivity.this, LoginActivity.class);
        //mainIntent.putExtra("key", value); //Optional parameters
        MainActivity.this.startActivity(loginIntent);
        sensorManager.unregisterListener(this);
    }

}
