package com.amos.server.mediadecoder;

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

    private int readyState = 0;

    // public final MediaDecoder decoder = new MediaDecoder();
    private final DecoderThread decoder = new DecoderThread();

    private static final String TAG = "MediaDecoderController";

    private MediaDecoderController() {
    }

    public void registerNearby() {
        ServerConnection.getInstance().addHandle(Payload.Type.STREAM, this);
    }

    public void registerOutput(Surface surface) {
        decoder.setSurface(surface);
        readyState |= 2;
        if (readyState == 3) {
            Thread thread = new Thread(decoder);
            thread.start();
        }
    }

    public void network() {
        StrictMode.ThreadPolicy policy = new StrictMode.ThreadPolicy.Builder().permitAll().build();
        StrictMode.setThreadPolicy(policy);
        try {
            Socket sock = new Socket("192.168.2.100", 5551);
            sock.getInputStream();
            decoder.setInputStream(sock.getInputStream());
            decoder.start();
        } catch (IOException error) {
            Log.e(TAG, error.toString());
            error.printStackTrace();
        }
    }

    @Override
    public void receive(Payload payload) {
        Log.d(TAG, "Receiving payload");
        Payload.Stream stream = payload.asStream();
        if (stream == null) return;

        InputStream input = stream.asInputStream();
        decoder.setInputStream(input);
        readyState |= 1;
        if (readyState == 3) {
            // decoder.run();
            Thread thread = new Thread(decoder);
            thread.start();
        }
    }
}
