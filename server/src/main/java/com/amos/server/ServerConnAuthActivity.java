package com.amos.server;

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
import com.google.android.gms.nearby.connection.ConnectionInfo;
import com.google.android.gms.nearby.connection.ConnectionLifecycleCallback;
import com.google.android.gms.nearby.connection.ConnectionResolution;
import com.google.android.gms.nearby.connection.ConnectionsClient;
import com.google.android.gms.nearby.connection.ConnectionsStatusCodes;
import com.google.android.gms.nearby.connection.DiscoveredEndpointInfo;
import com.google.android.gms.nearby.connection.DiscoveryOptions;
import com.google.android.gms.nearby.connection.EndpointDiscoveryCallback;
import com.google.android.gms.nearby.connection.Payload;
import com.google.android.gms.nearby.connection.PayloadCallback;
import com.google.android.gms.nearby.connection.PayloadTransferUpdate;
import com.google.android.gms.nearby.connection.Strategy;

import java.security.SecureRandom;
import java.util.HashMap;
/**
 *  Handles the server-side connection for FlyInn authentication.
 *  Checks permissions, manages a map of reachable clients connects to a client given its numeric
 *  code (as displayed to the user).
 */
public class ServerConnAuthActivity extends Activity {

    /** Permissions required for Nearby Connection. */
    private static final String[] REQUIRED_PERMISSIONS =
            new String[] {
                    Manifest.permission.BLUETOOTH,
                    Manifest.permission.BLUETOOTH_ADMIN,
                    Manifest.permission.ACCESS_WIFI_STATE,
                    Manifest.permission.CHANGE_WIFI_STATE,
                    Manifest.permission.ACCESS_COARSE_LOCATION
            };

    private static final int REQUEST_CODE_REQUIRED_PERMISSIONS = 1;

    /** Length of the numeric code displayed on the client used to connect server and client. */
    private final int CODE_LENGTH = 4;

    /** Length of the random suffix in the server names. */
    private final int NAME_SUFFIX_LENGTH = 5;

    /** 1-to-1 since a device will be connected to only one other device at most. */
    private static final Strategy STRATEGY = Strategy.P2P_POINT_TO_POINT;

    private ConnectionsClient connectionsClient;

    private final String serverName = generateName();
    private String clientID;
    private String clientName;
    private HashMap<String, String> clientNamesToIDs = new HashMap<>();
    private HashMap<String, String> clientIDsToNames = new HashMap<>();

    private Toast mToast;
    Handler handler = new Handler();

    /** Tag for logging purposes. */
    private static final String CONN_AUTH_TAG = "ServerConnAuth";


    /**
     * Obtain data from client and data transfer information via this handle.
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
     * Handling of endpoint (clients) discovery. Adds newly discovered  endpoints to clients
     * data maps, and removes lost endpoints.
     */
    private final EndpointDiscoveryCallback endpointDiscoveryCallback =
            new EndpointDiscoveryCallback() {
                @Override
                public void onEndpointFound(String endpointId, DiscoveredEndpointInfo info) {
                    // discovered a server, add to data maps
                    addClient(info.getEndpointName(), endpointId);
                }

                @Override
                public void onEndpointLost(String endpointId) {
                    // previously discovered server is no longer reachable, remove from data maps
                    removeClient(endpointId);
                }
            };

    /**
     * Callbacks for connections to other devices (clients).
     * Includes check for selected client given its app code via methods
     * and connection/disconnect handling.
     */
    private final ConnectionLifecycleCallback connectionLifecycleCallback =
            new ConnectionLifecycleCallback() {
                @Override
                public void onConnectionInitiated(String endpointId, ConnectionInfo connectionInfo) {
                    Log.i(CONN_AUTH_TAG, "Connection initiated to " + endpointId);

                    if (clientName != null && clientID != null && clientID.equals(endpointId)
                            && connectionInfo.getEndpointName().endsWith(clientName)) {

                        connectionsClient.acceptConnection(endpointId, payloadCallback);
                        Log.i(CONN_AUTH_TAG, "Attempt to connect to " + endpointId);

                    } else {
                        connectionsClient.rejectConnection(endpointId);
                        Log.i(CONN_AUTH_TAG, "Reject connection to " + endpointId);
                    }
                }

                @Override
                public void onConnectionResult(String endpointId, ConnectionResolution result) {
                    switch (result.getStatus().getStatusCode()) {

                        case ConnectionsStatusCodes.STATUS_OK:
                            // successful connection with server
                            Log.i(CONN_AUTH_TAG, "Connected with " + endpointId);
                            mToast.setText(R.string.nearby_connection_success);
                            mToast.show();
                            connectionsClient.stopDiscovery();
                            break;

                        case ConnectionsStatusCodes.STATUS_CONNECTION_REJECTED:
                            // connection was rejected by one side (or both)
                            Log.i(CONN_AUTH_TAG, "Connection rejected with " + endpointId);
                            mToast.setText(R.string.nearby_connection_rejected);
                            mToast.show();
                            clientName = null;
                            clientID = null;
                            break;

                        case ConnectionsStatusCodes.STATUS_ERROR:
                            // connection was lost
                            Log.w(CONN_AUTH_TAG, "Connection lost: " + endpointId);
                            mToast.setText(R.string.nearby_connection_error);
                            mToast.show();
                            clientName = null;
                            clientID = null;
                            break;

                        default:
                            // unknown status code. we shouldn't be here
                            Log.e(CONN_AUTH_TAG, "Unknown error when attempting to connect with "
                                    + endpointId);
                            mToast.setText(R.string.nearby_connection_error);
                            mToast.show();
                            clientName = null;
                            clientID = null;
                    }
                }

                @Override
                public void onDisconnected(String endpointId) {
                    // disconnected from server
                    Log.i(CONN_AUTH_TAG, "Disconnected from " + endpointId);
                    mToast.setText(R.string.nearby_disconnected);
                    mToast.show();
                    clearClientData();

                    // display toast for 2s, then recreate
                    handler.postDelayed(() -> recreate(), 2000);
                }
            };


    /**
     * Returns this server's name.
     *
     * @return The server name as generated by generateName().
     */
    protected String getServer() { return serverName; }

    /**
     * Returns information on the currently connected client.
     *
     * @return 2-entry array, where entry one is the client's ID, and entry two is its name.
     */
    protected String[] getClient() { return new String[]{clientID, clientName}; }

    /**
     * Returns the map of reachable clients managed by the server mapping client names to IDs.
     *
     * @return The reachable client map mapping client names to IDs.
     */
    protected HashMap<String, String> getClientNamesMap() { return clientNamesToIDs; }

    /**
     * Returns the map of reachable clients managed by the server mapping client IDs to names.
     *
     * @return The reachable client map mapping client IDs to names.
     */
    protected HashMap<String, String> getClientIDsMap() { return clientIDsToNames; }


    /**
     * Initialises nearby connectionsClient and Toast, checks permissions and starts discovery by
     * calling startAdvertising().
     *
     * @param savedInstanceState
     */
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        if (!hasPermissions(this, REQUIRED_PERMISSIONS) &&
                Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            requestPermissions(REQUIRED_PERMISSIONS, REQUEST_CODE_REQUIRED_PERMISSIONS);
        } else {
            Log.w(CONN_AUTH_TAG, "Could not check permissions due to version");
        }


        Log.i(CONN_AUTH_TAG, "Current name is: " + serverName);
        connectionsClient = Nearby.getConnectionsClient(this);
        mToast = Toast.makeText(this, "", Toast.LENGTH_SHORT);

        startDiscovering();
    }

    /**
     * Checks needed permissions for nearby connection.
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
     * Stops all connection discovery and connections from this client and removes all client data
     * before calling super.onDestroy().
     */
    @Override
    protected void onDestroy() {
        connectionsClient.stopDiscovery();
        connectionsClient.stopAllEndpoints();
        clearClientData();
        super.onDestroy();
    }


    /**
     * Clears all client data and starts discovery
     */
    private void startDiscovering() {
        clearClientData();

        DiscoveryOptions discoveryOptions =
                new DiscoveryOptions.Builder().setStrategy(STRATEGY).build();

        connectionsClient.startDiscovery("com.amos.flyinn", endpointDiscoveryCallback,
                discoveryOptions)
                .addOnSuccessListener( (Void unused) -> {
                    // started searching for clients successfully
                    Log.i(CONN_AUTH_TAG, "Discovering connections on " + serverName);
                    mToast.setText(R.string.nearby_discovering_success);
                    mToast.show();
                })
                .addOnFailureListener( (Exception e) -> {
                    // unable to start discovery
                    Log.e(CONN_AUTH_TAG, "Unable to start discovery on " + serverName);
                    mToast.setText(R.string.nearby_discovering_error);
                    mToast.show();
                    finish();
                });
    }

    /**
     * Attempts to connect to client identified by the given app code. This code must be equal to
     * the one displayed to the user on the client app.
     *
     * @param clientCode Identifier for the client. Numeric 4-digit code displayed to the user.
     *
     * @return True if the client is reachable (in the server's data maps), false otherwise.
     */
    protected boolean connectToClient(String clientCode) {
        if (clientNamesToIDs.containsKey(clientCode)) {
            // client is reachable (=in our data maps)
            clientName = clientCode;
            clientID = clientNamesToIDs.get(clientCode);
            connectionsClient.requestConnection(serverName, clientID, connectionLifecycleCallback);
            Log.i(CONN_AUTH_TAG, "User selected endpoint " + clientID + " with code "
                    + clientCode + " on server " + serverName);
            return true;
        }
        // else client not reachable
        Log.w(CONN_AUTH_TAG, "Endpoint with code " + clientCode
                + " not reachable from server " + serverName);
        mToast.setText(R.string.nearby_connection_unreachable);
        mToast.show();
        return false;
    }

    /**
     * Adds the client specified by the given name and ID to the server's data maps. The client's
     * numeric identifier (app code displayed to user) is extracted from endpointName and used
     * further as client name.
     *
     * @param endpointName The name of the client. It is stripped of all but the numeric code,
     *                     and then stored in the data maps with the ID.
     * @param id The nearby connection ID of the client.
     */
    private void addClient(String endpointName, String id) {
        // extract last 4 characters of endpointName as name of the client (4-digit code)
        if (endpointName.length() >= CODE_LENGTH) {
            String name = endpointName.substring(endpointName.length()-CODE_LENGTH);
            clientIDsToNames.put(id, name);
            clientNamesToIDs.put(name, id);
            Log.i(CONN_AUTH_TAG, serverName + " discovered endpoint " + id);
        } else {
            Log.e(CONN_AUTH_TAG, serverName + " discovered faulty endpoint " + id);
        }
    }

    /**
     * Removes the given client from the server's data maps.
     *
     * @param id The nearby connection ID of the client.
     */
    private void removeClient(String id) {
        try {
            String name = clientIDsToNames.get(id);
            clientIDsToNames.remove(id);
            clientNamesToIDs.remove(name);
            Log.i(CONN_AUTH_TAG, serverName + " lost discovered endpoint " + id);
        } catch (NullPointerException e) {
            Log.e(CONN_AUTH_TAG, "Endpoint " + id
                    + " was not stored correctly in the data maps of " + serverName);
        }
    }

    /**
     * Clears clientName/clientID (null) and all client data maps.
     */
    private void clearClientData() {
        clientIDsToNames.clear();
        clientNamesToIDs.clear();
        clientID = null;
        clientName = null;
    }

    /**
     * Determines whether the FlyInn server app has the necessary permissions to run nearby.
     *
     * @param context Checks the permissions against this context/application environment.
     * @param permissions The permissions to be checked.
     *
     * @return True if the app was granted all the permissions, false otherwise.
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
     * @param requestCode The request code passed in requestPermissions().
     * @param permissions Permissions that must be granted to run nearby connections.
     * @param grantResults Results of granting permissions.
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
                Log.w(CONN_AUTH_TAG, "Permissions necessary for " +
                        "Nearby Connection were not granted.");
                mToast.setText(R.string.nearby_missing_permissions);
                mToast.show();
                finish();
            }
        }
        recreate();
    }

    /**
     * Generates a name for the server.
     *
     * @return The server name, consisting of R.string.flyinn_server_name and a random
     * string of length NAME_SUFFIX_LENGTH.
     */
    protected String generateName() {
        String AB = "0123456789ABCDEFGHIJKLMNOPQRSTUVWXYZabcdefghijklmnopqrstuvwxyz";
        SecureRandom rnd = new SecureRandom();

        StringBuilder sb = new StringBuilder(NAME_SUFFIX_LENGTH);
        for (int i = 0; i < NAME_SUFFIX_LENGTH; i++) {
            sb.append(AB.charAt(rnd.nextInt(AB.length())));
        }
        return "FlyInnServer-" + sb.toString();
    }
}
