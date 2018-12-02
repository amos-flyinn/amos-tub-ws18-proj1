package com.amos.server;

import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v7.app.AppCompatActivity;
import android.widget.ProgressBar;
import android.widget.TextView;

public class ConnectionSetupServerActivity extends AppCompatActivity {

    private ProgressBar infiniteBar;
    private TextView progressText;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_connection_setup);
        infiniteBar = (ProgressBar) findViewById(R.id.infiniteBar);
        progressText = (TextView) findViewById(R.id.progressText);
        
    }

}
