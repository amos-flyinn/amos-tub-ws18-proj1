package com.amos.flyinn;

import android.os.Bundle;
import android.support.design.widget.FloatingActionButton;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.View;

import com.amos.flyinn.summoner.Demo;
import com.amos.flyinn.wifimanager.WifiManager;

public class ADBActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_adb);
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        FloatingActionButton fab = (FloatingActionButton) findViewById(R.id.fab);
        fab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                try {
                    Demo.start(getApplicationContext(), WifiManager.getInstance().getWifiReceiverP2P().getHostAddr());
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        });
    }
}
