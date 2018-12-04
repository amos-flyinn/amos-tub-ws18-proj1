package com.amos.server;

import android.Manifest;
import android.app.Activity;
import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.os.Build;
import android.os.Handler;
import android.os.Looper;
import android.os.Message;
import android.support.annotation.CallSuper;
import android.support.annotation.NonNull;
import android.support.v4.content.ContextCompat;
import android.os.Bundle;
import android.util.Log;
import android.view.MotionEvent;
import android.widget.Button;
import android.widget.TextView;

import org.webrtc.PeerConnection;
import org.webrtc.SurfaceViewRenderer;

import android.view.View;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.widget.Toast;

import com.amos.server.eventsender.EventServer;
import com.amos.server.signaling.Emitter;
import com.amos.server.signaling.WebServer;
import com.amos.server.webrtc.IPeer;
import com.amos.server.webrtc.PeerWrapper;
import com.amos.shared.TouchEvent;
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
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.LinkedBlockingQueue;

public class MainActivity extends Activity {
    TextView connectionInfo;
    Button threadStarter;
    Thread senderRunner;
    EventServer eventSender;
    BlockingQueue<TouchEvent> msgQueue;
    Handler uiHandler;
    SurfaceViewRenderer view;

    private PeerConnection localConnection;
    private WebServer webSocketServer;
    private PeerWrapper peerWrapper;
    private Button buttonInit;
    private SurfaceViewRenderer remoteRender;

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
                @Override
                public void onConnectionInitiated(String endpointId, ConnectionInfo connectionInfo) {
                    Log.i(NEARBY_TAG, "Connection initiated by " + endpointId);
                    clientName = connectionInfo.getEndpointName();

                    // authentication via tokens
                    // TODO replace token authentication with QR code/manual code input
                    new AlertDialog.Builder(MainActivity.this)
                            .setTitle("Accept connection to " + clientName + "?")
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
                }

                @Override
                public void onConnectionResult(String endpointId, ConnectionResolution result) {
                    switch (result.getStatus().getStatusCode()) {

                        case ConnectionsStatusCodes.STATUS_OK:
                            // successful connection with client
                            Log.i(NEARBY_TAG, "Connected with " + endpointId);
                            Toast.makeText(MainActivity.this,
                                    R.string.nearby_connection_success, Toast.LENGTH_SHORT).show();
                            connectionsClient.stopAdvertising();
                            clientID = endpointId;
                            break;

                        case ConnectionsStatusCodes.STATUS_CONNECTION_REJECTED:
                            // connection was rejected by one side (or both)
                            Log.i(NEARBY_TAG, "Connection rejected with " + endpointId);
                            Toast.makeText(MainActivity.this,
                                    R.string.nearby_connection_rejected, Toast.LENGTH_LONG).show();
                            clientName = null;
                            clientID = null;
                            break;

                        case ConnectionsStatusCodes.STATUS_ERROR:
                            // connection was lost
                            Log.w(NEARBY_TAG, "Connection lost: " + endpointId);
                            Toast.makeText(MainActivity.this,
                                    R.string.nearby_connection_lost, Toast.LENGTH_LONG).show();
                            clientName = null;
                            clientID = null;
                            break;

                        default:
                            // unknown status code. we shouldn't be here
                            Log.e(NEARBY_TAG, "Unknown error when attempting to connect with "
                                    + endpointId);
                            Toast.makeText(MainActivity.this,
                                    R.string.nearby_connection_lost, Toast.LENGTH_LONG).show();
                            clientName = null;
                            clientID = null;
                    }
                }

                @Override
                public void onDisconnected(String endpointId) {
                    // disconnect from client
                    Log.i(NEARBY_TAG, "Disconnected from " + endpointId);
                    Toast.makeText(MainActivity.this,
                            R.string.nearby_disconnected, Toast.LENGTH_LONG).show();
                    startAdvertising();
                }
            };


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        connectionInfo = findViewById(R.id.connectionInfo);
        connectionInfo.setVisibility(View.INVISIBLE);
        view = findViewById(R.id.surface_remote_viewer);
        // create touch listener components
        msgQueue = new LinkedBlockingQueue<>();
        uiHandler = new Handler(Looper.getMainLooper()) {
            @Override
            public void handleMessage(Message msg) {
                super.handleMessage(msg);
                connectionInfo.setText((String) msg.obj);
            }
        };
        eventSender = new EventServer(msgQueue, uiHandler);
        threadStarter = findViewById(R.id.threadStarter);
        threadStarter.setVisibility(View.INVISIBLE);
        // threadStarter.setOnClickListener((View v) -> {
        //     if (senderRunner == null) {
        //         senderRunner = new Thread(eventSender);
        //         senderRunner.start();
        //         threadStarter.setText("Stop server");
        //     } else {
        //         eventSender.close();
        //         try {
        //             senderRunner.join();
        //             threadStarter.setText("Start server");
        //             senderRunner = null;
        //         } catch (InterruptedException e) {
        //         }
        //     }
        // });

        //init WebRTC Signaling server
        this.initViews();
        this.peerWrapper = new PeerWrapper(this);
        this.webSocketServer = new WebServer((IPeer) this.peerWrapper);
        this.peerWrapper.setEmitter((Emitter)this.webSocketServer);
        this.webSocketServer.start();

        // initiate nearby connection manager
        connectionsClient = Nearby.getConnectionsClient(this);

        senderRunner = new Thread(eventSender);
        senderRunner.start();
        view.setOnTouchListener(
                (View v, MotionEvent e) -> {
                    e.setLocation(e.getX() / view.getWidth(), e.getY() / view.getHeight());
                    TouchEvent te = new TouchEvent(e.getX(), e.getY(), e.getAction(), e.getDownTime());
                    msgQueue.add(te);
                    return true;
                }
        );
    }

    /**
     * Checks whether the app has the required permissions to establish connections,
     * and then starts advertising to search for clients.
     */
    @Override
    protected void onStart() {
        super.onStart();
        connectionInfo.setText("Waiting for P2P Wifi connection");

        if (!hasPermissions(this, REQUIRED_PERMISSIONS) &&
                Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            requestPermissions(REQUIRED_PERMISSIONS, REQUEST_CODE_REQUIRED_PERMISSIONS);
        } else {
            Log.w(NEARBY_TAG, "Could not check permissions due to version");
        }

        startAdvertising();
    }

    public SurfaceViewRenderer getRender(){
        return remoteRender;
    }

    private void initViews(){
        remoteRender = findViewById(R.id.surface_remote_viewer);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        MenuInflater inflater = getMenuInflater();
        inflater.inflate(R.menu.main_menu, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        Intent intent;
        switch (item.getItemId()) {
            case R.id.p2pserver_activity:
                // skip since we use nearby
                // intent = new Intent(this,P2PActivityServer.class);
                // break;
                return super.onOptionsItemSelected(item);
            case R.id.webrtc_server_activity:
                intent = new Intent(this,WebRTCServerActivity.class);
                break;
            case R.id.event_grab_activity:
                intent = new Intent(MainActivity.this, EventGrabDemo.class);
                break;
            case R.id.event_sender_activity:
                intent = new Intent(MainActivity.this, EventSenderDemo.class);
                break;
            case R.id.build_info_activity:
                intent = new Intent(this, BuildInfoActivity.class);
                break;
            default:
                return super.onOptionsItemSelected(item);
        }

        startActivity(intent);
        return super.onOptionsItemSelected(item);
    }

    /**
     * Stops all advertising and connections from this server before calling super.onStop().
     */
    @Override
    protected void onStop() {
        connectionsClient.stopAdvertising();
        connectionsClient.stopAllEndpoints();
        super.onStop();
    }

    /**
     * Broadcast our presence using Nearby Connection so FlyInn users can find us.
     * Resets clientID and clientName first.
     */
    private void startAdvertising() {
        clientID = null;
        clientName = null;

        AdvertisingOptions advertisingOptions =
                new AdvertisingOptions.Builder().setStrategy(STRATEGY).build();

        connectionsClient.startAdvertising(serverName, "com.amos.flyinn",
                connectionLifecycleCallback, advertisingOptions)
                .addOnSuccessListener( (Void unused) -> {
                    // started advertising successfully
                    Log.i(NEARBY_TAG, "Started advertising " + serverName);
                    Toast.makeText(this, R.string.nearby_advertising_success,
                            Toast.LENGTH_SHORT).show();
                })
                .addOnFailureListener( (Exception e) -> {
                    // unable to advertise
                    Log.e(NEARBY_TAG, "Unable to start advertising " + serverName);
                    Toast.makeText(this, R.string.nearby_advertising_error,
                            Toast.LENGTH_LONG).show();
                    finish();
                });
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
                Toast.makeText(this, R.string.nearby_missing_permissions,
                        Toast.LENGTH_LONG).show();
                finish();
                return;
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
        return Build.MODEL + "_" + sb.toString();
    }
}
