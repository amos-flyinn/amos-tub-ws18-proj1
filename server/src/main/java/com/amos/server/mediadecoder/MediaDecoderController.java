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

    private Surface surface = null;
    private InputStream input = null;

    private Thread inputThread, outputThread;
    private MediaCodec codec;

    private static final String TAG = "MediaDecoderController";

    public static MediaDecoderController getInstance() {
        return ourInstance;
    }

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
            codec = MediaCodec.createDecoderByType("video/avc");
            MediaFormat format = MediaFormat.createVideoFormat("video/avc", 800, 480);
            format.setInteger("KEY_ROTATION", 180);
            codec.configure(format, surface, null, 0);
            codec.start();
            InputQueuer inputQueuer = new InputQueuer(codec, input);
            OutputQueuer outputQueuer = new OutputQueuer(codec);
            inputThread = new Thread(inputQueuer);
            outputThread = new Thread(outputQueuer);
            inputThread.start();
            outputThread.start();
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

    public void reset(){
        if (inputThread != null) {
            try {
                inputThread.interrupt();
                inputThread.join();
            } catch (InterruptedException ignored) {}
        }
        if (outputThread != null) {
            try {
                outputThread.interrupt();
                outputThread.join();
            } catch (InterruptedException ignored) {}
        }
        if (input != null) {
            try {
                input.close();
            } catch (IOException ignored){} finally {input = null;}
        }
        codec.stop();
        codec.reset();
        surface = null;
    }
}
