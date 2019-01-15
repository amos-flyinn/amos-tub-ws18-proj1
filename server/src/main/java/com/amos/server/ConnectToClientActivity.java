package com.amos.server;

import android.Manifest;
import android.app.Activity;
import android.app.Notification;
import android.app.PendingIntent;
import android.app.TaskStackBuilder;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.os.Build;
import android.os.Bundle;
import android.support.annotation.CallSuper;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.NotificationCompat;
import android.support.v4.content.ContextCompat;
import android.util.Log;
import android.view.KeyEvent;
import android.view.inputmethod.EditorInfo;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.Notification;
import android.support.v4.app.NotificationCompat;
import android.support.v4.app.NotificationManagerCompat;

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

    /** 1-to-1 since a device will be connected to only one other device at most. */
    private static final Strategy STRATEGY = Strategy.P2P_POINT_TO_POINT;

    /** Connection manager for the connection to FlyInn clients. */
    protected ConnectionsClient connectionsClient;

    private final String clientName = generateName(5);
    private String clientID;
    private String clientNameNow;

    /** Toast to publish user notifications */
    private Toast mToast;

    /** List of all discovered servers by name, continuously updated. */
    private List<String> clients = new ArrayList<>();

    /** Maps server names to their nearby connection IDs. */
    private HashMap<String, String> clientNamesToIDs = new HashMap<>();

    /** Maps server IDs to their nearby connection names. */
    private HashMap<String, String> clientIDsToNames = new HashMap<>();

    /** Tag for logging purposes. */
    private static final String TAG = "ClientNearbyConnection";

    public static final String CHANNEL_ID = "server nearby";
    private static final int NOTIFY_ID = 1;

    /**
     * Obtain data from clientID/clientName and data transfer information via this handle.
     */
    private final PayloadCallback payloadCallback =
            new PayloadCallback() {
                @Override
                public void onPayloadReceived(String endpointId, Payload payload) {
                    Log.d(TAG, "Payload received");
                }

                @Override
                public void onPayloadTransferUpdate(String endpointId, PayloadTransferUpdate update) {
                    Log.d(TAG, "Payload transfer update");
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

                    if (!(clientIDsToNames.containsKey(endpointId)
                            || clientNamesToIDs.containsKey(endpointName))) {
                        clients.add(endpointName);
                        clientNamesToIDs.put(endpointName, endpointId);
                        clientIDsToNames.put(endpointId, endpointName);
                        Log.i(TAG, clientName + " discovered endpoint " + endpointId + " with name " + endpointName);

                    } else {
                        // this should not happen
                        while (clients.remove(endpointName)) {}
                        clients.add(endpointName);
                        clientIDsToNames.put(endpointId, endpointName);
                        clientNamesToIDs.put(endpointName, endpointId);
                        Log.i(TAG, clientName + " rediscovered endpoint " + endpointId + " with name " + endpointName);
                    }
                }

                @Override
                public void onEndpointLost(String endpointId) {
                    // previously discovered server is no longer reachable, remove from data maps
                    String lostEndpointName = clientIDsToNames.get(endpointId);
                    clientIDsToNames.remove(endpointId);
                    clientNamesToIDs.remove(lostEndpointName);
                    Log.i(TAG, clientName + " lost discovered endpoint " + endpointId);
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
                            mToast.setText(R.string.nearby_connection_success);
                            mToast.show();
                            connectedToApp();
                            raiseNotification(buildNotification("Connection status: CONNECTED"));
                            break;

                        case ConnectionsStatusCodes.STATUS_CONNECTION_REJECTED:
                            // connection was rejected by one side (or both)
                            Log.i(TAG, "Connection rejected with " + endpointId);
                            mToast.setText(R.string.nearby_connection_rejected);
                            mToast.show();
                            clientNameNow = null;
                            clientID = null;
                            //raiseNotification(buildNotification("Connection was rejected."));
                            break;

                        case ConnectionsStatusCodes.STATUS_ERROR:
                            // connection was lost
                            Log.w(TAG, "Connection lost: " + endpointId);
                            mToast.setText(R.string.nearby_connection_error);
                            mToast.show();
                            clientNameNow = null;
                            clientID = null;
                            //raiseNotification(buildNotification("Failed to establish connection."));
                            break;

                        default:
                            // unknown status code. we shouldn't be here
                            Log.e(TAG, "Unknown error when attempting to connect with "
                                    + endpointId);
                            mToast.setText(R.string.nearby_connection_error);
                            mToast.show();
                            clientNameNow = null;
                            clientID = null;
                            raiseNotification(buildNotification("Failed to establish connection."));
                    }
                }

                @Override
                public void onDisconnected(String endpointId) {
                    // disconnected from server
                    Log.i(TAG, "Disconnected from " + endpointId);
                    mToast.setText(R.string.nearby_disconnected);
                    raiseNotification(buildNotification("Connection closed."));

                    mToast.show();
                    clearServerData();
                    finish();
                }
            };



    private void requestConnectionWithClient(String code){

        String searchedClientName = null;
        for (String clientName : clients){
            if(clientName.endsWith(code))
            {
                searchedClientName = clientName;
                break;
            }
        }

        if (searchedClientName == null)
        {
            Toast.makeText(this,"The code given was not found. Please try again",Toast.LENGTH_LONG).show();
            //raiseNotification(buildNotification("The code given was not found. Please try again"));
            return;
        }


        String endpoint = clientNamesToIDs.get(searchedClientName);
        clientID = endpoint;


        connectionsClient.requestConnection(searchedClientName,endpoint,connectionLifecycleCallback);

    }


    private void createChannel() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {

            NotificationChannel c = new NotificationChannel(CHANNEL_ID,
                    "server_channel", NotificationManager.IMPORTANCE_HIGH);

            c.setDescription("Server Channel checks for Connection.");
            c.enableLights(true);
            c.setLightColor(0xFFFF0000);

            NotificationManager manager = getSystemService(NotificationManager.class);
            manager.createNotificationChannel(c);
        }
    }

    private Notification buildNotification(String message) {
        NotificationCompat.Builder b =
                new NotificationCompat.Builder(this, CHANNEL_ID);

        b.setOngoing(true)
                .setPriority(NotificationCompat.PRIORITY_HIGH)  // makes notification pop up
                .setContentTitle(String.format("Nearby service on Server"))
                .setContentText(message)
                .setSmallIcon(android.R.drawable.stat_notify_sync);

        return (b.build());
    }

    private void raiseNotification(Notification notification) {
        NotificationManagerCompat mgr = NotificationManagerCompat.from(this);
        mgr.notify(NOTIFY_ID, notification);
    }




    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        createChannel();
        setContentView(R.layout.activity_connect_to_client);
        EditText text = findViewById(R.id.connect_editText);
        text.setOnEditorActionListener(new TextView.OnEditorActionListener() {
            @Override
            public boolean onEditorAction(TextView v, int actionId, KeyEvent event) {
                if (actionId == EditorInfo.IME_ACTION_DONE) {
                    String name = v.getText().toString(); // Get the String
                    requestConnectionWithClient(name);
                    return true;
                }
                return false;
            }
        });


        if (!hasPermissions(this, REQUIRED_PERMISSIONS) &&
                Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            requestPermissions(REQUIRED_PERMISSIONS, REQUEST_CODE_REQUIRED_PERMISSIONS);
        } else {
            Log.w(TAG, "Could not check permissions due to version");
        }

        connectionsClient = Nearby.getConnectionsClient(this);
        mToast = Toast.makeText(this, "", Toast.LENGTH_SHORT);


        startDiscovering();


    }

    /**
     * Checks needed permissions for nearby connection
     */
    @Override
    protected void onStart() {
        super.onStart();

        // user may have changed permissions
        if (!hasPermissions(this, REQUIRED_PERMISSIONS) &&
                Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            requestPermissions(REQUIRED_PERMISSIONS, REQUEST_CODE_REQUIRED_PERMISSIONS);
        } else {
            mToast.setText(R.string.connection_rejected_version);
            Log.w(TAG, "Could not check permissions due to version");
        }
    }

    /**
     * Stops all connection discovery and connections from this client
     * before calling super.onDestroy()
     */
    @Override
    protected void onDestroy() {
        connectionsClient.stopDiscovery();
        connectionsClient.stopAllEndpoints();
        clearServerData();
        super.onDestroy();
    }

    /**
     * Clears all servers map data as well as serverName/serverID and starts discovery
     */
    private void startDiscovering() {
        clearServerData();

        DiscoveryOptions discoveryOptions =
                new DiscoveryOptions.Builder().setStrategy(STRATEGY).build();

        connectionsClient.startDiscovery("com.amos.server", endpointDiscoveryCallback,
                discoveryOptions)
                .addOnSuccessListener( (Void unused) -> {
                    // started searching for servers successfully
                    Log.i(TAG, "Discovering connections on " + clientName);
                    raiseNotification(buildNotification("Connection status: DISCOVERY"));
                    //mToast.setText(R.string.nearby_discovering_success);
                    //mToast.show();
                })
                .addOnFailureListener( (Exception e) -> {
                    // unable to start discovery
                    Log.e(TAG, "Unable to start discovery on " + clientName);
                    mToast.setText(R.string.nearby_discovering_error);
                    mToast.show();
                    finish();
                });
    }

    /**
     * Clears serverName/serverID and all server data maps as well as the servers list
     */
    private void clearServerData() {
        clients.clear();
        clientIDsToNames.clear();
        clientNamesToIDs.clear();
        clientID = null;
        clientNameNow = null;
    }

    /**
     * Handle established connection with app.
     *
     * Clears servers data maps, stops discovery of new servers and adds close connection button
     */
    private void connectedToApp() {
        connectionsClient.stopDiscovery();
        clients.clear();
        clientNamesToIDs.clear();
        clientIDsToNames.clear();

        // Start setting up a connection with the other side
        Intent intent = new Intent(this, ConnectionSetupServerActivity.class);
        intent.putExtra("endpointId", clientID);
        startActivity(intent);
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
                Log.w(TAG, "Permissions necessary for " +
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
        Log.i(TAG, "Current name is: " + name);
        return name;
    }

}
