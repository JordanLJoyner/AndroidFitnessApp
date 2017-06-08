package com.jordan.jordanfitnessapp;

import android.graphics.Color;
import android.jordan.com.openglmodule.GLView;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.TextView;

/**
 * Created by Jordan on 6/7/2017.
 */

public class OpenGLActivity extends AppCompatActivity {
    GLView mView;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_opengl);
        //RelativeLayout glLayout = (RelativeLayout) findViewById(R.id.opengl_activity_relative_layout);
        mView = new GLView(getApplication());
        setContentView(mView);
        //glLayout.addView(mView);

        // Creating a new TextView
        TextView tv = new TextView(this);
        tv.setText("Test");
        tv.setTextColor(Color.RED);
        RelativeLayout.LayoutParams rlp= new RelativeLayout.LayoutParams(RelativeLayout.LayoutParams.WRAP_CONTENT,
                RelativeLayout.LayoutParams.WRAP_CONTENT);
        rlp.addRule(RelativeLayout.ALIGN_TOP);
        addContentView(tv, rlp);
    }

    @Override protected void onPause() {
        super.onPause();
        mView.onPause();
    }

    @Override protected void onResume() {
        super.onResume();
        mView.onResume();
    }
}
