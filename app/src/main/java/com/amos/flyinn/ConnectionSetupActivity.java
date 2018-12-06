package com.amos.flyinn;

import android.Manifest;
import android.annotation.SuppressLint;
import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Point;
import android.media.projection.MediaProjectionManager;
import android.net.wifi.WifiManager;
import android.net.wifi.p2p.WifiP2pManager;
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

import com.amos.flyinn.signaling.ClientSocket;
import com.amos.flyinn.signaling.Emitter;
import com.amos.flyinn.summoner.ADBService;
import com.amos.flyinn.summoner.Daemon;
import com.amos.flyinn.webrtc.PeerWrapper;
import com.amos.flyinn.webrtc.SetupStates;
import com.amos.flyinn.wificonnector.WifiConnectorSingelton;
import com.amos.flyinn.wificonnector.WifiStateMachine;

import org.webrtc.PeerConnection;
import org.webrtc.SurfaceViewRenderer;

import java.lang.reflect.Method;
import java.net.URI;
import java.util.concurrent.TimeUnit;

/**
 * <h1>ConnectionSetup</h1>
 * <p>
 * The ConnectionSetup Activity is responsible to Setup the connection between the Client and the Server app.
 * This class is also responsible to inform the user about possible problems that could happen in the WebRTC stream negotiation
 * or in the ADB server connection.
 * It also tries to handle all possible error states giving the user some options to proceed in failure cases.
 * </p>
 */

public class ConnectionSetupActivity extends AppCompatActivity {
    private ProgressBar infiniteBar;
    private TextView progressText;
    private Button switchToHomeScreenButton;
    private Button closeConnectionButton;
    private TextView connectedMessage;
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
        infiniteBar = findViewById(R.id.infiniteBar);
        progressText = findViewById(R.id.progressText);
        closeConnectionButton = findViewById(R.id.close_connection);
        switchToHomeScreenButton = findViewById(R.id.switch_home_screen);
        connectedMessage = findViewById(R.id.connected_message);

        //Giving callback to the close connection button
        closeConnectionButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                closeConnection();
            }
        });

        //Giving callback to the switch to home button
        switchToHomeScreenButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                swtichToHomeScreen();
            }
        });


        String addr;
        //Preparing and initializing the ADB service to listen for incoming connections.
        try {
            addr = "192.168.49.1";
            WifiConnectorSingelton wifiConnector = WifiConnectorSingelton.getInstance();
            WifiStateMachine stateMachine = wifiConnector.getWifiReceiverP2P();
            Log.d("IP", stateMachine.getHostAddr());
            while (ContextCompat.checkSelfPermission(this, Manifest.permission.WRITE_EXTERNAL_STORAGE) != PackageManager.PERMISSION_GRANTED) {
                ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.WRITE_EXTERNAL_STORAGE, Manifest.permission.READ_EXTERNAL_STORAGE}, 0);
            }
            adbDaemon = createADBService(addr);
        } catch (Exception e) {
            e.printStackTrace();
        }

        //Init components for WebRTC and ask permissions for Screen capture functionalities.
        this.initViewsWebRTC();
        this.initScreenCapturePermissions();
    }

    /**
     * Method to minimize the app and go to the home screen
     */
    public void swtichToHomeScreen() {
        Intent startMain = new Intent(Intent.ACTION_MAIN);
        startMain.addCategory(Intent.CATEGORY_HOME);
        startMain.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
        startActivity(startMain);
    }

    /**
     * This method is going to be called after the screen capture permission is asked.
     * It gives the permissions necessary for the WebRTC screen capture to work
     *
     * @param requestCode the permission code for the Android permission
     * @param resultCode  the result of the operation
     * @param data        information about the screen capture permissions
     */
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
            if (!connected) {
                boolean retry = true;
                int counter = 0;
                while (retry) {
                    boolean retryReconnect = this.clientSocket.reconnectBlocking();
                    if (retryReconnect) {
                        retry = false;
                    }

                    if (counter == 4) {
                        retry = false;
                        break;
                    }
                    counter++;
                }
                if (counter == 4) {
                    this.setStateText(SetupStates.ERROR_CONNECTING_SERVER);
                }
            } else {
                this.peerWrapper.beginTransactionWithOffer();
            }
        } catch (InterruptedException e) {
            e.printStackTrace();
            Log.d("MainActivity", "initWebRTCScreenCapture: " + "Error traying to connect to the server");
        }
    }

    /**
     * This method returns the SurfaceViewRender instance that
     * the client is using for the screen capture streaming
     *
     * @return SurfaceViewRenderer return instance of SurfaceView component
     */
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


    private void restarAPP() {
        Intent i = getBaseContext().getPackageManager()
                .getLaunchIntentForPackage(getBaseContext().getPackageName());
        i.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
        startActivity(i);
    }

    /**
     * This method is to create an ADB service
     *
     * @param addr the address where the ADB service is going to listen for connections
     * @return returns Daemon service to work with the ADB service
     */
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


    private void closeConnection() {
        peerWrapper.closeConnection();
        this.render.clearImage();
        this.stopService(new Intent(this, ADBService.class));

        WifiP2pManager mManager = (WifiP2pManager) getSystemService(Context.WIFI_P2P_SERVICE);
        WifiP2pManager.Channel mChannel = mManager.initialize(this, getMainLooper(), null);
        try {
            @SuppressLint("WifiManagerLeak")
            WifiManager wifiManager = (WifiManager) getSystemService(Context.WIFI_SERVICE);
            wifiManager.setWifiEnabled(false);
            Thread.sleep(100);
            wifiManager.setWifiEnabled(true);
        } catch (Exception e) {
        }

        try {
            Method[] methods = WifiP2pManager.class.getMethods();
            for (int i = 0; i < methods.length; i++) {
                if (methods[i].getName().equals("deletePersistentGroup")) {
                    // Delete any persistent group
                    for (int netid = 0; netid < 32; netid++) {
                        methods[i].invoke(mManager, mChannel, netid, null);
                    }
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }

        finish();
    }


    /**
     * This method should be use to update the TextView description state.
     * It will write a description about the current state of the WebRTC stream.
     * This method is going to handle all possible error states that the App could reach.
     * <p>
     * Please see {@link com.amos.flyinn.webrtc.SetupStates} for the states list.
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
                infiniteBar.setVisibility(View.INVISIBLE);
                connectedMessage.setVisibility(View.VISIBLE);
                switchToHomeScreenButton.setVisibility(View.VISIBLE);
                closeConnectionButton.setVisibility(View.VISIBLE);
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
