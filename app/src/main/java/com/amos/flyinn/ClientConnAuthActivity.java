package com.amos.flyinn;

import android.Manifest;
import android.app.Activity;
import android.content.Context;
import android.content.pm.PackageManager;
import android.os.Build;
import android.os.Bundle;
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

/**
 * Handles the client-side connection for FlyInn authentication.
 * Checks permissions and allows all incoming connection requests.
 */
public abstract class ClientConnAuthActivity extends Activity {

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

    private ConnectionsClient connectionsClient;

    private String clientName;
    private String serverID;
    private String serverName;

    private Toast mToast;
    Handler handler = new Handler();

    /** Tag for logging purposes. */
    private static final String CONN_AUTH_TAG = "ClientConnAuth";


    /**
     * Obtain data from server and data transfer information via this handle.
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
                @Override
                public void onConnectionInitiated(String endpointId, ConnectionInfo connectionInfo) {
                    Log.i(CONN_AUTH_TAG, "Connection initiated by " + endpointId);
                    serverName = connectionInfo.getEndpointName();
                    connectionsClient.acceptConnection(endpointId, payloadCallback);
                }

                @Override
                public void onConnectionResult(String endpointId, ConnectionResolution result) {
                    switch (result.getStatus().getStatusCode()) {

                        case ConnectionsStatusCodes.STATUS_OK:
                            // successful connection with server
                            Log.i(CONN_AUTH_TAG, "Connected with " + endpointId);
                            mToast.setText(R.string.nearby_connection_success);
                            mToast.show();
                            connectionsClient.stopAdvertising();
                            serverID = endpointId;
                            break;

                        case ConnectionsStatusCodes.STATUS_CONNECTION_REJECTED:
                            // connection was rejected by one side (or both)
                            Log.i(CONN_AUTH_TAG, "Connection rejected with " + endpointId);
                            mToast.setText(R.string.nearby_connection_rejected);
                            mToast.show();
                            clearServerData();
                            break;

                        case ConnectionsStatusCodes.STATUS_ERROR:
                            // connection was lost
                            Log.w(CONN_AUTH_TAG, "Connection lost: " + endpointId);
                            mToast.setText(R.string.nearby_connection_error);
                            mToast.show();
                            clearServerData();
                            break;

                        default:
                            // unknown status code. we shouldn't be here
                            Log.e(CONN_AUTH_TAG, "Unknown error when attempting to connect with "
                                    + endpointId);
                            mToast.setText(R.string.nearby_connection_error);
                            mToast.show();
                            clearServerData();
                    }
                }

                @Override
                public void onDisconnected(String endpointId) {
                    // disconnected from server
                    Log.i(CONN_AUTH_TAG, "Disconnected from " + endpointId);
                    mToast.setText(R.string.nearby_disconnected);
                    mToast.show();

                    // better be safe
                    clearServerData();
                    connectionsClient.stopAdvertising();
                    connectionsClient.stopAllEndpoints();

                    // display toast for 2s, then finish
                    // TODO perhaps recreate instead?
                    handler.postDelayed(() -> finish(), 2000);
                }
            };


    /**
     * Returns the client's name (this endpoint).
     *
     * @return The client's name.
     */
    protected String getClient() { return clientName; }

    /**
     * Returns information about the server we are currently connected to.
     *
     * @return 2-entry array, the first entry is the ID of the server, the second its name.
     */
    protected String[] getServer() { return new String[]{serverID, serverName}; }


    /**
     * Initialises nearby connectionsClient and Toast instances,
     * checks permissions and calls startAdvertising().
     *
     * @param savedInstanceState
     */
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        clientName = generateName();

        if (!hasPermissions(this, REQUIRED_PERMISSIONS) &&
                Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            requestPermissions(REQUIRED_PERMISSIONS, REQUEST_CODE_REQUIRED_PERMISSIONS);
        } else {
            Log.w(CONN_AUTH_TAG, "Could not check permissions due to version");
        }


        Log.i(CONN_AUTH_TAG, "Current name is: " + clientName);
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
            Log.w(CONN_AUTH_TAG, "Could not check permissions due to version");
        }
    }

    /**
     * Clears server data and stops all advertising and connections from this client
     * before calling super.onDestroy().
     */
    @Override
    protected void onDestroy() {
        connectionsClient.stopAdvertising();
        connectionsClient.stopAllEndpoints();
        clearServerData();
        super.onDestroy();
    }


    /**
     * Broadcast our presence using Nearby Connection so FlyInn servers can find us.
     * Resets server data first.
     */
    private void startAdvertising() {
        clearServerData();

        AdvertisingOptions advertisingOptions =
                new AdvertisingOptions.Builder().setStrategy(STRATEGY).build();

        connectionsClient.startAdvertising(clientName, "com.amos.flyinn",
                connectionLifecycleCallback, advertisingOptions)
                .addOnSuccessListener( (Void unused) -> {
                    // started advertising successfully
                    Log.i(CONN_AUTH_TAG, "Started advertising " + clientName);
                })
                .addOnFailureListener( (Exception e) -> {
                    // unable to advertise
                    Log.e(CONN_AUTH_TAG, "Unable to start advertising " + clientName);
                    mToast.setText(R.string.nearby_advertising_error);
                    mToast.show();

                    // display toast for 2s, then finish
                    handler.postDelayed(() -> finish(), 2000);
                });
    }

    /**
     * Resets server ID and name to null.
     */
    private void clearServerData() {
        serverID = null;
        serverName = null;
    }

    /**
     * Determines whether we (the FlyInn client app) have the necessary permissions to run nearby.
     *
     * @param context Checks the permissions against this context/application environment
     * @param permissions The permissions to be checked
     *
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
     *
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
                Log.w(CONN_AUTH_TAG, "Permissions necessary for connections were not granted.");
                mToast.setText(R.string.nearby_missing_permissions);
                mToast.show();
                finish();
            }
        }
        recreate();
    }

    /**
     * Returns the name of this client, which is set in onCreate().
     */
    protected abstract String generateName();
}
