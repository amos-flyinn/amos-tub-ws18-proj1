package com.amos.flyinn;

import android.Manifest;
import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Point;
import android.media.projection.MediaProjectionManager;
import android.os.Bundle;
import android.provider.Settings;
import android.support.annotation.Nullable;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import com.amos.flyinn.screenRecording.RecordingActivity;
import com.amos.flyinn.signaling.ClientSocket;
import com.amos.flyinn.signaling.Emitter;
import com.amos.flyinn.summoner.Daemon;
import com.amos.flyinn.webrtc.PeerWrapper;

import org.webrtc.PeerConnection;
import org.webrtc.SurfaceViewRenderer;

import java.net.URI;
import java.util.concurrent.TimeUnit;

public class MainActivity extends AppCompatActivity {
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
    public boolean onCreateOptionsMenu(Menu menu) {
        MenuInflater inflater = getMenuInflater();
        inflater.inflate(R.menu.main_menu, menu);

        return true;
    }


    private void initWebRTCScreenCapture() {
        try {
            this.clientSocket.connectBlocking(1, TimeUnit.MINUTES);
            this.peerWrapper.beginTransactionWithOffer();
        } catch (InterruptedException e) {
            e.printStackTrace();
            Log.d("MainActivity", "initWebRTCScreenCapture: " + "Error traying to connect to the server");
        }

    }

    private void configForWebRTC(Intent permissionsScreenCapture) {
        this.peerWrapper = new PeerWrapper(this, permissionsScreenCapture);
        this.clientSocket = new ClientSocket(URI.create("ws://192.168.49.1:8080"), this.peerWrapper);
        this.peerWrapper.setEmitter((Emitter) this.clientSocket);

    }


    private void initScreenCapturePermissions() {
        mProjectionManager = (MediaProjectionManager) getSystemService
                (Context.MEDIA_PROJECTION_SERVICE);
        startActivityForResult(mProjectionManager.createScreenCaptureIntent(), 42);
    }

    public SurfaceViewRenderer getRender() {
        return render;
    }

    private void initViewsWebRTC() {
        render = findViewById(R.id.surface_local_viewer);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        Intent intent;
        switch (item.getItemId()) {
            case R.id.search_connections:
                // intent = new Intent(this,WifiP2PActivity.class);
                intent = new Intent(this, NearbyConnectionActivity.class);
                break;
            case R.id.adb_activity:
                intent = new Intent(this, ADBActivity.class);
                break;
            case R.id.webrtc_activity:
                intent = new Intent(this, WebRTCActivity.class);
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

    // Used to load the 'native-lib' library on application startup.
    static {
        System.loadLibrary("native-lib");
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


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        this.buttonInit = (Button) this.findViewById(R.id.webrtc_init);
        this.buttonInit.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                initWebRTCScreenCapture();
            }
        });

        // Example of a call to a native method
        connectionStatus = findViewById(R.id.connectionStatus);
        String addr;
        try {
            addr = "127.0.0.1";
            while (ContextCompat.checkSelfPermission(this, Manifest.permission.WRITE_EXTERNAL_STORAGE) != PackageManager.PERMISSION_GRANTED) {
                ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.WRITE_EXTERNAL_STORAGE, Manifest.permission.READ_EXTERNAL_STORAGE}, 0);
            }
            adbDaemon = createADBService(addr);
        } catch (Exception e) {
        }

        this.initViewsWebRTC();
        this.initScreenCapturePermissions();
    }


    /**
     * A native method that is implemented by the 'native-lib' native library,
     * which is packaged with this application.
     */
    public native String stringFromJNI();

    public void toScreenActivityOnClick(View view) {
        Intent intent = new Intent(this, RecordingActivity.class);
        startActivity(intent);
    }


    @Override
    public void onResume() {
        super.onResume();
        checkForDebuggingMode();
    }

    private void checkForDebuggingMode() {
        if (Settings.Secure.getInt(this.getContentResolver(), Settings.Global.ADB_ENABLED, 0) != 1) {
            AlertDialog alertDialog = new AlertDialog.Builder(MainActivity.this).create();
            alertDialog.setTitle("Missing settings");
            alertDialog.setMessage("To use FlyInn please enable the debugging mode and USB debugging.\n" +
                    "Mostly enabling the debugging mode works with tapping multiple times the 'Software Build number' label.");
            alertDialog.setButton(AlertDialog.BUTTON_NEUTRAL, "OK",
                    new DialogInterface.OnClickListener() {
                        public void onClick(DialogInterface dialog, int which) {
                            try {
                                startActivity(new Intent(android.provider.Settings.ACTION_APPLICATION_DEVELOPMENT_SETTINGS));
                                Toast.makeText(MainActivity.this, "To use FlyInn please enable USB debugging.", Toast.LENGTH_SHORT).show();
                            } catch (Exception ex) {
                                startActivity(new Intent(Settings.ACTION_DEVICE_INFO_SETTINGS));
                            }

                            dialog.dismiss();
                        }
                    });
            alertDialog.show();
        }
    }


    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        if (resultCode != RESULT_OK) {
            Toast.makeText(this, "Screen Cast Permission Denied", Toast.LENGTH_SHORT).show();
            return;
        }
        this.configForWebRTC(data);
    }
}
