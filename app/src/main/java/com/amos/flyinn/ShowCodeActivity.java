package com.amos.flyinn;

import android.app.Notification;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.net.wifi.p2p.WifiP2pDevice;
import android.os.Build;
import android.os.Bundle;
import android.support.v4.app.NotificationCompat;
import android.support.v7.app.AppCompatActivity;
import android.widget.TextView;

import com.amos.flyinn.nearbyservice.NearbyService;
import com.amos.flyinn.nearbyservice.NearbyState;
import com.amos.flyinn.wificonnector.WifiConnectorBase;

import java.util.List;
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

        // Start NearbyService
        Context ctx = getApplicationContext();
        ctx.startService(NearbyService.createNearbyIntent(NearbyState.START, ctx));

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            startForegroundService(new Intent(this, NearbyService.class));
        } else {
            startService(new Intent(this, NearbyService.class));
        }
    }

    @Override
    public void onResume() {
        super.onResume();
        nameNum = String.valueOf(ThreadLocalRandom.current().nextInt(1000, 9998 + 1));
        display.setText(nameNum);
    }
}
