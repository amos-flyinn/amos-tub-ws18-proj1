package com.amos.server;

import android.Manifest;
import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.pm.PackageManager;
import android.os.Build;
import android.os.Bundle;
import android.app.Activity;
import android.os.Handler;
import android.support.annotation.CallSuper;
import android.support.annotation.NonNull;
import android.support.v4.content.ContextCompat;
import android.util.Log;
import android.widget.Toast;

import com.google.android.gms.nearby.Nearby;
import com.google.android.gms.nearby.connection.AdvertisingOptions;
import com.google.android.gms.nearby.connection.ConnectionInfo;
import com.google.android.gms.nearby.connection.ConnectionLifecycleCallback;
import com.google.android.gms.nearby.connection.ConnectionResolution;
import com.google.android.gms.nearby.connection.ConnectionsClient;
import com.google.android.gms.nearby.connection.ConnectionsStatusCodes;
import com.google.android.gms.nearby.connection.Payload;
import com.google.android.gms.nearby.connection.PayloadCallback;
import com.google.android.gms.nearby.connection.PayloadTransferUpdate;
import com.google.android.gms.nearby.connection.Strategy;

import java.security.SecureRandom;

/**
 * Activity that handles connection to clients via nearby connection.
 * Includes permission handling and simple token authentication.
 */
public class NearbyServerActivity extends Activity {

    /** Permissions required for Nearby Connection */
    private static final String[] REQUIRED_PERMISSIONS =
            new String[] {
                    Manifest.permission.BLUETOOTH,
                    Manifest.permission.BLUETOOTH_ADMIN,
                    Manifest.permission.ACCESS_WIFI_STATE,
                    Manifest.permission.CHANGE_WIFI_STATE,
            };

    private static final int REQUEST_CODE_REQUIRED_PERMISSIONS = 1;

    /** 1-to-1 since a device will be connected to only one other device at most. */
    private static final Strategy STRATEGY = Strategy.P2P_POINT_TO_POINT;

    /** Connection manager for the connection to FlyInn clients.*/
    protected ConnectionsClient connectionsClient;

    private final String serverName = generateName(5);
    private String clientID;
    private String clientName;

    final Handler handler = new Handler();

    /** Toast to publish user notifications */
    private Toast mToast;

    /** Tag for logging purposes. */
    private static final String NEARBY_TAG = "ServerNearbyConnection";


    /**
     * Obtain data from clientID/clientName and data transfer information via this handle.
     */
    private final PayloadCallback payloadCallback =
            new PayloadCallback() {
                @Override
                public void onPayloadReceived(String endpointId, Payload payload) {
                    // TODO
                }

                @Override
                public void onPayloadTransferUpdate(String endpointId, PayloadTransferUpdate update) {
                    // TODO
                }
            };

    /**
     * Callbacks for connections to other devices.
     * Includes token authentication and connection handling.
     */
    private final ConnectionLifecycleCallback connectionLifecycleCallback =
            new ConnectionLifecycleCallback() {
            };

    /**
     * Starts a nearby connectionsClient, checks permissions and calls startAdvertising().
     * @param savedInstanceState
     */
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_nearby_server);

        if (!hasPermissions(this, REQUIRED_PERMISSIONS) &&
                Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            requestPermissions(REQUIRED_PERMISSIONS, REQUEST_CODE_REQUIRED_PERMISSIONS);
        } else {
            Log.w(NEARBY_TAG, "Could not check permissions due to version");
        }

        connectionsClient = Nearby.getConnectionsClient(this);
        mToast = Toast.makeText(this, "", Toast.LENGTH_SHORT);

        startAdvertising();
    }

    /**
     * Checks whether the app has the required permissions to establish connections after the
     * super.onStart() call (the user may have changed permissions after starting the app).
     */
    @Override
    protected void onStart() {
        super.onStart();

        // user may have changed permissions
        if (!hasPermissions(this, REQUIRED_PERMISSIONS) &&
                Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            requestPermissions(REQUIRED_PERMISSIONS, REQUEST_CODE_REQUIRED_PERMISSIONS);
        } else {
            Log.w(NEARBY_TAG, "Could not check permissions due to version");
        }
    }

    /**
     * Clears client data and stops all advertising and connections from this server
     * before calling super.onDestroy().
     */
    @Override
    protected void onDestroy() {
        connectionsClient.stopAdvertising();
        connectionsClient.stopAllEndpoints();
        clearClientData();
        super.onDestroy();
    }


    /**
     * Broadcast our presence using Nearby Connection so FlyInn users can find us.
     * Resets clientID and clientName first.
     */
    private void startAdvertising() {
        clearClientData();

        AdvertisingOptions advertisingOptions =
                new AdvertisingOptions.Builder().setStrategy(STRATEGY).build();

        connectionsClient.startAdvertising(serverName, "com.amos.flyinn",
                connectionLifecycleCallback, advertisingOptions)
                .addOnSuccessListener( (Void unused) -> {
                    // started advertising successfully
                    Log.i(NEARBY_TAG, "Started advertising " + serverName);
                    mToast.setText(R.string.nearby_advertising_success);
                    mToast.show();
                })
                .addOnFailureListener( (Exception e) -> {
                    // unable to advertise
                    Log.e(NEARBY_TAG, "Unable to start advertising " + serverName);
                    mToast.setText(R.string.nearby_advertising_error);
                    mToast.show();
                    finish();
                });
    }

    /**
     * Resets client ID and client name to null.
     */
    private void clearClientData() {
        clientID = null;
        clientName = null;
    }

    /**
     * Determines whether the FlyInn server app has the necessary permissions to run nearby.
     * @param context Checks the permissions against this context/application environment
     * @param permissions The permissions to be checked
     * @return True if the app was granted all the permissions, false otherwise
     */
    private static boolean hasPermissions(Context context, String[] permissions) {
        for (String permission : permissions) {
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
                Log.w(NEARBY_TAG, "Permissions necessary for connections were not granted.");
                mToast.setText(R.string.nearby_missing_permissions);
                mToast.show();
                finish();
            }
        }
        recreate();
    }

    /**
     * Generates a name for the server.
     * @return The server name, consisting of the build model + a random string
     */
    // TODO Define better name system?
    private String generateName(int appendixLength){
        String AB = "0123456789ABCDEFGHIJKLMNOPQRSTUVWXYZabcdefghijklmnopqrstuvwxyz";
        SecureRandom rnd = new SecureRandom();

        StringBuilder sb = new StringBuilder(appendixLength);
        for (int i = 0; i < appendixLength; i++) {
            sb.append(AB.charAt(rnd.nextInt(AB.length())));
        }

        String name = Build.MODEL + "_" + sb.toString();
        Log.i(NEARBY_TAG, "Current name is: " + name);
        return name;
    }
}
