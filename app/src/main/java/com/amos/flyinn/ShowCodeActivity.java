package com.amos.flyinn;

import android.content.Intent;
import android.os.Build;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.widget.TextView;

import com.amos.flyinn.nearbyservice.NearbyService;
import com.amos.flyinn.nearbyservice.NearbyState;

import java.util.concurrent.ThreadLocalRandom;

/**
 * Initial activity showing code used for connection from remote display.
 */
public class ShowCodeActivity extends AppCompatActivity {
    private String nameNum = "1234";
    private TextView display;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_show_code);
        display = findViewById(R.id.textView2);

        Intent intent = NearbyService.createNearbyIntent(NearbyState.START, this);
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            startForegroundService(intent);
        } else {
            startService(intent);
        }
    }

    @Override
    public void onResume() {
        super.onResume();
        nameNum = String.valueOf(ThreadLocalRandom.current().nextInt(1000, 9998 + 1));
        display.setText(nameNum);
    }


    @Override
    protected void onDestroy() {
        Intent intent = NearbyService.createNearbyIntent(NearbyState.UNKNOWN, this);
        stopService(intent);
        super.onDestroy();
    }
}
