package com.amos.server.mediadecoder;

import android.media.MediaCodec;
import android.media.MediaFormat;
import android.os.StrictMode;
import android.util.Log;
import android.view.Surface;

import com.amos.server.nearby.HandlePayload;
import com.amos.server.nearby.ServerConnection;
import com.google.android.gms.nearby.connection.Payload;

import java.io.IOException;
import java.io.InputStream;
import java.net.Socket;

public class MediaDecoderController implements HandlePayload {
    private static final MediaDecoderController ourInstance = new MediaDecoderController();

    public static MediaDecoderController getInstance() {
        return ourInstance;
    }

    private Surface surface = null;
    private InputStream input = null;

    private static final String TAG = "MediaDecoderController";

    private MediaDecoderController() {
    }

    public void registerNearby() {
        ServerConnection.getInstance().addHandle(Payload.Type.STREAM, this);
    }

    public void registerOutput(Surface surface) {
        this.surface = surface;
        run();
    }

    /**
     * Test function enabling us to read a raw h264 tcp stream.
     */
    @SuppressWarnings("unused")
    public void network() {
        StrictMode.ThreadPolicy policy = new StrictMode.ThreadPolicy.Builder().permitAll().build();
        StrictMode.setThreadPolicy(policy);
        try {
            Socket sock = new Socket("192.168.2.100", 5551);
            sock.getInputStream();
        } catch (IOException error) {
            Log.e(TAG, error.toString());
            error.printStackTrace();
        }
    }

    private void run() {
        if (surface == null) return;
        if (input == null) return;
        try {
            MediaCodec codec = MediaCodec.createDecoderByType("video/avc");
            MediaFormat format = MediaFormat.createVideoFormat("video/avc", 480, 800);
            codec.configure(format, surface, null, 0);
            codec.start();
            InputQueuer inputQueuer = new InputQueuer(codec, input);
            OutputQueuer outputQueuer = new OutputQueuer(codec);
            new Thread(inputQueuer).start();
            new Thread(outputQueuer).start();
        } catch (IOException ignored) {}
    }

    @Override
    public void receive(Payload payload) {
        Log.d(TAG, "Receiving streaming payload");
        Payload.Stream stream = payload.asStream();
        if (stream == null){
            Log.e(TAG, "No stream found");
            return;
        }
        input = stream.asInputStream();
        run();
    }
}
