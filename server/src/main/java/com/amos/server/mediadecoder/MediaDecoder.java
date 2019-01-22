package com.amos.server.mediadecoder;

import android.media.MediaCodec;
import android.media.MediaCodecInfo;
import android.media.MediaCodecList;
import android.media.MediaFormat;
import android.util.Log;
import android.view.Surface;

import java.io.IOException;
import java.nio.ByteBuffer;

public class MediaDecoder implements Runnable {
    MediaCodec codec;
    MediaInputStream inputStream;

    final static String TAG = "MediaDecoder";

    public MediaDecoder() {
    }

    private static MediaCodecInfo selectCodec(String mimeType) {
        int numCodecs = MediaCodecList.getCodecCount();
        for (int i = 0; i < numCodecs; i++) {
            MediaCodecInfo codecInfo = MediaCodecList.getCodecInfoAt(i);
            if (codecInfo.isEncoder()) {
                continue;
            }
            Log.d(TAG, codecInfo.getName());
            String[] types = codecInfo.getSupportedTypes();
            for (int j = 0; j < types.length; j++) {
                if (types[j].equalsIgnoreCase(mimeType)) {
                    return codecInfo;
                }
            }
        }
        return null;
    }

    void setOutput(Surface surface) {
        // String mimetype = "video/x-vnd.on2.vp9";
        String mimetype = "video/avc";
        MediaCodecInfo info = selectCodec(mimetype);

        Log.e(TAG, info.getName());
        MediaCodecInfo.VideoCapabilities videoCapabilities = info.getCapabilitiesForType(mimetype).getVideoCapabilities();
        for (int i : info.getCapabilitiesForType(mimetype).colorFormats) {
            Log.d(TAG, String.format("%d", i));
        }
        Log.d(TAG, videoCapabilities.getSupportedHeights().toString());
        Log.d(TAG, videoCapabilities.getSupportedWidths().toString());
        Log.d(TAG, videoCapabilities.getSupportedFrameRatesFor(200, 200).toString());
        Log.d(TAG, videoCapabilities.getBitrateRange().toString());

        // MediaFormat format = new MediaFormat();
        // format.setString(MediaFormat.KEY_MIME, "video/avc");
        // format.setInteger(MediaFormat.KEY_WIDTH, 480);
        // format.setInteger(MediaFormat.KEY_HEIGHT, 800);
        // format.setLong(MediaFormat.KEY_REPEAT_PREVIOUS_FRAME_AFTER, 1_000_000 * 6 / 60);

        MediaFormat format = MediaFormat.createVideoFormat(mimetype, 800, 480);
        // format.setInteger(MediaFormat.KEY_COLOR_FORMAT, MediaCodecInfo.CodecCapabilities.COLOR_FormatSurface);
        format.setInteger(MediaFormat.KEY_COLOR_FORMAT, MediaCodecInfo.CodecCapabilities.COLOR_FormatYUV420Flexible);
        // format.setInteger(MediaFormat.KEY_BIT_RATE, 128_000);
        format.setInteger(MediaFormat.KEY_FRAME_RATE, 60);
        // format.setInteger(MediaFormat.KEY_I_FRAME_INTERVAL, 1);

        Log.e(TAG, format.toString());

        try {
            // MediaCodecList list = new MediaCodecList(MediaCodecList.REGULAR_CODECS);
            // String fmt = list.findDecoderForFormat(format);
            // if (fmt != null) {
            //     Log.d(TAG, "Format found: " + fmt);
            //     codec = MediaCodec.createByCodecName(fmt);
            //     codec.configure(format, surface, null, 0);
            // } else {
            //     Log.d(TAG, "Format not found");
            // }
            // codec = MediaCodec.createDecoderByType("video/avc");
            codec = MediaCodec.createByCodecName(info.getName());
            codec.configure(format, surface, null, 0);
        } catch (IOException error) {
            Log.e(TAG, "Failed to configure codec");
        }
    }

    void setInput(MediaInputStream is) {
        inputStream = is;
    }

    @Override
    public void run() {
        codec.start();
        Log.d(TAG, "Starting decoder");
        boolean first = true;
        long timestamp = 0;
        while (true) {
            int index = codec.dequeueInputBuffer(100);
            if (index >= 0) {
                ByteBuffer input = codec.getInputBuffer(index);
                try {
                    inputStream.readMedia(input);
                    Log.e(TAG, String.format("Received %d", input.position()));
                    codec.queueInputBuffer(index, 0, input.position(), timestamp, 0);
                    timestamp++;
                } catch (IOException error) {
                }
            }

            MediaCodec.BufferInfo info = new MediaCodec.BufferInfo();
            int outIndex = codec.dequeueOutputBuffer(info, 1000);
            switch (outIndex) {
                case MediaCodec.INFO_OUTPUT_FORMAT_CHANGED:
                    Log.d(TAG, "New format " + codec.getOutputFormat());
                    break;
                case MediaCodec.INFO_TRY_AGAIN_LATER:
                    Log.d(TAG, "dequeueOutputBuffer timed out!");
                    break;
                default:
                    ByteBuffer outputBuffer = codec.getOutputBuffer(outIndex);
                    Log.v(TAG, "We can't use this buffer but render it due to the API limit, " + outputBuffer);
                    codec.releaseOutputBuffer(outIndex, true);
            }

        }
    }
}
