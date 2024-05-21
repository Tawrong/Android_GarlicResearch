package com.example.garlicapp;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;

import androidx.appcompat.app.AppCompatActivity;

public class IntroActivity extends AppCompatActivity {
    private static final String PREFS_NAME = "MyAppPrefs";
    Button button;
    private static final String OPEN_COUNT_KEY = "openCount";
    private final boolean showAlternativeLayout = false;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        int openCount = getOpenCount();
        openCount++;
        saveOpenCount(openCount);
        if(openCount <= 1){
            Intent intent = new Intent(IntroActivity.this, OnBoarding.class);
            intent.putExtra("email_extra_users", getIntent().getStringExtra("email_extra_users"));
            startActivity(intent);
        } else if (openCount > 1) {
            Intent intent = new Intent(IntroActivity.this, Main_page.class);
            intent.putExtra("email_extra_users", getIntent().getStringExtra("email_extra_users"));
            startActivity(intent);
        }
        Log.e("StartCount", String.valueOf(openCount));
        Log.e("Open", getIntent().getStringExtra("email_extra_users"));
        setContentView(R.layout.introduction_layout);
        button = findViewById(R.id.nextButton);

    }

    private int getOpenCount() {
        SharedPreferences prefs = this.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE);
        return prefs.getInt(OPEN_COUNT_KEY, 0);
    }

    private void saveOpenCount(int count) {
        SharedPreferences prefs = this.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE);
        SharedPreferences.Editor editor = prefs.edit();
        editor.putInt(OPEN_COUNT_KEY, count);
        editor.apply();
    }
}
