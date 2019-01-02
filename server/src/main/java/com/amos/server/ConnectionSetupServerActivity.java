package com.amos.server;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.support.annotation.Nullable;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.ProgressBar;
import android.widget.TextView;

import com.amos.server.eventsender.EventServer;
import com.amos.server.nearby.ServerConnection;
import com.amos.server.signaling.WebServer;
import com.amos.server.webrtc.PeerWrapper;
import com.amos.server.webrtc.SetupStates;
import com.amos.shared.TouchEvent;
import com.google.android.gms.nearby.Nearby;
import com.google.android.gms.nearby.connection.ConnectionsClient;
import com.google.android.gms.nearby.connection.Payload;

import org.webrtc.SurfaceViewRenderer;

import java.util.concurrent.BlockingQueue;

public class ConnectionSetupServerActivity extends Activity {

    private ProgressBar infiniteBar;
    private TextView progressText;

    private ServerConnection connection;

    TextView connectionInfo;
    Button threadStarter;
    Thread senderRunner;
    EventServer eventSender;
    BlockingQueue<TouchEvent> msgQueue;
    Handler uiHandler;
    SurfaceViewRenderer view;

    private WebServer webSocketServer;
    private PeerWrapper peerWrapper;
    private SurfaceViewRenderer remoteRender;

    private String endpointId;

    private static final String TAG = "ConnectionSetup";

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_connection_setup);
        infiniteBar = findViewById(R.id.infiniteBar);
        progressText = findViewById(R.id.progressText);
        view = findViewById(R.id.surface_remote_viewer);
        connectionInfo = findViewById(R.id.connectionInfo);
        connectionInfo.setVisibility(View.INVISIBLE);

        connection = ServerConnection.getInstance();
        connection.sendStream();
    }

    /**
     * This method returns the SurfaceViewRender instance that
     * the client is using for the screen capture streaming
     *
     * @return SurfaceViewRenderer return instance of SurfaceView component
     */
    public SurfaceViewRenderer getRender() {
        return remoteRender;
    }

    private void initViews() {
        remoteRender = findViewById(R.id.surface_remote_viewer);
    }


    private void restartApp() {
        Intent i = getBaseContext().getPackageManager()
                .getLaunchIntentForPackage(getBaseContext().getPackageName());
        i.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
        startActivity(i);
    }

    /**
     * This method should be use to update the TextView description state.
     * It will write a description about the current state of the WebRTC stream.
     * This method is going to handle all possible error states that the App could reach.
     * <p>
     * Please see {@link com.amos.server.webrtc.SetupStates} for the states list.
     *
     * @param state the identification number to identify correct or error states.
     */
    public void setStateText(int state) {

        AlertDialog.Builder builder;
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            builder = new AlertDialog.Builder(this, android.R.style.Theme_Material_Dialog_Alert);
        } else {
            builder = new AlertDialog.Builder(this);
        }
        builder.setIcon(android.R.drawable.ic_dialog_alert)
                .setCancelable(false)
                .setTitle("Setup error");

        builder.setNegativeButton("Restart app", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialogInterface, int i) {
                restartApp();
            }
        });

        switch (state) {

            case SetupStates.PERMISSIONS_FOR_SCREENCAST_DENIED:
                progressText.setVisibility(View.INVISIBLE);
                infiniteBar.setVisibility(View.INVISIBLE);
                builder.setMessage("Error getting permissions for screen capture");
                builder.show();
                break;

            case SetupStates.ASKING_PERMISSIONS:
                progressText.setText("Asking permissions for screen capture");
                break;

            case SetupStates.SETUP_SCREEN_CONNECTION:
                progressText.setText("Setup screen share connection with server");
                break;

            case SetupStates.LOCAL_DESCRIPTOR_CREATE:
                progressText.setText("Local descriptor created Successfully");
                break;

            case SetupStates.REMOTE_DESCRIPTOR_CREATE:
                progressText.setText("Remote descriptor created Successfully");
                break;

            case SetupStates.LOCAL_DESCRIPTOR_SETTED:
                progressText.setText("Local descriptor setted Successfully");
                break;

            case SetupStates.REMOTE_DESCRIPTOR_SETTED:
                progressText.setText("Remote descriptor setted Successfully");
                progressText.setVisibility(View.INVISIBLE);
                remoteRender.setVisibility(View.VISIBLE);
                break;

            case SetupStates.FAIL_CREATING_LOCAL_DESCRIPTOR:
                builder.setMessage("Creating local descriptor failed. Please restart the app");
                builder.show();
                progressText.setVisibility(View.INVISIBLE);
                infiniteBar.setVisibility(View.INVISIBLE);
                break;


            case SetupStates.FAIL_CREATING_REMOTE_DESCRIPTOR:
                builder.setMessage("Creating remote descriptor failed. Please restart the app");
                builder.show();
                progressText.setVisibility(View.INVISIBLE);
                infiniteBar.setVisibility(View.INVISIBLE);
                break;


            case SetupStates.FAIL_SETTED_LOCAL_DESCRIPTION:
                builder.setMessage("Setting local descriptor failed. Please restart the app");
                builder.show();
                progressText.setVisibility(View.INVISIBLE);
                infiniteBar.setVisibility(View.INVISIBLE);
                break;

            case SetupStates.FAIL_SETTED_REMOTE_DESCRIPTION:
                builder.setMessage("Setting remote descriptor failed. Please restart the app");
                builder.show();
                progressText.setVisibility(View.INVISIBLE);
                infiniteBar.setVisibility(View.INVISIBLE);
                break;


            case SetupStates.FAIL_SENDING_SESSION_DESCRIPTOR:
                builder.setMessage("Sending remote descriptor failed. Please restart the app");
                builder.show();
                progressText.setVisibility(View.INVISIBLE);
                infiniteBar.setVisibility(View.INVISIBLE);
                break;

            case SetupStates.ERROR_CONNECTING_SERVER:
                builder.setMessage("Error connecting server. Please restart the app and make sure you are connected");
                builder.show();
                progressText.setVisibility(View.INVISIBLE);
                infiniteBar.setVisibility(View.INVISIBLE);
                break;
        }
    }


}
