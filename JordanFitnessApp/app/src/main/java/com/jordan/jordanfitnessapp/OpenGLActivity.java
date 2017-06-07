package com.jordan.jordanfitnessapp;

import android.jordan.com.openglmodule.GLView;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;

/**
 * Created by Scott on 6/7/2017.
 */

public class OpenGLActivity extends AppCompatActivity {
    GLView mView;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_opengl);
        mView = new GLView(getApplication());
        setContentView(mView);
    }
}
