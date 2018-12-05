package com.amos.server;

import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.os.Message;
import android.support.annotation.Nullable;
import android.support.v7.app.AppCompatActivity;
import android.view.MotionEvent;
import android.view.View;
import android.widget.Button;
import android.widget.ProgressBar;
import android.widget.TextView;

import com.amos.server.eventsender.EventServer;
import com.amos.server.signaling.Emitter;
import com.amos.server.signaling.WebServer;
import com.amos.server.webrtc.IPeer;
import com.amos.server.webrtc.PeerWrapper;
import com.amos.server.webrtc.SetupStates;
import com.amos.shared.TouchEvent;

import org.webrtc.PeerConnection;
import org.webrtc.SurfaceViewRenderer;

import java.util.concurrent.BlockingQueue;
import java.util.concurrent.LinkedBlockingQueue;

public class ConnectionSetupServerActivity extends AppCompatActivity {

    private ProgressBar infiniteBar;
    private TextView progressText;

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

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_connection_setup);
        infiniteBar = (ProgressBar) findViewById(R.id.infiniteBar);
        progressText = (TextView) findViewById(R.id.progressText);
        view = findViewById(R.id.surface_remote_viewer);
        connectionInfo = (TextView) findViewById(R.id.connectionInfo);
        connectionInfo.setVisibility(View.INVISIBLE);

        //setStateText(SetupStates.LOCAL_DESCRIPTOR_CREATE);
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




        //init WebRTC Signaling server
        this.initViews();
        this.peerWrapper = new PeerWrapper(this);
        this.webSocketServer = new WebServer((IPeer) this.peerWrapper);
        this.peerWrapper.setEmitter((Emitter)this.webSocketServer);
        this.webSocketServer.start();


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

    public SurfaceViewRenderer getRender(){
        return remoteRender;
    }

    private void initViews(){
        remoteRender = findViewById(R.id.surface_remote_viewer);
    }



    private void restarAPP(){
        Intent i = getBaseContext().getPackageManager()
                .getLaunchIntentForPackage( getBaseContext().getPackageName() );
        i.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
        startActivity(i);
    }


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

        builder.setNegativeButton("Restar app", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialogInterface, int i) {
                restarAPP();
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
