package com.amos.server.settingscreen;

import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;

import com.amos.server.R;
/*
this Activity only holds the Fragment which inflates the preferences
 */
public class SettingsActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_settings);
    }
    
}
