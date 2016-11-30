package com.jordan.jordanfitnessapp;

import android.app.Activity;
import android.content.DialogInterface;
import android.support.v7.app.AlertDialog;
import android.util.Log;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileOutputStream;
import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayList;

/**
 * Created by Jordan on 11/30/2016.
 */

public class UserInfoManager {
    private static UserInfoManager instance = null;
    ArrayList<UserInfo> loginInfos = new ArrayList<UserInfo>();
    String loginFileName = "LoginData";
    private String LOG_TAG = "UserInfoManager";
    private int activeUserIndex;

    public class UserInfo{
        public String userName;
        public String password;
        public int numSteps;
    }

    //Singleton Functions
    protected UserInfoManager() {
        // Exists only to defeat instantiation.
    }
    public static UserInfoManager getInstance() {
        if(instance == null) {
            instance = new UserInfoManager();
        }
        return instance;
    }

    //Public interface

    public void loadLoginInfo(Activity activity){
        //Load in the list of usernames / passwords
        File saveData = new File(activity.getFilesDir().getPath() + loginFileName);

        if(saveData.exists()){
            //read in the save data
            Log.i(LOG_TAG,"Save data exists, loading it in now");
            try {
                BufferedReader br = new BufferedReader(new FileReader(saveData));
                String line;

                while ((line = br.readLine()) != null) {
                    String[] infoParts = line.split(",");
                    if(infoParts.length > 2 || infoParts.length < 0){
                        Log.e(LOG_TAG,"Save data corrupted, a line from the save data is longer than 2 parts: " + line);
                    } else {
                        UserInfo newInfo = new UserInfo();
                        newInfo.userName = infoParts[0];
                        newInfo.password = infoParts[1];
                        loginInfos.add(newInfo);
                    }
                }
                br.close();
            }
            catch (IOException e) {
                spawnAlertDialog(activity,activity.getString(R.string.error_loading_savedata_message) + e.toString());
            }
            Log.i(LOG_TAG,"Save data loaded.  Found " + loginInfos.size() + " members saved");
            for(int i=0; i < loginInfos.size(); i++){
                Log.d(LOG_TAG,"Username: " + loginInfos.get(i).userName);// + " Password: " + loginInfos.get(i).password);
            }
        } else {
            //create the save data
            Log.i(LOG_TAG,"No save data detected at " + saveData.getAbsolutePath());
        }
    }

    //Checks to see if the username and the password match any of the saved username/passwords
    //Called when the continue button is clicked in loginMode
    //Returns true if theres a username/password match with local saved data, spawns a message and returns false otherwise
    public boolean isLoginInfoValid(Activity activity, String newUserName, String newPassword){
        newUserName = newUserName.trim();
        newPassword = newPassword.trim();
        //query the local save data for a list of username/password combos we have stored
        for(int i=0; i < loginInfos.size();i++){
            UserInfo currentLoginInfo = loginInfos.get(i);
            if(newUserName.equals(currentLoginInfo.userName)) {
                if (newPassword.equals(currentLoginInfo.password)) {
                    //Found a match log us in
                    activeUserIndex = i;
                    return true;
                } else {
                    //if the username matches something but the password doesn't tell the user "wrong password" clear the password field and try again
                    spawnAlertDialog(activity, activity.getString(R.string.wrong_password_error_message));
                    return false;
                }
            }
        }
        //if the username doesn't match any of them post a message to the user saying that the username is unrecognized
        spawnAlertDialog(activity, activity.getString(R.string.wrong_username_error_message));
        return false;
    }

    //Creates a new user account, adds it to the current managed list, and writes it to the save data in local storage
    //returns true if the account was successfully created, returns false and spawns a message if the account was not successfully created
    public boolean createNewUserAccount(Activity activity, String newUserName, String newPassword){
        newUserName = newUserName.trim();
        newPassword = newPassword.trim();

        //Do error checking for username and password
        if(newUserName.length() == 0){
            spawnAlertDialog(activity,activity.getString(R.string.empty_username_error_message));
            return false;
        }
        if(newPassword.length() == 0){
            spawnAlertDialog(activity,activity.getString(R.string.empty_password_error_message));
            return false;
        }

        //This doesn't scale great if there are 10 million billion users but this works for demo purposes
        for(int i=0; i < loginInfos.size();i++){
            if(newUserName.equals(loginInfos.get(i).userName)){
                spawnAlertDialog(activity,"Username " + newUserName + " is in use, please enter another");
                return false;
            }
        }

        //Append the new save data to the list
        UserInfo newInfo = new UserInfo();
        newInfo.userName = newUserName;
        newInfo.password = newPassword;
        newInfo.numSteps = 0;
        loginInfos.add(newInfo);
        activeUserIndex = loginInfos.size()-1;

        //Save out the new savedata chunk
        FileOutputStream outputStream;

        String newSaveData = "";
        for(int i=0 ; i < loginInfos.size();i++){
            newSaveData += loginInfos.get(i).userName + "," + loginInfos.get(i).password + "\n";
        }
        Log.d(LOG_TAG,"Creating new User account with username: " + newUserName + " and password: " + newPassword);

        try {
            outputStream = new FileOutputStream(new File(activity.getFilesDir() + loginFileName));
            outputStream.write(newSaveData.getBytes());
            outputStream.close();
            Log.d(LOG_TAG,"Successfully wrote out new account");
        } catch (Exception e) {
            Log.e(LOG_TAG,"Encountered error writing to file while creating new account: " + e.toString());
            e.printStackTrace();
            return false;
        }
        return true;
    }

    public UserInfo getActiveUser(){
        if(activeUserIndex > -1 && activeUserIndex < loginInfos.size()){
            return loginInfos.get(activeUserIndex);
        }
        return null;
    }

    //this really belongs in a utility class or file but I don't want to make a whole extra file just for 1 function
    private void spawnAlertDialog(Activity activity, String alertMessage){
        new AlertDialog.Builder(activity)
                .setTitle("Problem Occured")
                .setMessage(alertMessage)
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
}