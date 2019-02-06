package com.amos.flyinn;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.content.pm.ActivityInfo;
import android.graphics.Point;
import android.hardware.display.DisplayManager;
import android.media.MediaCodec;
import android.media.MediaCodecInfo;
import android.media.MediaFormat;
import android.media.projection.MediaProjection;
import android.media.projection.MediaProjectionManager;
import android.os.Build;
import android.os.Bundle;
import android.os.StrictMode;
import android.provider.MediaStore;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.annotation.RequiresApi;
import android.util.DisplayMetrics;
import android.util.Log;
import android.view.Surface;

import com.amos.flyinn.nearbyservice.NearbyService;
import com.amos.flyinn.nearbyservice.VideoStreamSingleton;
import com.amos.flyinn.service.FPSOverlay;

import java.io.BufferedOutputStream;
import java.io.EOFException;
import java.io.IOException;
import java.io.OutputStream;
import java.io.PipedInputStream;
import java.io.PipedOutputStream;
import java.nio.ByteBuffer;
import java.nio.ByteOrder;

import static android.media.MediaCodecList.getCodecCount;
import static android.media.MediaCodecList.getCodecInfoAt;

/**
 * <h1>ConnectionSetup</h1>
 * <p>
 * The ConnectionSetup Activity is responsible to Setup the connection between the Client and the Server app.
 * This class is also responsible to inform the user about possible problems that could happen in the WebRTC stream negotiation
 * or in the ADB server connection.
 * It also tries to handle all possible error states giving the user some options to proceed in failure cases.
 * </p>
 */

public class ConnectionSetupActivity extends Activity {
    private static final String TAG = "ConnectionSetupActivity";
    private static final int REQUEST_CODE = 1000;
    private int mScreenDensity;
    private MediaProjectionManager mProjectionManager;
    private MediaProjection mMediaProjection;
    private MediaProjectionCallback mMediaProjectionCallback;
    private Surface si;

    private MediaCodec codec;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_connection_setup);
        DisplayMetrics metrics = new DisplayMetrics();
        getWindowManager().getDefaultDisplay().getRealMetrics(metrics);
        mScreenDensity = metrics.densityDpi;
        mProjectionManager = (MediaProjectionManager) getSystemService(Context.MEDIA_PROJECTION_SERVICE);

        if (mMediaProjection == null) {
            startActivityForResult(videoIntent(), REQUEST_CODE);
        }
    }

    @RequiresApi(api = Build.VERSION_CODES.M)
    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (requestCode != REQUEST_CODE) {
            Log.e(TAG, "Unknown request code: " + requestCode);
            return;
        }
        if (resultCode != RESULT_OK) {
            return;
        }
        mMediaProjectionCallback = new MediaProjectionCallback();
        mMediaProjection = mProjectionManager.getMediaProjection(resultCode, data);
        mMediaProjection.registerCallback(mMediaProjectionCallback, null);
        if (mMediaProjection == null) {
            startActivityForResult(videoIntent(), REQUEST_CODE);
            return;
        }
        startScreenShare();
    }

    private Intent videoIntent() {
        setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_PORTRAIT);
        Intent mOptionIntent = mProjectionManager.createScreenCaptureIntent();
        mOptionIntent.putExtra("return-data", true);
        mOptionIntent.putExtra(MediaStore.EXTRA_SCREEN_ORIENTATION, ActivityInfo.SCREEN_ORIENTATION_LANDSCAPE);
        return mOptionIntent;
    }


    @RequiresApi(api = Build.VERSION_CODES.M)
    public void startScreenShare() {
        initRecorder();
    }

    @Nullable
    private static MediaCodecInfo selectCodec(String mimeType) {
        int numCodecs = getCodecCount();
        for (int i = 0; i < numCodecs; i++) {
            MediaCodecInfo codecInfo = getCodecInfoAt(i);
            if (!codecInfo.isEncoder()) {
                continue;
            }
            String[] types = codecInfo.getSupportedTypes();
            for (String type : types) {
                if (type.equalsIgnoreCase(mimeType)) {
                    return codecInfo;
                }
            }
        }
        return null;
    }

    private MediaCodec.Callback buildEncoderCallback(OutputStream data, int width, int height) {
        return new MediaCodec.Callback() {
            MediaFormat mOutputFormat;
            byte[] qq = new byte[width * height * 2];
            ByteBuffer bb = ByteBuffer.allocate(4 + 8).order(ByteOrder.BIG_ENDIAN);
            ByteBuffer outputBuffer;

            @Override
            public void onInputBufferAvailable(@NonNull MediaCodec codec, int index) {
                // do not do anything with input buffers
            }

            @Override
            public void onOutputBufferAvailable(@NonNull MediaCodec codec, int outputBufferId, @NonNull MediaCodec.BufferInfo info) {
                outputBuffer = codec.getOutputBuffer(outputBufferId);
                if (outputBuffer != null) {
                    outputBuffer.position(info.offset);
                }
                if (outputBuffer != null) {
                    outputBuffer.limit(info.offset + info.size);
                }
                try {
                    bb.rewind();
                    bb.putLong(info.presentationTimeUs);
                    bb.putInt(info.size);
                    data.write(bb.array());
                    outputBuffer.get(qq, 0, info.size);
                    data.write(qq, 0, info.size);
                } catch (EOFException e) {
                   stopScreenSharing();
                } catch (IOException e) {
                    e.printStackTrace();
                }
                outputBuffer.clear();
                codec.releaseOutputBuffer(outputBufferId, false);
            }

            @Override
            public void onError(@NonNull MediaCodec codec, @NonNull MediaCodec.CodecException e) {
                // not handling errors yet
            }

            @Override
            public void onOutputFormatChanged(@NonNull MediaCodec mc, @NonNull MediaFormat format) {
                mOutputFormat = format;
            }
        };
    }

    private MediaFormat buildMediaFormat(String mimetype, int width, int height) {
        MediaFormat format = MediaFormat.createVideoFormat(mimetype, width, height);
//            MediaCodecInfo.VideoCapabilities videoCapabilities = codecInfo.getCapabilitiesForType(MIME_TYPE).getVideoCapabilities();
        format.setInteger(MediaFormat.KEY_COLOR_FORMAT, MediaCodecInfo.CodecCapabilities.COLOR_FormatSurface);
        format.setInteger(MediaFormat.KEY_BIT_RATE, 834_001);
        format.setInteger(MediaFormat.KEY_FRAME_RATE, 30);
        format.setInteger(MediaFormat.KEY_I_FRAME_INTERVAL, 1);
        format.setInteger(MediaFormat.KEY_REPEAT_PREVIOUS_FRAME_AFTER, 4000);
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            format.setInteger(MediaFormat.KEY_ROTATION, 90);
        }
        format.setInteger("rotation-degrees", 90);

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            format.setInteger(MediaFormat.KEY_PRIORITY, 0);
        }
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            format.setInteger(MediaFormat.KEY_LATENCY, 0);
        }
        return format;
    }

    @RequiresApi(api = Build.VERSION_CODES.M)
    private void initRecorder() {
        Point p = new Point();
        getWindowManager().getDefaultDisplay().getRealSize(p);
        int w = 480;
        int h = 800;
        String MIME_TYPE = "video/avc";
        try {
            PipedInputStream stream = new PipedInputStream();
            PipedOutputStream data2 = new PipedOutputStream(stream);
            BufferedOutputStream data = new BufferedOutputStream(data2);
            {
                StrictMode.ThreadPolicy policy = new StrictMode.ThreadPolicy.Builder().permitAll().build();
                StrictMode.setThreadPolicy(policy);
            }
//            OutputStream data = new Socket("192.168.2.121", 5551).getOutputStream();
            VideoStreamSingleton.getInstance().os = stream;
            Intent intent = NearbyService.createNearbyIntent(NearbyService.VIDEO_START, this);
            startService(intent);

            Log.d(TAG, "Payload sent video_start");

            MediaCodecInfo codecInfo = selectCodec(MIME_TYPE);
            assert codecInfo != null;
            codec = MediaCodec.createEncoderByType(MIME_TYPE);
            MediaFormat format = buildMediaFormat(MIME_TYPE, w, h);

            Log.d(TAG, format.toString());
            codec.setCallback(buildEncoderCallback(data, w, h));

            codec.configure(format, null, null, MediaCodec.CONFIGURE_FLAG_ENCODE);
            si = codec.createInputSurface();
            mMediaProjection.createVirtualDisplay(
                    TAG,
                    w,
                    h,
                    mScreenDensity,
                    DisplayManager.VIRTUAL_DISPLAY_FLAG_AUTO_MIRROR,
                    si,
                    null,
                    null
            );
            Log.d(TAG, "Created Media Encoder");
            new Thread(() -> {
                Log.d(TAG, "Start encoding");
                codec.start();
            }).start();
            Intent fpsOverlayIntent = new Intent(getApplicationContext(), FPSOverlay.class);
            startService(fpsOverlayIntent);
        } catch (Exception e) {
            e.printStackTrace();
        }
        startActivity(new Intent(this, ConnectedActivity.class));
    }

    private class MediaProjectionCallback extends MediaProjection.Callback {
        @Override
        public void onStop() {
            Log.v(TAG, "Recording Stopped");
            mMediaProjection = null;
            stopScreenSharing();
        }
    }

    private void stopScreenSharing() {
        if (codec != null) {
            codec.stop();
            codec.release();
        }
        if (si != null) {
            si.release();
        }
        Intent fpsOverlayIntent = new Intent(getApplicationContext(), FPSOverlay.class);
        stopService(fpsOverlayIntent);
        // If used: mMediaRecorder object cannot be reused again
        destroyMediaProjection();
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        Log.d(TAG, "Destroy codec");
        stopScreenSharing();
    }

    private void destroyMediaProjection() {
        if (mMediaProjection != null) {
            mMediaProjection.unregisterCallback(mMediaProjectionCallback);
            mMediaProjection.stop();
            mMediaProjection = null;
        }
        Log.i(TAG, "MediaProjection Stopped");
    }
}
