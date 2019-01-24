package com.amos.server.mediadecoder;

import android.media.MediaCodec;
import android.util.Log;

public class OutputQueuer implements Runnable {
    private static final String TAG = "OutputQueuer";
    MediaCodec codec;

    public OutputQueuer(MediaCodec codec) {
        this.codec = codec;
    }
    @Override
    public void run() {
        MediaCodec.BufferInfo info = new MediaCodec.BufferInfo();
        int index;
        while (true) {
            index = codec.dequeueOutputBuffer(info, 10000);
            if (index >= 0)
            {
                // ByteBuffer output = decoder.getOutputBuffer(index);
                // Log.d(TAG, String.format("Output: %d", output.remaining()));
                codec.releaseOutputBuffer(index, true);
            }
            Log.i(TAG, String.format("dequeueOutputBuffer index = %d", index));
        }
    }
}
