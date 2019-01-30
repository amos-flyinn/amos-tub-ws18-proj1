package com.amos.server.mediadecoder;

import android.media.MediaCodec;
import android.util.Log;

import java.nio.ByteBuffer;

public class OutputQueue implements Runnable {
    private static final String TAG = "OutputQueue";
    private static final boolean DEBUG = false;
    private MediaCodec codec;

    OutputQueue(MediaCodec codec) {
        this.codec = codec;
    }

    @Override
    public void run() {
        MediaCodec.BufferInfo info = new MediaCodec.BufferInfo();
        int index;
        while (!Thread.interrupted()) {
            index = codec.dequeueOutputBuffer(info, 10000);
            if (index >= 0)
            {
                if (DEBUG) {
                    ByteBuffer output = codec.getOutputBuffer(index);
                    Log.d(TAG, String.format("Output: %d", output.remaining()));
                }
                codec.releaseOutputBuffer(index, true);
            }
            if (DEBUG) Log.i(TAG, String.format("dequeueOutputBuffer index = %d", index));
        }
    }
}
