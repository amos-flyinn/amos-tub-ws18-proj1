package com.amos.server;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.graphics.Point;
import android.os.Bundle;
import android.util.Log;
import android.view.MotionEvent;
import android.view.SurfaceHolder;
import android.view.SurfaceView;
import android.view.View;
import android.widget.Toast;

import com.amos.server.eventsender.EventWriter;
import com.amos.server.nearby.ServerConnection;

import java.io.IOException;

public class ConnectedActivity extends Activity {

    SurfaceView surfaceView;
    SurfaceHolder surfaceHolder;

    /**
     * Connection singleton managing nearby connection
     */
    private ServerConnection connection;
    private EventWriter writer;

    private static final String TAG = "Connected";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_connected);

        connection = ServerConnection.getInstance();

        surfaceView = findViewById(R.id.surfaceView);
        surfaceView.getHolder().addCallback(new SurfaceHolder.Callback() {
            @Override
            public void surfaceCreated(SurfaceHolder sHolder) {
                surfaceHolder = sHolder;
            }

            @Override
            public void surfaceChanged(SurfaceHolder sHolder, int i, int i1, int i2) {
                Log.d(TAG, "Surface changed");
            }

            @Override
            public void surfaceDestroyed(SurfaceHolder sHolder) {
                surfaceHolder = null;
            }
        });

        transmitInputEvents();
    }

    /**
     * Send touch events over established connection
     */
    @SuppressLint("ClickableViewAccessibility")
    private void transmitInputEvents() {
        Log.d(TAG, "Trying to transmit input events");
        toast("Trying to transmit input events");
        try {
            writer = new EventWriter(connection.sendStream(), new Point(surfaceView.getWidth(), surfaceView.getHeight()));
        } catch (IOException ignored) {
        }
        surfaceView.setOnTouchListener((View v, MotionEvent event) -> {
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

}
