package com.amos.flyinn;

import android.content.Intent;
import android.graphics.Point;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;

import com.amos.flyinn.screenRecording.RecordingActivity;
import com.amos.flyinn.summoner.Daemon;
import com.amos.flyinn.wifimanager.WifiManager;

import java.io.IOException;

public class MainActivity extends AppCompatActivity {
    TextView connectionStatus;
    Button adbButton;
    Daemon adbDaemon;

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
            case R.id.wifip2p_activity:
                intent = new Intent(this,WifiP2PActivity.class);
                break;
            case R.id.adb_activity:
                intent = new Intent(this,ADBActivity.class);
                break;
            case R.id.webrtc_activity:
                intent = new Intent(this, WebRTCActivity.class);
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

    private Daemon createADBService(String addr) {
        Point p = new Point();
        getWindowManager().getDefaultDisplay().getRealSize(p);
        Daemon d = new Daemon(getApplicationContext(), addr, p);
        try {
            d.writeFakeInputToFilesystem();
            d.spawn_adb();
        } catch (Exception e) {
        }
        return d;
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        // Example of a call to a native method
        connectionStatus = findViewById(R.id.connectionStatus);
        adbButton = findViewById(R.id.adb_button);
        adbButton.setOnClickListener((View v) -> {
            if (adbDaemon == null) {
                try {
                    String addr = WifiManager.getInstance().getWifiReceiverP2P().getHostAddr();
                    adbDaemon = createADBService(addr);
                } catch (Exception e) {
                    connectionStatus.setText("Error starting ADB service");
                }
                adbButton.setText("Stop ADB Daemon");
            } else {
                // TODO Stop the adb daemon again
                adbButton.setText("Start ADB Daemon");
            }
        });
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
}
