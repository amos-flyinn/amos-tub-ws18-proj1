package com.amos.server;

import android.app.NotificationManager;
import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.os.Binder;
import android.os.IBinder;
import android.util.Log;

/**
 * Background service to kill notifications when the app is closed from the overview window
 */
public class KillNotificationService extends Service {

    /**
     * Binder with a service as class variable
     */
    public class KillBinder extends Binder {

        public final Service service;

        public KillBinder (Service service) {
            this.service = service;
        }

    }

    private static final String TAG = "KillNotificationServerService";

    private final IBinder myBinder = new KillBinder(this);

    /**
     * Returns a KillBinder with this service as its instance's service
     * @param intent
     * @return
     */
    @Override
    public IBinder onBind(Intent intent) {
        return myBinder;
    }

    /**
     * Make sure the service is sticky
     * @param intent
     * @param flags
     * @param startId
     * @return
     */
    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        Log.i(TAG, "Kill notification service running in background.");
        return Service.START_STICKY;
    }

    /**
     * Remove notifications when user closes app externally (e.g. from task bar)
     * @param rootIntent
     */
    @Override
    public void onTaskRemoved(Intent rootIntent) {
        ((NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE)).cancelAll();
        Log.i(TAG, "Task was removed, cleaning up notifications and ending service.");

        super.onTaskRemoved(rootIntent);
        stopSelf();
    }
}