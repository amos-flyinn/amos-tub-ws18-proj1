package com.amos.server;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.media.MediaPlayer;
import android.os.Bundle;
import android.os.Environment;
import android.os.ParcelFileDescriptor;
import android.support.v4.util.SimpleArrayMap;
import android.util.Log;
import android.view.SurfaceHolder;
import android.view.SurfaceView;

import com.google.android.gms.nearby.Nearby;
import com.google.android.gms.nearby.connection.ConnectionsClient;
import com.google.android.gms.nearby.connection.Payload;
import com.google.android.gms.nearby.connection.PayloadCallback;
import com.google.android.gms.nearby.connection.PayloadTransferUpdate;

import java.io.File;
import java.io.FileDescriptor;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.nio.charset.StandardCharsets;

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

                    Log.d("ScreensharingServer", "onPayloadReceived: Received from here :  " + endpointId);

                    if(payload.getType() == Payload.Type.BYTES)
                    {
                        incomingStreamPayloads.put(payload.getId(),payload);
                    }

                    if (payload.getType() == Payload.Type.FILE) {
                        Log.d("ScreensharingServer", "onPayloadReceived: Received with id  :  " + payload.getId());
                        incomingStreamPayloads.put(payload.getId(), payload);
                    }
                }

                @Override
                public void onPayloadTransferUpdate(String endpointId, PayloadTransferUpdate update) {

                    Log.d("onPayloadTransferUpdate", "onPayloadReceived: Received from here :  " + endpointId);
                    if (update.getStatus() == PayloadTransferUpdate.Status.SUCCESS) {
                        //


                        Log.d("ReceivedTransfer", "onPayloadTransferUpdate: The id of payload is : " + update.getPayloadId());

                        Payload payload = incomingStreamPayloads.get(update.getPayloadId());
                        //Payload.Stream is = payload.asFile();

                        if(payload.getType() == Payload.Type.BYTES)
                        {
                            String text = new String(payload.asBytes(), StandardCharsets.UTF_8);
                            Log.d("ReceivedPayloadBytes", "onPayloadTransferUpdate: " + text);
                        }

                        if(payload.getType() == Payload.Type.FILE)
                        {
                            //File sendedFile = payload.asFile().asParcelFileDescriptor();
                            ParcelFileDescriptor sendedFile = payload.asFile().asParcelFileDescriptor();

                            Log.d("ParcelReader", "onPayloadTransferUpdate: " + (sendedFile == null));

                            InputStream fileStream = new FileInputStream(sendedFile.getFileDescriptor());



                            try {
                                File tempFileVideo = File.createTempFile("GettedFile",".mp4",Environment
                                         .getExternalStoragePublicDirectory(Environment
                                                 .DIRECTORY_DOWNLOADS));

                                OutputStream newDatabase = new FileOutputStream(tempFileVideo);

                                byte[] buffer = new byte[1024];
                                int length;

                                while((length = fileStream.read(buffer)) > 0)
                                {
                                    newDatabase.write(buffer, 0, length);
                                }

                                newDatabase.flush();
                                fileStream.close();
                                newDatabase.close();

                                String path = Environment
                                        .getExternalStoragePublicDirectory(Environment
                                                .DIRECTORY_DOWNLOADS) + "/" + tempFileVideo.getName();

                                Log.d("NameReaderoutput", "onPayloadTransferUpdate: " + path);

                                InitMediaPlayer(((FileInputStream) fileStream).getFD(),path);

                            } catch (IOException e) {
                                e.printStackTrace();
                            }



                            //Log.d("FileReceivedPayload", "onPayloadTransferUpdate: The path is :  " + sendedFile.getAbsolutePath());


                        }





                        //InitMediaPlayer(is.asParcelFileDescriptor().getFileDescriptor());
                    }
                }
            };

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_screen_server);

        // Set endpoint ID
        Intent i = getIntent();
        String endpointID = i.getStringExtra("endpointID");

        // Set payload callback
        ConnectionsClient connectionsClient = Nearby.getConnectionsClient(this);
        connectionsClient.acceptConnection(endpointID, payloadCallback);
    }

    // Setup media player
    private void InitMediaPlayer(FileDescriptor fd,String path) {
        // Init media player
        mediaPlayer = new MediaPlayer();
        mediaPlayer.setVolume(50,50);
        try {
            mediaPlayer.setDataSource(path);
        } catch (IOException e) {
            e.printStackTrace();
        }

        try {
            mediaPlayer.prepareAsync();
            InitMediaPlayerView();
        } catch (Exception ex) {
            ex.printStackTrace();
        }
    }

    // Init view where the media player should be displayed
    private void InitMediaPlayerView() {
        // Apply surface to mediaplayer
        SurfaceView sv = (SurfaceView) findViewById(R.id.surfaceView);
        SurfaceHolder holder = sv.getHolder();
        holder.addCallback(new Callback() {
            @Override
            public void surfaceChanged(SurfaceHolder holder, int format, int width, int height) {
            }

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
