package com.jordan.jordanfitnessapp;

import android.content.DialogInterface;
import android.content.Intent;
import android.jordan.com.userinfomodule.UserInfoManager;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

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
    private static final String LOG_TAG = "LoginActivity";

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
                    validateLoginInfo();
                } else { //we're in create account mode
                    Log.i(LOG_TAG, "continue button clicked, attempting to create account");
                    createNewAccount();
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
        UserInfoManager.getInstance().loadLoginInfo(this);
    }


    private void validateLoginInfo(){
        if(UserInfoManager.getInstance().isLoginInfoValid(this,usernameEditText.getText().toString(),passwordEditText.getText().toString())){
            login();
        } else {
            resetFields();
        }
    }

    private void createNewAccount(){
        String newUserName = usernameEditText.getText().toString();
        String newPassword = passwordEditText.getText().toString();

        boolean accountCreatedSuccessfully = UserInfoManager.getInstance().createNewUserAccount(this,newUserName,newPassword);
        if(accountCreatedSuccessfully){
            //proceed to log the user into the account
            login();
        } else {
            resetFields();
        }
    }

    private void login(){
        resetFields();
        Log.d(LOG_TAG,"Credentials correct, logging in user");
        Intent mainIntent = new Intent(this, MainActivity.class);
        //mainIntent.putExtra("key", value); //Optional parameters
        this.startActivity(mainIntent);
        Toast.makeText(this, getString(R.string.successfully_logging_in), Toast.LENGTH_LONG).show();
    }

    private void resetFields(){
        usernameEditText.setText("");
        passwordEditText.setText("");
    }

    @Override
    public void onBackPressed() {
        new AlertDialog.Builder(this)
                .setTitle("Quit Application")
                .setMessage("Are you sure you want to quit the application?")
                .setPositiveButton(android.R.string.yes, new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int which) {
                        System.exit(1);
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
