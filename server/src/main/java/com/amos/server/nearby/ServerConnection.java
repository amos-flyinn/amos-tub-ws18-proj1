package com.amos.server.nearby;

import android.content.Context;
import android.os.Build;
import android.support.annotation.NonNull;
import android.util.Log;

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

import java.io.IOException;
import java.io.PipedInputStream;
import java.io.PipedOutputStream;
import java.security.SecureRandom;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

/**
 * Singleton managing Android nearby connection on the server side
 *
 * The server needs to send input events to the client and correctly receive the recorded screen.
 */
public class ServerConnection {

    private static final ServerConnection ourInstance = new ServerConnection();

    /** 1-to-1 since a device will be connected to only one other device at most. */
    private static final Strategy STRATEGY = Strategy.P2P_POINT_TO_POINT;

    private final String serverName = generateName(5);
    private String clientID;

    /** List of all discovered servers by name, continuously updated. */
    private List<String> clients = new ArrayList<>();

    /** Maps server names to their nearby connection IDs. */
    private HashMap<String, String> clientNamesToIDs = new HashMap<>();

    /** Maps server IDs to their nearby connection names. */
    private HashMap<String, String> clientIDsToNames = new HashMap<>();

    /** Tag for logging purposes. */
    private static final String TAG = "NearbyServer";

    /** Connection manager for the connection to FlyInn clients. */
    private ConnectionsClient connectionsClient;

    public static ServerConnection getInstance() {
        return ourInstance;
    }

    /**
     * Create a new server connection.
     */
    private ServerConnection() {
        // All real work is in the init!
    }

    /**
     * Bind application context to the singleton.
     *
     * @param ctx Application context should be passed to ensure survival between different activities.
     */
    public void init(Context ctx) {
        connectionsClient = Nearby.getConnectionsClient(ctx);
    }

    /**
     * Clears all servers map data as well as serverName/serverID and starts discovery
     */
    public void discover() {
        resetDiscovery();

        DiscoveryOptions discoveryOptions =
                new DiscoveryOptions.Builder().setStrategy(STRATEGY).build();

        connectionsClient.startDiscovery("com.amos.server", endpointDiscoveryCallback,
                discoveryOptions)
                .addOnSuccessListener( (Void unused) -> {
                    // started searching for servers successfully
                    Log.i(TAG, "Discovering connections on " + serverName);
                })
                .addOnFailureListener( (Exception e) -> {
                    // unable to start discovery
                    Log.e(TAG, e.toString());
                    Log.e(TAG, "Unable to start discovery on " + serverName);
                });
    }

    /**
     * Connect to nearby service with client name ending with our required code.
     *
     * @param code Suffix of connection target
     * @param callback Callbacks on connection success and failure
     */
    public void connectTo(String code, ConnectCallback callback){
        String searchedClientName = null;
        for (String clientName : clients){
            if(clientName.endsWith(code))
            {
                searchedClientName = clientName;
                break;
            }
        }

        String endpoint = clientNamesToIDs.get(searchedClientName);
        if (endpoint != null && searchedClientName != null)
        {
            clientID = endpoint;
            // callback success will be called in the subsequent function
            connectionsClient.requestConnection(searchedClientName, endpoint, buildConnectionLifecycleCallback(callback));
        } else {
            callback.failure();
        }

    }

    public void abort() {
    }

    public PipedOutputStream sendStream() throws IOException {
        PipedInputStream stream  = new PipedInputStream();
        PipedOutputStream data = new PipedOutputStream(stream);
        Payload payload = Payload.fromStream(stream);
        connectionsClient.sendPayload(clientID, payload);
        Log.d(TAG, "Sent test payload to receiver " + clientID);
        return data;
    }

    /**
     * Obtain data from clientID/serverName and data transfer information via this handle.
     */
    private final PayloadCallback payloadCallback =
            new PayloadCallback() {
                @Override
                public void onPayloadReceived(@NonNull String endpointId, @NonNull Payload payload) {
                    Log.d(TAG, "Payload received from " + endpointId);
                    Log.d(TAG, payload.toString());
                }

                @Override
                public void onPayloadTransferUpdate(@NonNull String endpointId, @NonNull PayloadTransferUpdate update) {
                    Log.d(TAG, "Payload transfer update from " + endpointId);
                    Log.d(TAG, update.toString());
                }
            };

    /**
     * Handling of discovered endpoints (servers). Adds new endpoints to servers data maps/list,
     * and removes lost endpoints.
     */
    private final EndpointDiscoveryCallback endpointDiscoveryCallback =
            new EndpointDiscoveryCallback() {
                @Override
                public void onEndpointFound(@NonNull String endpointId, @NonNull DiscoveredEndpointInfo info) {
                    // discovered a server, add to data maps
                    String endpointName = info.getEndpointName();

                    if (!(clientIDsToNames.containsKey(endpointId)
                            || clientNamesToIDs.containsKey(endpointName))) {
                        clients.add(endpointName);
                        clientNamesToIDs.put(endpointName, endpointId);
                        clientIDsToNames.put(endpointId, endpointName);
                        Log.i(TAG, serverName + " discovered endpoint " + endpointId + " with name " + endpointName);

                    } else {
                        // this should not happen
                        while (clients.remove(endpointName)) {}
                        clients.add(endpointName);
                        clientIDsToNames.put(endpointId, endpointName);
                        clientNamesToIDs.put(endpointName, endpointId);
                        Log.i(TAG, serverName + " rediscovered endpoint " + endpointId + " with name " + endpointName);
                    }
                }

                @Override
                public void onEndpointLost(@NonNull String endpointId) {
                    // previously discovered server is no longer reachable, remove from data maps
                    String lostEndpointName = clientIDsToNames.get(endpointId);
                    clientIDsToNames.remove(endpointId);
                    clientNamesToIDs.remove(lostEndpointName);
                    Log.i(TAG, serverName + " lost discovered endpoint " + endpointId);
                }
            };

    private ConnectionLifecycleCallback buildConnectionLifecycleCallback(ConnectCallback callback) {
        return new ConnectionLifecycleCallback() {
            @Override
            public void onConnectionInitiated(String endpointId, ConnectionInfo connectionInfo) {
                Log.i(TAG, "Connection initiated to " + endpointId);

                if (endpointId.equals(clientID)) {
                    connectionsClient.acceptConnection(endpointId, payloadCallback);
                } else {
                    // initiated connection is not with server selected by user
                    connectionsClient.rejectConnection(endpointId);
                    Log.i(TAG, "Connection rejected to non-selected server "
                            + endpointId);
                }
            }

            @Override
            public void onConnectionResult(String endpointId, ConnectionResolution result) {
                switch (result.getStatus().getStatusCode()) {
                    case ConnectionsStatusCodes.STATUS_OK:
                        // successful connection with server
                        Log.i(TAG, "Connected with " + endpointId);
                        resetDiscovery();
                        callback.success();
                        break;
                    case ConnectionsStatusCodes.STATUS_CONNECTION_REJECTED:
                        // connection was rejected by one side (or both)
                        Log.i(TAG, "Connection rejected with " + endpointId);
                        clientID = null;
                        callback.failure();
                        break;
                    case ConnectionsStatusCodes.STATUS_ERROR:
                        // connection was lost
                        Log.w(TAG, "Connection lost: " + endpointId);
                        clientID = null;
                        callback.failure();
                        break;
                    default:
                        // unknown status code. we shouldn't be here
                        Log.e(TAG, "Unknown error when attempting to connect with "
                                + endpointId);
                        clientID = null;
                        callback.failure();
                        break;
                }
            }

            @Override
            public void onDisconnected(String endpointId) {
                // disconnected from server
                Log.i(TAG, "Disconnected from " + endpointId);
                resetClientData();
            }
        };
    }

    /**
     * Generates a name for the server.
     *
     * TODO: Create a better name for the server
     *
     * @return The server name, consisting of the build model + a random string
     */
    private String generateName(int appendixLength){
        String AB = "0123456789ABCDEFGHIJKLMNOPQRSTUVWXYZabcdefghijklmnopqrstuvwxyz";
        SecureRandom rnd = new SecureRandom();

        StringBuilder sb = new StringBuilder(appendixLength);
        for (int i = 0; i < appendixLength; i++) {
            sb.append(AB.charAt(rnd.nextInt(AB.length())));
        }

        String name = Build.MODEL + "_" + sb.toString();
        Log.i(TAG, "Current name is: " + name);
        return name;
    }

    /**
     * Clears serverName/serverID and all server data maps as well as the servers list
     */
    private void resetClientData() {
        clientID = null;
    }

    /**
     * Handle established connection with app.
     *
     * Clears servers data maps, stops discovery of new servers and adds close connection button
     */
    private void resetDiscovery() {
        connectionsClient.stopDiscovery();
        clients.clear();
        clientNamesToIDs.clear();
        clientIDsToNames.clear();
    }
}