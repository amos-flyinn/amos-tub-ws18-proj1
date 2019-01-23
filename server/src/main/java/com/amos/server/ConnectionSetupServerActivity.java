package com.amos.server;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.content.Intent;
import android.graphics.Point;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.util.Log;
import android.view.MotionEvent;
import android.view.View;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import com.amos.server.eventsender.EventWriter;
import com.amos.server.nearby.ConnectCallback;
import com.amos.server.nearby.ServerConnection;

import org.webrtc.SurfaceViewRenderer;

import java.io.IOException;

public class ConnectionSetupServerActivity extends Activity {

    private ProgressBar infiniteBar;
    private TextView progressText;

    /**
     * Connection singleton managing nearby connection
     */
    private ServerConnection connection;
    private EventWriter writer;

    TextView connectionInfo;
    SurfaceViewRenderer view;

    private static final String TAG = "ConnectionSetup";

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_connection_setup);
        Thread.setDefaultUncaughtExceptionHandler(new ExceptionHandler(this));

        infiniteBar = findViewById(R.id.infiniteBar);
        progressText = findViewById(R.id.progressText);
        view = findViewById(R.id.surface_remote_viewer);
        connectionInfo = findViewById(R.id.connectionInfo);
        connectionInfo.setVisibility(View.INVISIBLE);

        connection = ServerConnection.getInstance();

        // only correct actions will be processed
        Intent intent = getIntent();
        if ("connect".equals(intent.getAction())) {
            buildConnection(intent.getStringExtra("name"));
        } else {
            toInitialActivity();
        }
    }

    private void toInitialActivity() {
        Intent intent = new Intent(this, ConnectToClientActivity.class);
        startActivity(intent);
    }

    /**
     * Create connection to the given destination server
     */
    private void buildConnection(String name) {
        setProgressText("Connecting to " + name);
        connection.connectTo(name, new ConnectCallback() {
            @Override
            public void success() {
                Log.d(TAG, "Successfully connected to " + name);
                toast(String.format("Successfully connected to %s", name));
                infiniteBar.setVisibility(View.INVISIBLE);
                progressText.setVisibility(View.INVISIBLE);
                view.setVisibility(View.VISIBLE);
                connectionInfo.setVisibility(View.VISIBLE);
                transmitInputEvents();
            }

            @Override
            public void failure() {
                Log.d(TAG, "Failed to connect to " + name);
                toast(String.format("Failed to connect to %s", name));
                toInitialActivity();
            }
        });
    }

    /**
     * Send touch events over established connection
     */
    @SuppressLint("ClickableViewAccessibility")
    private void transmitInputEvents() {
        Log.d(TAG, "Trying to transmit input events");
        toast("Trying to transmit input events");
        try {
            writer = new EventWriter(connection.sendStream(), new Point(view.getWidth(), view.getHeight()));
        } catch (IOException ignored) {
        }
        view.setOnTouchListener((View v, MotionEvent event) -> {
            Log.d(TAG, event.toString());
            if (writer != null) {
                try {
                    writer.write(event);
                } catch (IOException ignored) {
                    Log.d(TAG, "Failed to write touch event to EventWriter");
                }
            }
            return true;
        });
    }

    private void toast(String message) {
        Toast toast = Toast.makeText(this, message, Toast.LENGTH_SHORT);
        toast.show();
    }

    private void setProgressText(String message) {
        progressText.setText(message);
    }
}
