package com.amos.server.mediadecoder;

import android.media.MediaCodec;
import android.util.Log;

import java.io.IOException;
import java.io.InputStream;
import java.nio.ByteBuffer;

public class InputQueuer implements Runnable {

    private static final String TAG = "InputQueuer";
    private MediaCodec codec;
    private InputStream stream;

    private final byte[] buffer = new byte[16384 * 16];
    private final ByteBuffer header = ByteBuffer.allocate(12);

    private long timestamp;
    private int size;

    InputQueuer(MediaCodec codec, InputStream stream) {
        this.codec = codec;
        this.stream = stream;
    }

    private void readHeader() {
        int result = -1;
        int num = 0;
        Log.d(TAG, "Reading header");
        header.rewind();
        while (num < 12) {
            try {
                result = this.stream.read();
            } catch(IOException ignored) {}
            if (result < 0) continue;
            header.put((byte)result);
            num++;
        }
        header.rewind();
        timestamp = header.getLong();
        size = header.getInt();
        Log.d(TAG, String.format("Read header: %d", size));
    }

    private void readData(){
        int offset = 0;
        int remaining = size;
        int read;
        Log.d(TAG, "Reading data");
        while (remaining > 0) {
            try {
                read = stream.read(buffer, offset, remaining);
                if (read > 0) {
                    offset += read;
                    remaining -= read;
                }
            } catch (IOException ignored) {}
        }
        Log.d(TAG, "Finished reading data");
    }

    @Override
    public void run() {
        //noinspection deprecation
        ByteBuffer[] inputBuffers = codec.getInputBuffers();
        int index;

        while (!Thread.interrupted()) {
            Log.d(TAG, "Queueing input buffer");
            index = codec.dequeueInputBuffer(10000);
            if (index < 0) continue;
            ByteBuffer inputBuffer = inputBuffers[index];

            readHeader();
            readData();
            inputBuffer.put(buffer, 0, size);
            codec.queueInputBuffer(index, 0, size, timestamp, 0);
        }
    }
}
