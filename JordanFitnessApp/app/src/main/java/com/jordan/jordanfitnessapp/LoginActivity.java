package com.jordan.jordanfitnessapp;

import android.content.DialogInterface;
import android.content.Intent;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileOutputStream;
import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayList;

/**
 * Created by Jordan on 11/29/2016.
 */
public class LoginActivity extends AppCompatActivity {
    private Button continueButton;
    private Button loginButton;
    private Button createAccountButton;
    private TextView menuModeTitleTextView;
    private EditText usernameEditText;
    private EditText passwordEditText;
    private boolean loginMode = true;
    ArrayList<LoginInfo> loginInfos = new ArrayList<LoginInfo>();
    String loginFileName = "LoginData";
    private static final String LOG_TAG = "LoginActivity";

    public class LoginInfo{
        public String userName;
        public String password;
        public int numSteps;
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);
        continueButton = (Button) findViewById(R.id.continue_button);
        loginButton = (Button) findViewById(R.id.login_button);
        createAccountButton = (Button) findViewById(R.id.create_account_button);
        menuModeTitleTextView = (TextView) findViewById(R.id.menu_mode_title_textview);
        usernameEditText = (EditText) findViewById(R.id.username_edittext);
        passwordEditText = (EditText) findViewById(R.id.password_edittext);

        continueButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if(loginMode) {
                    Log.i(LOG_TAG, "continue button clicked, validating data");
                    boolean validLogin = validateLoginInfo();
                } else { //we're in create account mode
                    Log.i(LOG_TAG, "continue button clicked, attempting to create account");
                    createNewUserAccount();
                }
            }
        });

        loginButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Log.i(LOG_TAG, "Login Button clicked, swapping to login mode");
                loginMode = true;
                continueButton.setText("Log In");
                menuModeTitleTextView.setText(getString(R.string.login_activity_login_title_text));
            }
        });

        createAccountButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Log.i(LOG_TAG, "create account button clicked, swapping to create account mode");
                loginMode = false;
                continueButton.setText("Create Account");
                menuModeTitleTextView.setText(getString(R.string.login_activity_create_account_title_text));
            }
        });


        //Load in the list of usernames / passwords
        File saveData = new File(getFilesDir().getPath() + loginFileName);

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
                        LoginInfo newInfo = new LoginInfo();
                        newInfo.userName = infoParts[0];
                        newInfo.password = infoParts[1];
                        loginInfos.add(newInfo);
                    }
                }
                br.close();
            }
            catch (IOException e) {
                spawnAlertDialog(getString(R.string.error_loading_savedata_message) + e.toString());
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
    private boolean validateLoginInfo(){
        String newUserName = usernameEditText.getText().toString().trim();
        String newPassword = passwordEditText.getText().toString().trim();

        //query the local save data for a list of username/password combos we have stored
        boolean foundUserName = false;
        for(int i=0; i < loginInfos.size();i++){
            LoginInfo currentLoginInfo = loginInfos.get(i);
            if(newUserName.equals(currentLoginInfo.userName)) {
                foundUserName = true;
                if (newPassword.equals(currentLoginInfo.password)) {
                    //Found a match log us in
                    login();
                    return true;
                } else {
                    //if the username matches something but the password doesn't tell the user "wrong password" clear the password field and try again
                    passwordEditText.setText("");
                    spawnAlertDialog(getString(R.string.wrong_password_error_message));
                    return false;
                }
            }
        }
        //if the username doesn't match any of them post a message to the user saying that the username is unrecognized
        if(!foundUserName){
            spawnAlertDialog(getString(R.string.wrong_username_error_message));
        }
        return false;
    }

    private void createNewUserAccount(){
        String newUserName = usernameEditText.getText().toString().trim();
        String newPassword = passwordEditText.getText().toString().trim();

        //Do error checking for username and password
        if(newUserName.length() == 0){
            resetFields();
            spawnAlertDialog(getString(R.string.empty_username_error_message));
            return;
        }
        if(newPassword.length() == 0){
            spawnAlertDialog(getString(R.string.empty_password_error_message));
            return;
        }

        //This doesn't scale great if there are 10 million billion users but this works for demo purposes
        for(int i=0; i < loginInfos.size();i++){
            if(newUserName.equals(loginInfos.get(i).userName)){
                spawnAlertDialog("Username " + newUserName + " is in use, please enter another");
                resetFields();
                return;
            }
        }
        FileOutputStream outputStream;

        //Append the new save data to the list
        LoginInfo newInfo = new LoginInfo();
        newInfo.userName = newUserName;
        newInfo.password = newPassword;
        newInfo.numSteps = 0;
        loginInfos.add(newInfo);

        //Save out the new savedata chunk
        String newSaveData = "";
        for(int i=0 ; i < loginInfos.size();i++){
            newSaveData += loginInfos.get(i).userName + "," + loginInfos.get(i).password;
        }
        Log.d(LOG_TAG,"Creating new User account with username: " + newUserName + " and password: " + newPassword);

        try {
            outputStream = new FileOutputStream(new File(getFilesDir() + loginFileName));
            outputStream.write(newSaveData.getBytes());
            outputStream.close();
            Log.d(LOG_TAG,"Successfully wrote out new account");
        } catch (Exception e) {
            Log.e(LOG_TAG,"Encountered error writing to file while creating new account: " + e.toString());
            e.printStackTrace();
        }

        //proceed to log the user into the account
        login();
    }

    private void login(){
        resetFields();
        Log.d(LOG_TAG,"Credentials correct, logging in user");
        Intent mainIntent = new Intent(this, MainActivity.class);
        //mainIntent.putExtra("key", value); //Optional parameters
        this.startActivity(mainIntent);
    }

    private void spawnAlertDialog(String alertMessage){
        new AlertDialog.Builder(this)
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

    private void resetFields(){
        usernameEditText.setText("");
        passwordEditText.setText("");
    }
}
