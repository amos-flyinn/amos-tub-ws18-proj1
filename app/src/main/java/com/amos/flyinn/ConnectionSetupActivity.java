package com.amos.flyinn;

import android.Manifest;
import android.app.Activity;
import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Point;
import android.media.projection.MediaProjectionManager;
import android.os.Build;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import com.amos.flyinn.webrtc.SetupStates;
import com.amos.flyinn.signaling.ClientSocket;
import com.amos.flyinn.signaling.Emitter;
import com.amos.flyinn.summoner.Daemon;
import com.amos.flyinn.webrtc.PeerWrapper;
import com.amos.flyinn.wifimanager.WifiManager;

import org.webrtc.PeerConnection;
import org.webrtc.SurfaceViewRenderer;

import java.net.URI;
import java.util.Set;
import java.util.concurrent.TimeUnit;

public class ConnectionSetupActivity extends AppCompatActivity {

    private ProgressBar infiniteBar;
    private TextView progressText;

    TextView connectionStatus;
    Button adbButton;
    Daemon adbDaemon;
    private MediaProjectionManager mProjectionManager;
    private PeerConnection localConnection;
    private ClientSocket clientSocket;
    private PeerWrapper peerWrapper;
    private SurfaceViewRenderer render;
    private Button buttonInit;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_connection_setup);
        infiniteBar = (ProgressBar) findViewById(R.id.infiniteBar);
        progressText = (TextView) findViewById(R.id.progressText);

        String addr;
        try {
            addr = WifiManager.getInstance().getWifiReceiverP2P().getHostAddr();
            while (ContextCompat.checkSelfPermission(this, Manifest.permission.WRITE_EXTERNAL_STORAGE) != PackageManager.PERMISSION_GRANTED) {
                ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.WRITE_EXTERNAL_STORAGE, Manifest.permission.READ_EXTERNAL_STORAGE}, 0);
            }
            adbDaemon = createADBService(addr);
        } catch (Exception e) {
        }


        this.initViewsWebRTC();
        this.initScreenCapturePermissions();
    }





    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {

        if (resultCode != RESULT_OK) {
            Toast.makeText(this,
                    "Screen Cast Permission Denied", Toast.LENGTH_SHORT).show();
            setStateText(SetupStates.PERMISSIONS_FOR_SCREENCAST_DENIED);
            return;

        }

        setStateText(1);
        this.configForWebRTC(data);
        this.initWebRTCScreenCapture();

    }


    private void initWebRTCScreenCapture() {
        try {
            boolean connected = this.clientSocket.connectBlocking(1, TimeUnit.MINUTES);
            if(!connected)
            {
                this.setStateText(SetupStates.ERROR_CONNECTING_SERVER);
            }
            else
            {
                this.peerWrapper.beginTransactionWithOffer();
            }

        } catch (InterruptedException e) {
            e.printStackTrace();
            Log.d("MainActivity", "initWebRTCScreenCapture: " + "Error traying to connect to the server");
        }

    }

    public SurfaceViewRenderer getRender() {
        return render;
    }

    private void initViewsWebRTC() {
        render = findViewById(R.id.surface_local_viewer);
    }


    private void configForWebRTC(Intent permissionsScreenCapture) {
        this.peerWrapper = new PeerWrapper(this, permissionsScreenCapture);
        this.clientSocket = new ClientSocket(URI.create("ws://192.168.49.1:8080"), this.peerWrapper);
        this.peerWrapper.setEmitter((Emitter) this.clientSocket);

    }

    private void initScreenCapturePermissions() {
        mProjectionManager = (MediaProjectionManager) getSystemService
                (Context.MEDIA_PROJECTION_SERVICE);
        setStateText(0);
        startActivityForResult(mProjectionManager.createScreenCaptureIntent(), 42);


    }


    private void restarAPP(){
        Intent i = getBaseContext().getPackageManager()
                .getLaunchIntentForPackage( getBaseContext().getPackageName() );
        i.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
        startActivity(i);
    }

    protected Daemon createADBService(String addr) {
        Point p = new Point();
        getWindowManager().getDefaultDisplay().getRealSize(p);
        Daemon d = new Daemon(getApplicationContext(), addr, p);
        try {
            d.writeFakeInputToFilesystem();
            d.spawn_adb();
        } catch (Exception e) {
            Log.d("AppDaemon", e.toString());
        }
        return d;
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
