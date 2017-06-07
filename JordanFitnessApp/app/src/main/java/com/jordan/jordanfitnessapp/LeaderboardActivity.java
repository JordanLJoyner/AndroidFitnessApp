package com.jordan.jordanfitnessapp;

import android.jordan.com.userinfomodule.UserInfoManager;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.widget.ArrayAdapter;
import android.widget.ListView;

import java.util.ArrayList;
import java.util.PriorityQueue;

/**
 * Created by Jordan on 12/2/2016.
 */

//Displays a list of all the users registered to the app sorted by steps for the day
    //This class assumes the user only wanted to see the list at the second they hit the button to load this activity
    //so it doesn't update in real time
public class LeaderboardActivity extends AppCompatActivity {
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_leaderboard);
        PriorityQueue<UserInfoManager.UserInfo> sortedWalkers = UserInfoManager.getInstance().getSortedUsers();
        ArrayList<String> displayValues = new ArrayList<String>();
        int counter = 1;
        while(sortedWalkers.size() > 0){
            UserInfoManager.UserInfo walker = sortedWalkers.poll();
            String displayValue = counter + ": " + walker.userName + " with " + walker.numSteps + " steps";
            displayValues.add(displayValue);
            counter++;
        }
        ArrayAdapter adapter = new ArrayAdapter<String>(this,
                android.R.layout.simple_list_item_1, displayValues);

        ListView listView = (ListView) findViewById(R.id.leaderboard_listview);
        listView.setAdapter(adapter);
    }
}
