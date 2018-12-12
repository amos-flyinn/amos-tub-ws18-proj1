package com.amos.flyinn.nearbyservice;

import android.Manifest;
import android.content.Context;
import android.content.pm.PackageManager;
import android.support.v4.content.ContextCompat;
import android.util.Log;

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
 * Handle creating a nearby service that will advertise itself and manage
 * incoming connections.
 */
class NearbyServer {
    public static final String TAG = NearbyServer.class.getPackage().getName();
    /**
     * Required permissions for Nearby connections
     */
    private static final String[] REQUIRED_PERMISSIONS =
            new String[]{
                    Manifest.permission.BLUETOOTH,
                    Manifest.permission.BLUETOOTH_ADMIN,
                    Manifest.permission.ACCESS_WIFI_STATE,
                    Manifest.permission.CHANGE_WIFI_STATE,
                    Manifest.permission.ACCESS_COARSE_LOCATION
            };
    private static final int REQUEST_CODE_REQUIRED_PERMISSIONS = 1;

    /**
     * 1-to-1 since a device will be connected to only one other device at most.
     */
    private static final Strategy STRATEGY = Strategy.P2P_POINT_TO_POINT;

    /**
     * Connection manager for the connection to FlyInn clients.
     */
    protected ConnectionsClient connectionsClient;
    private final String serverName;
    private String clientID;
    private String clientName;

    private NearbyService nearbyService;

    /**
     * Create new nearby server with the given name.
     *
     * @param name
     */
    public NearbyServer(String name, NearbyService service) {
        this.serverName = name;
        this.nearbyService = service;
        this.connectionsClient = Nearby.getConnectionsClient(this.nearbyService);
    }

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
     * Determines whether the FlyInn server app has the necessary permissions to run nearby.
     *
     * @param context Checks the permissions against this context/application environment
     * @return True if the app was granted all the permissions, false otherwise
     */
    public static boolean hasPermissions(Context context) {
        for (String permission : REQUIRED_PERMISSIONS) {
            if (ContextCompat.checkSelfPermission(context, permission)
                    != PackageManager.PERMISSION_GRANTED) {
                return false;
            }
        }
        return true;
    }

    /**
     * Broadcast our presence using Nearby Connection so FlyInn users can find us.
     * Resets clientID and clientName first.
     */
    public void start() {
        clearClientData();

        AdvertisingOptions advertisingOptions =
                new AdvertisingOptions.Builder().setStrategy(STRATEGY).build();

        connectionsClient.startAdvertising(serverName, TAG,
                connectionLifecycleCallback, advertisingOptions)
                .addOnSuccessListener((Void unused) -> {
                    Log.d(TAG, "Start advertising Android nearby");
                })
                .addOnFailureListener((Exception e) -> {
                    Log.d(TAG, "Error trying to advertise Android nearby");
                    nearbyService.handleResponse(true, e.toString());
                });
    }

    public void stop() {
    }

    /**
     * Callbacks for connections to other devices.
     * Includes token authentication and connection handling.
     */
    private final ConnectionLifecycleCallback connectionLifecycleCallback =
            new ConnectionLifecycleCallback() {
                @Override
                public void onConnectionInitiated(String endpointId, ConnectionInfo connectionInfo) {
                    Log.i(TAG, "Connection initiated by " + endpointId);
                    clientName = connectionInfo.getEndpointName();
                }

                @Override
                public void onConnectionResult(String endpointId, ConnectionResolution result) {
                    switch (result.getStatus().getStatusCode()) {
                        case ConnectionsStatusCodes.STATUS_OK:
                            // successful connection with client
                            Log.i(TAG, "Connected with " + endpointId);
                            connectionsClient.stopAdvertising();
                            clientID = endpointId;
                            break;

                        case ConnectionsStatusCodes.STATUS_CONNECTION_REJECTED:
                            // connection was rejected by one side (or both)
                            Log.i(TAG, "Connection rejected with " + endpointId);
                            clearClientData();
                            break;

                        case ConnectionsStatusCodes.STATUS_ERROR:
                            // connection was lost
                            Log.w(TAG, "Connection lost: " + endpointId);
                            clearClientData();
                            break;

                        default:
                            // unknown status code. we shouldn't be here
                            Log.e(TAG, "Unknown error when attempting to connect with "
                                    + endpointId);
                            clearClientData();
                    }
                }

                @Override
                public void onDisconnected(String endpointId) {
                    // disconnected from client
                    Log.i(TAG, "Disconnected from " + endpointId);
                }
            };

    /**
     * Resets client ID and client name to null.
     */
    private void clearClientData() {
        clientID = null;
        clientName = null;
    }
}
