package com.amos.flyinn.nearbyservice;

import android.annotation.TargetApi;
import android.app.Activity;
import android.app.IntentService;
import android.app.Notification;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.app.TaskStackBuilder;
import android.content.Context;
import android.content.Intent;
import android.os.Build;
import android.os.Handler;
import android.support.annotation.Nullable;
import android.support.v4.app.NotificationCompat;
import android.util.Log;


import com.amos.flyinn.ConnectionSetupActivity;
import com.amos.flyinn.R;
import com.amos.flyinn.ShowCodeActivity;

import java.util.Timer;
import java.util.TimerTask;
import java.util.concurrent.ThreadLocalRandom;

/**
 * Manage nearby connections with a server.
 * <p>
 * After starting this service. This will actively advertise
 * the current device for nearby connections. A server can then
 * initiate a connection over nearby.
 */
public class NearbyService extends IntentService {

    public static final String TAG = NearbyService.class.getPackage().getName();

    public static final String ACTION_START = "nearby_start";
    public static final String ACTION_STOP = "nearby_stop";

    private NearbyServer server;

    private static final int FOREGROUND_ID = 1;
    private static final int NOTIFY_ID = 2;
    private static final String CHANNEL_ID = "flyinn_nearby";

    private String nearbyCode = "";

    /**
     * Define the serviceState of our service.
     */
    private NearbyState serviceState = NearbyState.STOPPED;

    public NearbyService() {
        super("NearbyService");
    }

    @Override
    public int onStartCommand(@Nullable Intent intent, int flags, int startId) {
        Log.d(TAG, "Creating channel");
        createChannel();
        return super.onStartCommand(intent, flags, startId);
    }

    /**
     * Create a sticky notification that won't go away.
     * @param message String message shown in the notification.
     * @param target Optional target intent to switch to after tapping the notification.
     * @return
     */
    private Notification buildForegroundNotification(String message, @Nullable Intent target) {
        NotificationCompat.Builder b =
                new NotificationCompat.Builder(this, CHANNEL_ID);

        b.setOngoing(true)
                .setContentTitle(String.format("Nearby service %s", nearbyCode))
                .setContentText(message)
                .setSmallIcon(android.R.drawable.stat_notify_sync);

        if (target != null) {
            TaskStackBuilder stackBuilder = TaskStackBuilder.create(this);
            stackBuilder.addNextIntentWithParentStack(target);
            PendingIntent pendingIntent = stackBuilder.getPendingIntent(0, PendingIntent.FLAG_UPDATE_CURRENT);
            b.setContentIntent(pendingIntent);
        }

        return (b.build());
    }

    private void raiseNotification(Notification notification) {
        NotificationManager mgr=
                (NotificationManager)getSystemService(NOTIFICATION_SERVICE);
        startForeground(FOREGROUND_ID, buildForegroundNotification("Nearby Action running", null));
        mgr.notify(NOTIFY_ID, notification);
    }

    /**
     * Create or update shown notification.
     * @param message
     */
    public void notify(String message) {
        Intent intent = null;
        switch (serviceState) {
            case STOPPED:
                intent = new Intent(this, ShowCodeActivity.class);
                intent.putExtra("code", getNearbyCode());
                break;
            case ADVERTISING:
                intent = new Intent(this, ShowCodeActivity.class);
                intent.putExtra("code", getNearbyCode());
                break;
            case CONNECTING:
                intent = new Intent(this, ConnectionSetupActivity.class);
                intent.putExtra("code", getNearbyCode());
                break;
            case CONNECTED:
                intent = new Intent(this, ConnectionSetupActivity.class);
                intent.putExtra("code", getNearbyCode());
                break;
        }
        raiseNotification(buildForegroundNotification(message, intent));
    }

    /**
     * Create an Intent to control NearbyService.
     *
     * @param state   Defined serviceState for the application as enum.
     * @param context
     * @return Intent containing desired application serviceState.
     */
    public static Intent createNearbyIntent(String state, Context context) {
        Intent intent = new Intent(context, NearbyService.class);
        intent.setAction(state);
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
     * Start advertising Android nearby.
     */
    public void start() {
        if (serviceState == NearbyState.STOPPED) {
            Log.d(TAG, "Starting NearbyService");
            if (server == null) {
                try {
                    server = new NearbyServer(this);
                    serviceState = NearbyState.ADVERTISING;
                    notify("Start advertising nearby service");
                    server.start();
                } catch (SecurityException error) {
                    notify("Insufficient permissions");
                }
            }
        } else {
            Log.d(TAG, "NearbyService already started");
        }
    }

    /**
     * Stop advertising Android nearby.
     */
    public void stop() {
        if (serviceState != NearbyState.STOPPED) {
            Log.d(TAG, "Stopping NearbyService");
            serviceState = NearbyState.STOPPED;
            notify("Stopping nearby advertising");
            server.stop();
        } else {
            Log.d(TAG, "NearbyService already stopped");
        }
    }

    /**
     * Create a notification channel.
     *
     * Notifications are organized into different channels to theoretically enable the user to
     * individually set what they want to be informed about.
     *
     * This is required since Android 8.
     */
    private void createChannel() {
        NotificationManager mgr=
                (NotificationManager)getSystemService(NOTIFICATION_SERVICE);
        if (Build.VERSION.SDK_INT>=Build.VERSION_CODES.O &&
                mgr.getNotificationChannel(CHANNEL_ID)==null) {

            NotificationChannel c=new NotificationChannel(CHANNEL_ID,
                    "flyinn_channel", NotificationManager.IMPORTANCE_DEFAULT);

            c.enableLights(true);
            c.setLightColor(0xFFFF0000);

            mgr.createNotificationChannel(c);
        }
    }

    /**
     * Handle intents for initiation of advertising and connection shutdown.
     *
     * @param intent An intent with a custom Extra field called action.
     */
    @Override
    protected void onHandleIntent(@Nullable Intent intent) {
        Log.d(TAG, "Handling intent now");
        try {
            String code = intent.getStringExtra("code");
            setNearbyCode(code);
            Log.d(TAG, String.format("Setting code to %s", code));
        } catch (NullPointerException err) {
            Log.d(TAG, "Could not get code from intent.");
        }

        switch (intent.getAction()) {
            case ACTION_START:
                start();
                break;
            case ACTION_STOP:
                stop();
                break;
            default:
                Log.d(TAG, "Unknown intent received. Will do nothing");
                break;
        }
    }

    public void setNearbyCode(String code) {
        nearbyCode = code;
    }

    public String getNearbyCode() {
        return nearbyCode;
    }
}
