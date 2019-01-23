package com.amos.flyinn;

import android.graphics.Point;
import android.os.Bundle;
import android.support.design.widget.FloatingActionButton;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;

import com.amos.flyinn.summoner.Demo;

public class ADBActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_adb);
        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        FloatingActionButton fab = findViewById(R.id.fab);
        fab.setOnClickListener(view -> {
            try {
                Point p = new Point();
                getWindowManager().getDefaultDisplay().getRealSize(p);
                Demo.start(getApplicationContext(), p);

            } catch (Exception e) {
                e.printStackTrace();
            }
        });
    }
}
