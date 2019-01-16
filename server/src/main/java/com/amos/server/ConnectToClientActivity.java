package com.amos.server;

import android.Manifest;
import android.app.Activity;
import android.app.Notification;
import android.app.NotificationManager;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.os.Build;
import android.os.Bundle;
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
    private final String CHANNEL_ID = getString(R.string.notification_channel_id);

    private Toast mToast;

    private ServerConnection connection = ServerConnection.getInstance();

    /** Tag for logging purposes. */
    private static final String TAG = "ServerNearbyConnection";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_connect_to_client);
        EditText text = findViewById(R.id.connect_editText);

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
        super.onDestroy();
        connection.abort();
    }

    private void checkPermissions() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            if (!hasPermissions(this)) {
                requestPermissions(REQUIRED_PERMISSIONS, REQUEST_CODE_REQUIRED_PERMISSIONS);
            } else {
                Log.d(TAG, "Permissions are ok.");
            }
        } else {
            Log.e(TAG, "Could not check permissions due to version");
            toast(getString(R.string.nearby_wrong_version_permissions));
            // TODO finish/recreate (?)
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
                finish();
            }
        }
        recreate();
    }

    public void toast(String message) {
        mToast.setText(message);
        mToast.show();
    }

    public void notification(String message) {
        NotificationCompat.Builder b =
                new NotificationCompat.Builder(this, CHANNEL_ID);

        b.setOngoing(true) // persistent notification
                .setPriority(NotificationCompat.PRIORITY_DEFAULT)
                .setContentTitle("Nearby server")
                .setContentText(message)
                .setSmallIcon(android.R.drawable.stat_notify_sync);

        NotificationManager mgr = (NotificationManager) getSystemService(NOTIFICATION_SERVICE);
        mgr.notify(NOTIFY_ID, b.build());
    }
}
