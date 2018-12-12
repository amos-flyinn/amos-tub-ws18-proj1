package com.amos.flyinn.nearbyservice;

import android.annotation.TargetApi;
import android.app.Activity;
import android.app.IntentService;
import android.app.Notification;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.os.Build;
import android.os.Handler;
import android.os.Message;
import android.support.v4.app.NotificationCompat;
import android.support.v4.app.NotificationManagerCompat;
import android.util.Log;


import com.amos.flyinn.ConnectionSetupActivity;
import com.amos.flyinn.R;
import com.amos.flyinn.ShowCodeActivity;

import javax.annotation.Nullable;

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

    private static final int FOREGROUND_ID = 10;

    /**
     * Define the serviceState of our service.
     */
    private NearbyState serviceState = NearbyState.STOPPED;
    private Handler handler = null;

    public NearbyService() {
        super("NearbyService");
    }

    /**
     * Source: https://stackoverflow.com/a/50634187
     * @param context
     * @param id
     * @param importance
     */
    @TargetApi(26)
    private static void prepareChannel(Context context, String id, int importance) {
        final String appName = context.getString(R.string.app_name);
        String description = "flyInn notification";
        final NotificationManager nm = (NotificationManager) context.getSystemService(Activity.NOTIFICATION_SERVICE);

        if(nm != null) {
            NotificationChannel nChannel = nm.getNotificationChannel(id);

            if (nChannel == null) {
                nChannel = new NotificationChannel(id, appName, importance);
                nChannel.setDescription(description);
                nm.createNotificationChannel(nChannel);
            }
        }
    }

    /**
     * Source: https://stackoverflow.com/a/50634187
     * @param context
     * @param channelId
     * @param importance
     * @return
     */
    public static NotificationCompat.Builder getNotificationBuilder(Context context, String channelId, int importance) {
        NotificationCompat.Builder builder;
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            prepareChannel(context, channelId, importance);
            builder = new NotificationCompat.Builder(context, channelId);
        } else {
            builder = new NotificationCompat.Builder(context);
        }
        return builder;
    }

    /**
     * Source: https://stackoverflow.com/a/50634187
     * @param text
     * @return
     */
    private Notification buildForegroundNotification(String text) {
        NotificationCompat.Builder b=getNotificationBuilder(this, "com.amos.flyinn.nearbyservice.notification.nearbyservice", NotificationManagerCompat.IMPORTANCE_LOW);
        b.setOngoing(true)
                .setContentTitle("Testing")
                .setContentText(text).setSmallIcon(android.R.drawable.stat_notify_sync);
        return(b.build());
    }

    public NearbyService(Handler handler) {
        this();
        this.handler = handler;
    }

    /**
     * Check that our components have all required permissions
     *
     * @return
     */
    public boolean hasPermissions() {
        return server.hasPermissions(this);
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

    /**
     * Create a system toast message
     *
     * @param message
     */
    protected void createToast(String message) {
        if (handler != null) {
            Message.obtain(handler, 0, message);
        } else {
            Log.d(TAG, String.format("No toast defined. Cannot send: %s", message));
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
        startForeground(FOREGROUND_ID, buildForegroundNotification("Hello"));
        NearbyState action;
        try {
            action = (NearbyState) intent.getSerializableExtra("action");
            if (action == null) {
                action = NearbyState.UNKNOWN;
            }
        } catch (NullPointerException err) {
            action = NearbyState.UNKNOWN;
        }
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
