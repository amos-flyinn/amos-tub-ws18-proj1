package com.amos.server;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.media.MediaPlayer;
import android.os.Bundle;
import android.support.v4.util.SimpleArrayMap;
import android.view.SurfaceHolder;
import android.view.SurfaceView;

import com.google.android.gms.nearby.Nearby;
import com.google.android.gms.nearby.connection.ConnectionsClient;
import com.google.android.gms.nearby.connection.Payload;
import com.google.android.gms.nearby.connection.PayloadCallback;
import com.google.android.gms.nearby.connection.PayloadTransferUpdate;

import java.io.FileDescriptor;
import java.io.IOException;
import java.io.InputStream;
import android.view.SurfaceHolder.Callback;

// Sources:
// http://blogs.innovationm.com/nearby-api-post-connection-phase/

public class ScreensharingServer extends Activity {
    private MediaPlayer mediaPlayer;

    // Callback for every received byte
    private final PayloadCallback payloadCallback =
            new PayloadCallback() {
                private final SimpleArrayMap<Long, Payload> incomingStreamPayloads = new SimpleArrayMap<>();

                @Override
                public void onPayloadReceived(String endpointId, Payload payload) {
                    //triggered when the first byte of STREAM type payload is received.
                    if(payload.getType()==Payload.Type.STREAM) {
                        incomingStreamPayloads.put(payload.getId(), payload);
                    }
                }

                @Override
                public void onPayloadTransferUpdate(String endpointId, PayloadTransferUpdate update) {
                    if (update.getStatus() == PayloadTransferUpdate.Status.SUCCESS) {
                        //
                        Payload payload = incomingStreamPayloads.get(update.getPayloadId());
                        Payload.Stream is = payload.asStream();
                        InitMediaPlayer(is.asParcelFileDescriptor().getFileDescriptor());
                    }
                }
            };

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        // Set endpoint ID
        Intent i = getIntent();
        String endpointID = i.getStringExtra("endpointID");

        // Set payload callback
        ConnectionsClient connectionsClient=Nearby.getConnectionsClient(this);
        connectionsClient.acceptConnection(endpointID, payloadCallback);
    }

    // Setup media player
    private void InitMediaPlayer(FileDescriptor fd) {
        // Init media player
        mediaPlayer = new MediaPlayer();
        try {
            mediaPlayer.setDataSource(fd);
        } catch (IOException e) {
            e.printStackTrace();
        }

        try {
            mediaPlayer.prepare();
        } catch(Exception ex) {

        }
    }

    // Init view where the media player should be displayed
    private void InitMediaPlayerView() {
        // Apply surface to mediaplayer
        SurfaceView sv = (SurfaceView) findViewById(R.id.surface_remote_viewer);
        SurfaceHolder holder = sv.getHolder();
        holder.addCallback(new Callback(){
            @Override
            public void surfaceChanged(SurfaceHolder holder, int format, int width, int height) { }

            @Override
            public void surfaceCreated(SurfaceHolder holder) {
                mediaPlayer.setDisplay(holder);
                mediaPlayer.start();
            }

            @Override
            public void surfaceDestroyed(SurfaceHolder holder) {

            }
        });
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        if (mediaPlayer != null) {
            mediaPlayer.release();
            mediaPlayer = null;
        }
    }
}
