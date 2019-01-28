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
import com.amos.server.mediadecoder.MediaDecoderController;
import com.amos.server.nearby.ServerConnection;

import java.io.IOException;


public class ConnectedActivity extends Activity {

    SurfaceView surfaceView;

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
        surfaceView.setRotation(270);
        // surfaceView.setSurfaceTextureListener(new TextureView.SurfaceTextureListener() {
        //     @Override
        //     public void onSurfaceTextureAvailable(SurfaceTexture surface, int width, int height) {
        //         Log.d(TAG, "Surface changed");
        //         Log.d(TAG, String.format("%d %d", surfaceView.getWidth(), surfaceView.getHeight()));
        //         transmitInputEvents();
        //         MediaDecoderController.getInstance().registerOutput(new Surface(surface));
        //         // MediaDecoderController.getInstance().network();

        //     }

        //     @Override
        //     public void onSurfaceTextureSizeChanged(SurfaceTexture surface, int width, int height) {

        //     }

        //     @Override
        //     public boolean onSurfaceTextureDestroyed(SurfaceTexture surface) {
        //         return false;
        //     }

        //     @Override
        //     public void onSurfaceTextureUpdated(SurfaceTexture surface) {
        //     }
        // });
        surfaceView.getHolder().addCallback(new SurfaceHolder.Callback() {
            @Override
            public void surfaceCreated(SurfaceHolder sHolder) {
            }

            @Override
            public void surfaceChanged(SurfaceHolder sHolder, int format, int width, int height) {
                Log.d(TAG, "Surface changed");
                MediaDecoderController.getInstance().registerOutput(sHolder.getSurface());
                // MediaDecoderController.getInstance().network();
                transmitInputEvents();
            }

            @Override
            public void surfaceDestroyed(SurfaceHolder sHolder) {}
        });
    }

    /**
     * Send touch events over established connection
     */
    @SuppressLint("ClickableViewAccessibility")
    private void transmitInputEvents() {
        if (!connection.isConnected()) {
            toast("Not connected via nearby.");
            return;
        }
        Log.d(TAG, "Trying to transmit input events");
        toast("Trying to transmit input events");
        try {
            writer = new EventWriter(
                    connection.sendStream(),
                    new Point(surfaceView.getWidth(), surfaceView.getHeight()),
                    getResources().getConfiguration().orientation
            );
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
