package com.amos.server;

import android.Manifest;
import android.app.Activity;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.ServiceConnection;
import android.content.pm.PackageManager;
import android.os.Build;
import android.os.Bundle;
import android.os.IBinder;
import android.support.annotation.CallSuper;
import android.support.annotation.NonNull;
import android.support.v4.app.NotificationCompat;
import android.support.v4.content.ContextCompat;
import android.util.Log;
import android.view.KeyEvent;
import android.view.inputmethod.EditorInfo;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import com.amos.server.nearby.ServerConnection;

public class ConnectToClientActivity extends Activity {

    private static final String[] REQUIRED_PERMISSIONS =
            new String[] {
                    Manifest.permission.BLUETOOTH,
                    Manifest.permission.BLUETOOTH_ADMIN,
                    Manifest.permission.ACCESS_WIFI_STATE,
                    Manifest.permission.CHANGE_WIFI_STATE,
                    Manifest.permission.ACCESS_COARSE_LOCATION
            };

    private static final int REQUEST_CODE_REQUIRED_PERMISSIONS = 1;

    private static final int NOTIFY_ID = 2;
    private static final String CHANNEL_ID = "FlyInn server nearby";

    private Toast mToast;

    private ServerConnection connection = ServerConnection.getInstance();

    /** Tag for logging purposes. */
    private static final String TAG = "ServerConnectToClient";

    private ServiceConnection myConnection = new ServiceConnection() {
        public void onServiceConnected (ComponentName className, IBinder binder) {
            ((KillNotificationService.KillBinder) binder).service.startService(
                    new Intent(ConnectToClientActivity.this, KillNotificationService.class));
        }
        public void onServiceDisconnected(ComponentName className) {}
    };

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        bindService(new Intent(ConnectToClientActivity.this,
                KillNotificationService.class), myConnection, Context.BIND_AUTO_CREATE);

        setContentView(R.layout.activity_connect_to_client);
        Thread.setDefaultUncaughtExceptionHandler(new ExceptionHandler(this));
        EditText text = findViewById(R.id.connect_editText);

        createChannel();
        notification(getString(R.string.notification_initialising));
        mToast = Toast.makeText(this, "", Toast.LENGTH_LONG);
        connection.setActivity(this);

        checkPermissions();
        // Ensure survival for life of entire application
        connection.init(getApplicationContext());
        connection.discover();
        text.setOnEditorActionListener(
                (TextView v, int actionId, KeyEvent event) -> {
                    if (actionId == EditorInfo.IME_ACTION_DONE) {
                        String name = v.getText().toString(); // Get the String
                        toConnectionSetup(name);
                        return true;
                    }
                    return false;
                });
    }

    /**
     * Switch to connection setup activity
     */
    private void toConnectionSetup(String name) {
        Intent intent = new Intent(this, ConnectionSetupServerActivity.class);
        intent.setAction("connect");
        intent.putExtra("name", name);
        startActivity(intent);
    }

    /**
     * Checks needed permissions for nearby connection
     */
    @Override
    protected void onStart() {
        super.onStart();

        // user may have changed permissions
        checkPermissions();
    }

    /**
     * Stops all connection discovery and connections from this client
     * before calling super.onDestroy()
     */
    @Override
    protected void onDestroy() {
        connection.abort();

        try {
            unbindService(myConnection);
        } catch (Exception e) {
            Log.d(TAG, e.toString());
            Log.i(TAG, "KillNotificationService was already unbound.");
        }

        ((NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE)).cancelAll();
        super.onDestroy();
    }

    /**
     * Checks whether FlyInn has the required permissions
     */
    private void checkPermissions() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            if (!hasPermissions(this)) {
                requestPermissions(REQUIRED_PERMISSIONS, REQUEST_CODE_REQUIRED_PERMISSIONS);
            } else {
                Log.d(TAG, "Permissions are ok.");
            }
        } else {
            Log.w(TAG, "Could not check permissions due to version");
            toast(getString(R.string.nearby_permissions_version));
            closeApp();
        }
    }

    /**
     * Determines whether the FlyInn server app has the necessary permissions to run nearby.
     * @param context Checks the permissions against this context/application environment
     * @return True if the app was granted all the permissions, false otherwise
     */
    private static boolean hasPermissions(Context context) {
        for (String permission : ConnectToClientActivity.REQUIRED_PERMISSIONS) {
            if (ContextCompat.checkSelfPermission(context, permission)
                    != PackageManager.PERMISSION_GRANTED) {
                return false;
            }
        }
        return true;
    }

    /**
     * Handles user acceptance (or denial) of our permission request.
     * @param requestCode The request code passed in requestPermissions()
     * @param permissions Permissions that must be granted to run nearby connections
     * @param grantResults Results of granting permissions
     */
    @CallSuper
    @Override
    public void onRequestPermissionsResult(
            int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);

        if (requestCode != REQUEST_CODE_REQUIRED_PERMISSIONS) {
            return;
        }

        for (int grantResult : grantResults) {
            if (grantResult == PackageManager.PERMISSION_DENIED) {
                Log.w(TAG, "Permissions necessary for " +
                        "Nearby Connection were not granted.");
                toast(getString(R.string.nearby_missing_permissions));
                closeApp();
            }
        }
        recreate();
    }

    /**
     * Shows the message as toast (length: long).
     * Toasts will replace each other, and not overlap/stack.
     *
     * @param message String message which should be shown as a Toast
     */
    public void toast(String message) {
        mToast.setText(message);
        mToast.show();
    }

    /**
     * Simply creates a persistent notification with default priority (no pop-up).
     *
     * @param message String message which should be shown as a notification
     */
    public void notification(String message) {
        Log.d(TAG, "Notification status: " + message);
        NotificationCompat.Builder b =
                new NotificationCompat.Builder(this, CHANNEL_ID);

        b.setOngoing(true) // persistent notification
                .setPriority(NotificationCompat.PRIORITY_MAX)
                .setContentTitle(getString(R.string.notification_name))
                .setContentText(message)
                .setSmallIcon(android.R.drawable.stat_notify_sync);

        // open FlyInn when clicking on notification
        Intent intent = new Intent(this, ConnectToClientActivity.class);
        intent.setAction(Intent.ACTION_MAIN);
        intent.addCategory(Intent.CATEGORY_LAUNCHER);

        PendingIntent pendingIntent = PendingIntent.getActivity(this, 0,
                intent, 0);
        b.setContentIntent(pendingIntent);

        NotificationManager mgr = (NotificationManager) getSystemService(NOTIFICATION_SERVICE);
        mgr.notify(NOTIFY_ID, b.build());
    }

    private void createChannel() {
        NotificationManager mgr =
                (NotificationManager) getSystemService(NOTIFICATION_SERVICE);
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O &&
                mgr.getNotificationChannel(CHANNEL_ID) == null) {

            NotificationChannel c = new NotificationChannel(CHANNEL_ID,
                    "flyinn_channel", NotificationManager.IMPORTANCE_LOW);

            c.enableLights(true);
            c.setLightColor(0xFFFF0000);
            c.enableVibration(false);

            mgr.createNotificationChannel(c);
        }
    }

    /**
     * Closes the app (kills all activities)
     */
    public void closeApp() {
        Log.d(TAG, "Closing server via closeApp function.");
        ((NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE)).cancelAll();

        this.finishAffinity();
        finishAndRemoveTask();
    }

    /**
     * Finishes all activities and then restarts the app
     */
    public void restartApp() {
        Log.d(TAG, "Restarting server via restartApp function.");
        Intent i = getBaseContext().getPackageManager()
                .getLaunchIntentForPackage( getBaseContext().getPackageName() );
        i.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
        startActivity(i);
    }
}
