package com.amos.flyinn;

import android.Manifest;
import android.app.AlertDialog;
import android.app.ListActivity;
import android.content.Context;
import android.content.DialogInterface;
import android.content.pm.PackageManager;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.support.annotation.CallSuper;
import android.support.annotation.NonNull;
import android.support.v4.content.ContextCompat;
import android.util.Log;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.ListView;
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
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

/**
 * Activity that handles connection to servers via nearby connection.
 * Updates a list (that is viewed by the user) containing all reachable servers.
 * Includes permission handling and simple token authentication.
 */
public class NearbyConnectionActivity extends ListActivity {

    /** Permissions required for Nearby Connection */
    private static final String[] REQUIRED_PERMISSIONS =
            new String[] {
                    Manifest.permission.BLUETOOTH,
                    Manifest.permission.BLUETOOTH_ADMIN,
                    Manifest.permission.ACCESS_WIFI_STATE,
                    Manifest.permission.CHANGE_WIFI_STATE,
                    Manifest.permission.ACCESS_COARSE_LOCATION
            };

    private static final int REQUEST_CODE_REQUIRED_PERMISSIONS = 1;

    /** 1-to-1 since a device will be connected to only one other device at most. */
    private static final Strategy STRATEGY = Strategy.P2P_POINT_TO_POINT;

    /** Connection manager for the connection to FlyInn clients. */
    protected ConnectionsClient connectionsClient;

    private final String clientName = generateName(5);
    private String serverID;
    private String serverName;

    final Handler handler = new Handler();

    /** Toast to publish user notifications */
    private Toast mToast = Toast.makeText(this  , "" , Toast.LENGTH_SHORT);

    /** List of all discovered servers by name, continuously updated. */
    private List<String> servers = new ArrayList<>();

    /** Maps server names to their nearby connection IDs. */
    private HashMap<String, String> serverNamesToIDs = new HashMap<>();

    /** Maps server IDs to their nearby connection names. */
    private HashMap<String, String> serverIDsToNames = new HashMap<>();

    /** Tag for logging purposes. */
    private static final String NEARBY_TAG = "ClientNearbyConnection";


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
     * Handling of discovered endpoints (servers). Adds new endpoints to servers data maps/list,
     * and removes lost endpoints.
     */
    private final EndpointDiscoveryCallback endpointDiscoveryCallback =
            new EndpointDiscoveryCallback() {
                @Override
                public void onEndpointFound(String endpointId, DiscoveredEndpointInfo info) {
                    // discovered a server, add to data maps
                    String endpointName = info.getEndpointName();

                    if (!(serverIDsToNames.containsKey(endpointId)
                            || serverNamesToIDs.containsKey(endpointName))) {
                        servers.add(endpointName);
                        serverNamesToIDs.put(endpointName, endpointId);
                        serverIDsToNames.put(endpointId, endpointName);
                        ((ArrayAdapter) NearbyConnectionActivity.this.getListAdapter())
                                .notifyDataSetChanged();
                        Log.d(NEARBY_TAG, clientName + " discovered endpoint " + endpointId);

                    } else {
                        // this should not happen
                        while (servers.remove(endpointName)) {}
                        servers.add(endpointName);
                        serverIDsToNames.put(endpointId, endpointName);
                        serverNamesToIDs.put(endpointName, endpointId);
                        ((ArrayAdapter) NearbyConnectionActivity.this.getListAdapter())
                                .notifyDataSetChanged();
                        Log.w(NEARBY_TAG, clientName + " rediscovered endpoint " + endpointId);
                    }
                }

                @Override
                public void onEndpointLost(String endpointId) {
                    // previously discovered server is no longer reachable, remove from data maps
                    String lostEndpointName = serverIDsToNames.get(endpointId);
                    serverIDsToNames.remove(endpointId);
                    serverNamesToIDs.remove(lostEndpointName);
                    while (servers.remove(lostEndpointName)) {}
                    ((ArrayAdapter) NearbyConnectionActivity.this.getListAdapter())
                            .notifyDataSetChanged();
                    Log.d(NEARBY_TAG, clientName + " lost discovered endpoint " + endpointId);
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
                    Log.i(NEARBY_TAG, "Connection initiated to " + endpointId);

                    if (endpointId.equals(serverID)) {
                        // authentication via tokens
                        // TODO replace token authentication with QR code/manual code input
                        new AlertDialog.Builder(NearbyConnectionActivity.this)
                                .setTitle("Accept connection to " + serverName + "?")
                                .setMessage("Confirm the code matches on both devices: " +
                                        connectionInfo.getAuthenticationToken())
                                .setPositiveButton(android.R.string.yes,
                                        (DialogInterface dialog, int which) ->
                                                // accept the connection
                                                connectionsClient.acceptConnection(endpointId,
                                                        payloadCallback))
                                .setNegativeButton(android.R.string.cancel,
                                        (DialogInterface dialog, int which) ->
                                                // reject the connection
                                                connectionsClient.rejectConnection(endpointId))
                                .setIcon(android.R.drawable.ic_dialog_alert)
                                .show();

                    } else {
                        // initiated connection is not with server selected by user
                        connectionsClient.rejectConnection(endpointId);
                        Log.i(NEARBY_TAG, "Connection rejected to non-selected server "
                                + endpointId);
                    }
                }

                @Override
                public void onConnectionResult(String endpointId, ConnectionResolution result) {
                    switch (result.getStatus().getStatusCode()) {

                        case ConnectionsStatusCodes.STATUS_OK:
                            // successful connection with server
                            Log.i(NEARBY_TAG, "Connected with " + endpointId);
                            mToast.setText(R.string.nearby_connection_success);
                            mToast.show();
                            connectedToServer();
                            break;

                        case ConnectionsStatusCodes.STATUS_CONNECTION_REJECTED:
                            // connection was rejected by one side (or both)
                            Log.i(NEARBY_TAG, "Connection rejected with " + endpointId);
                            mToast.setText(R.string.nearby_connection_rejected);
                            mToast.show();
                            serverName = null;
                            serverID = null;
                            break;

                        case ConnectionsStatusCodes.STATUS_ERROR:
                            // connection was lost
                            Log.w(NEARBY_TAG, "Connection lost: " + endpointId);
                            mToast.setText(R.string.nearby_connection_error);
                            mToast.show();
                            serverName = null;
                            serverID = null;
                            break;

                        default:
                            // unknown status code. we shouldn't be here
                            Log.e(NEARBY_TAG, "Unknown error when attempting to connect with "
                                    + endpointId);
                            mToast.setText(R.string.nearby_connection_error);
                            mToast.show();
                            serverName = null;
                            serverID = null;
                    }
                }

                @Override
                public void onDisconnected(String endpointId) {
                    // disconnected from server
                    Log.i(NEARBY_TAG, "Disconnected from " + endpointId);
                    mToast.setText(R.string.nearby_disconnected);
                    mToast.show();
                    clearServerData();
                    finish();
                }
            };


    /**
     * Initialises nearby's connectionsClient and our list adapter to showcase servers to the user,
     * checks permissions and starts discovery
     * @param savedInstanceState
     */
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        if (!hasPermissions(this, REQUIRED_PERMISSIONS) &&
                Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            requestPermissions(REQUIRED_PERMISSIONS, REQUEST_CODE_REQUIRED_PERMISSIONS);
        } else {
            Log.w(NEARBY_TAG, "Could not check permissions due to version");
        }
        
        connectionsClient = Nearby.getConnectionsClient(this);

        ArrayAdapter<String> adapter = new ArrayAdapter<>(
                this, android.R.layout.simple_list_item_1, servers);
        setListAdapter(adapter);

        startDiscovering();
    }

    /**
     * Checks needed permissions for nearby connection
     */
    @Override
    protected void onStart() {
        super.onStart();

        if (!hasPermissions(this, REQUIRED_PERMISSIONS) &&
                Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            requestPermissions(REQUIRED_PERMISSIONS, REQUEST_CODE_REQUIRED_PERMISSIONS);
        } else {
            Log.w(NEARBY_TAG, "Could not check permissions due to version");
        }
    }

    /**
     * Stops all connection discovery and connections from this client
     * before calling super.onDestroy()
     */
    @Override
    protected void onDestroy() {
        clearServerData();
        connectionsClient.stopDiscovery();
        connectionsClient.stopAllEndpoints();
        super.onDestroy();
    }

    /**
     * Clears all servers map data as well as serverName/serverID and starts discovery
     */
    private void startDiscovering() {
        clearServerData();

        DiscoveryOptions discoveryOptions =
                new DiscoveryOptions.Builder().setStrategy(STRATEGY).build();

        connectionsClient.startDiscovery("com.amos.flyinn", endpointDiscoveryCallback,
                discoveryOptions)
                .addOnSuccessListener( (Void unused) -> {
                    // started searching for servers successfully
                    Log.i(NEARBY_TAG, "Discovering connections on " + clientName);
                    mToast.setText(R.string.nearby_discovering_success);
                    mToast.show();
                })
                .addOnFailureListener( (Exception e) -> {
                    // unable to start discovery
                    Log.e(NEARBY_TAG, "Unable to start discovery on " + clientName);
                    mToast.setText(R.string.nearby_discovering_error);
                    mToast.show();
                    finish();
                });
    }

    /**
     * Handles selection of server from list by user and requesting connections to those servers
     * when not connected to another device, and "close connection" button actions if a server
     * connection is active.
     * @param l The ListView where the click happened
     * @param v The view that was clicked within the ListView
     * @param position The position of the view in the list
     * @param id The row id of the item that was clicked
     */
    @Override
    protected void onListItemClick(ListView l, View v, int position, long id) {
        super.onListItemClick(l, v, position, id);

        // user chose to disconnect from server
        if (servers.get(position).startsWith(
                getResources().getString(R.string.nearby_close_connection))) {

            connectionsClient.stopAllEndpoints();
            Log.i(NEARBY_TAG, "User chose to disconnect from " + serverID);
            // make sure connectionLifecycleCallback.onDisconnected() is called by delaying finish
            handler.postDelayed(() -> finish(), 2000);
            return;
        }

        // store user selection
        serverName = servers.get(position);
        serverID = serverNamesToIDs.get(serverName);

        //request connection to server selected by user
        connectionsClient.requestConnection(clientName, serverID, connectionLifecycleCallback)
                .addOnSuccessListener( (Void unused) -> {
                    // connection request successful
                    Log.i(NEARBY_TAG, clientName + " requested connection to " + serverID);
                })
                .addOnFailureListener( (Exception e) -> {
                    // failed to request connection
                    serverName = null;
                    serverID = null;
                    Log.w(NEARBY_TAG, clientName + " failed requesting connection to " +
                            serverID);
                    mToast.setText(R.string.nearby_connection_error);
                    mToast.show();
                });
    }

    /**
     * Clears serverName/serverID and all server data maps as well as the servers list
     */
    private void clearServerData() {
        servers.clear();
        ((ArrayAdapter) this.getListAdapter()).notifyDataSetChanged();
        serverIDsToNames.clear();
        serverNamesToIDs.clear();
        serverID = null;
        serverName = null;
    }

    /**
     * Clears servers data maps, stops discovery of new servers and adds close connection button
     */
    private void connectedToServer() {
        servers.clear();
        serverNamesToIDs.clear();
        serverIDsToNames.clear();
        connectionsClient.stopDiscovery();

        //add close connection button
        servers.add(getResources().getString(R.string.nearby_close_connection) + " " + serverName);
        ((ArrayAdapter) this.getListAdapter()).notifyDataSetChanged();
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
                Log.w(NEARBY_TAG, "Permissions necessary for " +
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
        Log.d(NEARBY_TAG, "Current name is: " + name);
        return name;
    }
}
