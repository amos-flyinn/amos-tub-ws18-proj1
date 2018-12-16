package com.amos.flyinn.nearbyservice;

import android.annotation.TargetApi;
import android.app.Activity;
import android.app.IntentService;
import android.app.Notification;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.content.Context;
import android.content.Intent;
import android.os.Build;
import android.os.Handler;
import android.support.annotation.Nullable;
import android.support.v4.app.NotificationCompat;
import android.util.Log;


import com.amos.flyinn.R;

import java.util.Timer;
import java.util.TimerTask;

/**
 * Manage nearby connections with a server.
 * <p>
 * After starting this service. This will actively advertise
 * the current device for nearby connections. A server can then
 * initiate a connection over nearby.
 */
public class NearbyService extends IntentService {

    public static final String TAG = NearbyService.class.getPackage().getName();
    private NearbyServer server;

    private static final int FOREGROUND_ID = 1;
    private static final int NOTIFY_ID = 2;
    private static final String CHANNEL_ID = "flyinn_nearby";

    /**
     * Define the serviceState of our service.
     */
    private NearbyState serviceState = NearbyState.STOPPED;
    private Handler handler = null;

    public NearbyService() {
        super("NearbyService");
    }

    private Notification buildForegroundNotification(String filename) {
        NotificationCompat.Builder b =
                new NotificationCompat.Builder(this, CHANNEL_ID);

        b.setOngoing(true)
                .setContentTitle("Ongoing")
                .setContentText(filename)
                .setSmallIcon(android.R.drawable.stat_sys_download);

        return (b.build());
    }

    private void raiseNotification(Notification notification) {
        NotificationManager mgr=
                (NotificationManager)getSystemService(NOTIFICATION_SERVICE);
        mgr.notify(NOTIFY_ID, notification);
    }

    /**
     * Create an Intent to control NearbyService.
     *
     * @param state   Defined serviceState for the application as enum.
     * @param context
     * @return Intent containing desired application serviceState.
     */
    public static Intent createNearbyIntent(NearbyState state, Context context) {
        Intent intent = new Intent(context, NearbyService.class);
        intent.putExtra("action", state);
        return intent;
    }

    public void handleResponse(boolean error, String message) {
        if (error) {
            Log.d(TAG, String.format("Error received: %s", message));
        } else {
            Log.d(TAG, String.format("Status update: %s", message));
        }
    }

    /**
     * Dummy timer for testing purposes
     */
    private void startTimer() {
        Timer timer = new Timer();
        timer.schedule(new TimerTask() {
            @Override
            public void run() {
                Log.i(TAG, "in timer");
            }
        }, 1000, 1000);
    }

    /**
     * Handle intents for initiation of advertising and connection shutdown.
     *
     * @param intent An intent with a custom Extra field called action.
     */
    @Override
    protected void onHandleIntent(@Nullable Intent intent) {
        NotificationManager mgr=
                (NotificationManager)getSystemService(NOTIFICATION_SERVICE);

        if (Build.VERSION.SDK_INT>=Build.VERSION_CODES.O &&
                mgr.getNotificationChannel(CHANNEL_ID)==null) {

            NotificationChannel c=new NotificationChannel(CHANNEL_ID,
                    "flyinnchannel", NotificationManager.IMPORTANCE_DEFAULT);

            c.enableLights(true);
            c.setLightColor(0xFFFF0000);

            mgr.createNotificationChannel(c);
        }
        startForeground(FOREGROUND_ID,
                buildForegroundNotification("Test"));
        raiseNotification(buildForegroundNotification("test"));
        startTimer();
        Log.d(TAG, "Handling intent now");
        NearbyState action;
        try {
            action = (NearbyState) intent.getSerializableExtra("action");
            if (action == null) {
                action = NearbyState.UNKNOWN;
            }
        } catch (NullPointerException err) {
            action = NearbyState.UNKNOWN;
        }
        raiseNotification(buildForegroundNotification("test"));
        startTimer();
        switch (action) {
            case START:
                if (serviceState == NearbyState.STOPPED) {
                    Log.d(TAG, "Starting NearbyService");
                    if (server == null) {
                        server = new NearbyServer("testdevice", this);
                    }
                    server.start();
                    serviceState = NearbyState.STARTED;
                } else {
                    Log.d(TAG, "NearbyService already started");
                }
                break;
            case STOP:
                if (serviceState == NearbyState.STARTED) {
                    Log.d(TAG, "Stopping NearbyService");
                    server.stop();
                    serviceState = NearbyState.STOPPED;
                } else {
                    Log.d(TAG, "NearbyService already stopped");
                }
                break;
            case UNKNOWN:
                Log.d(TAG, "Unknown intent received. Will do nothing");
                break;
        }
    }
}
