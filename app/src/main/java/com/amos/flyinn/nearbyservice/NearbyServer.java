package com.amos.flyinn.nearbyservice;

import android.Manifest;
import android.os.Handler;
import android.os.Looper;
import android.support.annotation.NonNull;
import android.util.Log;
import android.widget.Toast;

import com.amos.flyinn.R;
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
    public static final String[] REQUIRED_PERMISSIONS =
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
    private final String serviceName = "nearby_server";
    private final String serviceID = "com.amos.server";
    private String clientID;
    private String clientName;

    private NearbyService nearbyService;

    /**
     * Create new nearby server with the given name.
     *
     * @param service
     */
    public NearbyServer(@NonNull NearbyService service) throws SecurityException {
        this.nearbyService = service;
        this.connectionsClient = Nearby.getConnectionsClient(this.nearbyService);
    }

    /**
     * Server name as combination of name with suffix which is our nearby code
     * @return
     */
    public String getServerName() {
        return serviceName + this.nearbyService.getNearbyCode();
    }

    /**
     * Obtain data from clientID/clientName and data transfer information via this handle.
     */
    private final PayloadCallback payloadCallback =
            new PayloadCallback() {
                @Override
                public void onPayloadReceived(String endpointId, Payload payload) {
                    nearbyService.handlePayload(payload);
                }

                @Override
                public void onPayloadTransferUpdate(String endpointId, PayloadTransferUpdate update) {
                    nearbyService.handlePayloadTransferUpdate(update);
                }
            };

    /**
     * Broadcast our presence using Nearby Connection so FlyInn users can find us.
     * Resets clientID and clientName first.
     */
    public void start() {
        clearClientData();

        AdvertisingOptions advertisingOptions =
                new AdvertisingOptions.Builder().setStrategy(STRATEGY).build();

        connectionsClient.startAdvertising(getServerName(), serviceID,
                connectionLifecycleCallback, advertisingOptions)
                .addOnSuccessListener((Void unused) -> {
                    Log.d(TAG, "Start advertising Android nearby");
                    nearbyService.setServiceState(NearbyState.ADVERTISING,
                            nearbyService.getString(R.string.notification_advertising));
                })
                .addOnFailureListener((Exception e) -> {
                    Log.d(TAG, "Error trying to advertise Android nearby");
                    nearbyService.handleResponse(true, e.toString());

                    (new Handler(Looper.getMainLooper())).post(() -> Toast.makeText(
                            nearbyService.getApplicationContext(),
                            R.string.nearby_advertising_error, Toast.LENGTH_LONG).show());

                    // TODO do we restart the app if this happens (?)
                    nearbyService.setServiceState(NearbyState.STOPPED, "Failed to advertise android nearby");
                });
    }

    /**
     * Stop all things nearby
     */
    public void stop() {
        connectionsClient.stopAllEndpoints();
        Log.d(TAG, "Stopped all endpoints");
        nearbyService.setServiceState(NearbyState.STOPPED, "Stopped all endpoints");
    }

    /**
     * Callbacks for connections to other devices.
     * Includes token authentication and connection handling.
     */
    private final ConnectionLifecycleCallback connectionLifecycleCallback =
            new ConnectionLifecycleCallback() {
                @Override
                public void onConnectionInitiated(String endpointId, ConnectionInfo connectionInfo) {
                    clientName = connectionInfo.getEndpointName();
                    connectionsClient.acceptConnection(endpointId, payloadCallback);
                    Log.i(TAG, "Auto accepting initiated connection from " + endpointId);
                    nearbyService.setServiceState(NearbyState.CONNECTING, null);
                }

                @Override
                public void onConnectionResult(String endpointId, ConnectionResolution result) {
                    switch (result.getStatus().getStatusCode()) {
                        case ConnectionsStatusCodes.STATUS_OK:
                            // successful connection with client
                            Log.i(TAG, "Connected with " + endpointId);
                            connectionsClient.stopAdvertising();
                            clientID = endpointId;
                            nearbyService.setServiceState(NearbyState.CONNECTED,
                                    nearbyService.getString(R.string.notification_connected));
                            break;

                        case ConnectionsStatusCodes.STATUS_CONNECTION_REJECTED:
                            // connection was rejected by one side (or both)
                            Log.i(TAG, "Connection rejected with " + endpointId);
                            nearbyService.setServiceState(NearbyState.ADVERTISING,
                                    nearbyService.getString(R.string.notification_advertising));

                            (new Handler(Looper.getMainLooper())).post(() -> Toast.makeText(
                                    nearbyService.getApplicationContext(),
                                    R.string.nearby_connection_rejected, Toast.LENGTH_LONG).show());

                            clearClientData();
                            break;

                        case ConnectionsStatusCodes.STATUS_ERROR:
                            // connection was lost
                            Log.w(TAG, "Connection lost: " + endpointId);
                            nearbyService.setServiceState(NearbyState.ADVERTISING,
                                    nearbyService.getString(R.string.notification_advertising));

                            (new Handler(Looper.getMainLooper())).post(() -> Toast.makeText(
                                    nearbyService.getApplicationContext(),
                                    R.string.nearby_connection_error, Toast.LENGTH_LONG).show());

                            clearClientData();
                            break;

                        default:
                            // unknown status code. we shouldn't be here
                            Log.e(TAG, "Unknown error when attempting to connect with " + endpointId);
                            nearbyService.setServiceState(NearbyState.ADVERTISING,
                                    nearbyService.getString(R.string.notification_advertising));

                            (new Handler(Looper.getMainLooper())).post(() -> Toast.makeText(
                                    nearbyService.getApplicationContext(),
                                    R.string.nearby_connection_error, Toast.LENGTH_LONG).show());

                            clearClientData();
                    }
                }

                @Override
                public void onDisconnected(String endpointId) {
                    // disconnected from client
                    Log.i(TAG, "Disconnected from " + endpointId);
                    nearbyService.setServiceState(NearbyState.ADVERTISING,
                            nearbyService.getString(R.string.notification_advertising));

                    (new Handler(Looper.getMainLooper())).post(() -> Toast.makeText(
                            nearbyService.getApplicationContext(),
                            R.string.nearby_disconnected, Toast.LENGTH_LONG).show());
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
