package com.amos.flyinn;

import android.Manifest;
import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Point;
import android.hardware.display.DisplayManager;
import android.hardware.display.VirtualDisplay;
import android.media.MediaCodec;
import android.media.MediaCodecInfo;
import android.media.MediaCodecList;
import android.media.MediaFormat;
import android.media.projection.MediaProjection;
import android.media.projection.MediaProjectionManager;
import android.os.Build;
import android.os.Bundle;
import android.os.StrictMode;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.util.DisplayMetrics;
import android.util.Log;
import android.util.SparseIntArray;
import android.view.Surface;

import com.amos.flyinn.nearbyservice.NearbyService;
import com.amos.flyinn.nearbyservice.VideoStreamSingleton;

import java.io.IOException;
import java.io.PipedInputStream;
import java.io.PipedOutputStream;
import java.nio.ByteBuffer;
import java.nio.ByteOrder;

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
    private VirtualDisplay mVirtualDisplay;
    private MediaProjectionCallback mMediaProjectionCallback;
    private static final SparseIntArray ORIENTATIONS = new SparseIntArray();
    private static final int REQUEST_PERMISSIONS = 10;

    static {
        ORIENTATIONS.append(Surface.ROTATION_0, 90);
        ORIENTATIONS.append(Surface.ROTATION_90, 0);
        ORIENTATIONS.append(Surface.ROTATION_180, 270);
        ORIENTATIONS.append(Surface.ROTATION_270, 180);
    }

    private Surface si;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_connection_setup);
        DisplayMetrics metrics = new DisplayMetrics();
        getWindowManager().getDefaultDisplay().getMetrics(metrics);
        mScreenDensity = metrics.densityDpi;
        mProjectionManager = (MediaProjectionManager) getSystemService(Context.MEDIA_PROJECTION_SERVICE);
        if (ContextCompat.checkSelfPermission(this, Manifest.permission.WRITE_EXTERNAL_STORAGE) + ContextCompat.checkSelfPermission(this, Manifest.permission.RECORD_AUDIO) != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.WRITE_EXTERNAL_STORAGE, Manifest.permission.RECORD_AUDIO}, REQUEST_PERMISSIONS);
        } else {
            if (mMediaProjection == null) {
                startActivityForResult(mProjectionManager.createScreenCaptureIntent(), REQUEST_CODE);
                return;
            }
        }
    }

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
            startActivityForResult(mProjectionManager.createScreenCaptureIntent(), REQUEST_CODE);
            return;
        }
        startScreenShare();
    }

    public void startScreenShare() {
        initRecorder();
    }

    public void StopScreenShare() {
        Log.v(TAG, "Stopping Recording");
        stopScreenSharing();
    }


    private static MediaCodecInfo selectCodec(String mimeType) {
        int numCodecs = MediaCodecList.getCodecCount();
        for (int i = 0; i < numCodecs; i++) {
            MediaCodecInfo codecInfo = MediaCodecList.getCodecInfoAt(i);
            if (!codecInfo.isEncoder()) {
                continue;
            }
            String[] types = codecInfo.getSupportedTypes();
            for (int j = 0; j < types.length; j++) {
                if (types[j].equalsIgnoreCase(mimeType)) {
                    return codecInfo;
                }
            }
        }
        return null;
    }

    MediaCodec codec;

    private void initRecorder() {
        Point p = new Point();
        getWindowManager().getDefaultDisplay().getRealSize(p);
        int w = 240, h = 400;
        String MIME_TYPE = "video/avc";    // H.264 Advanced Video Coding
        try {
            PipedInputStream stream = new PipedInputStream();
            PipedOutputStream data = new PipedOutputStream(stream);
            VideoStreamSingleton.getInstance().os = stream;
            Intent intent = NearbyService.createNearbyIntent(NearbyService.VIDEO_START, this);
            startService(intent);

            {
                StrictMode.ThreadPolicy policy = new StrictMode.ThreadPolicy.Builder().permitAll().build();
                StrictMode.setThreadPolicy(policy);
            }

//            OutputStream data = new Socket("192.168.2.100", 5552).getOutputStream();
            Log.d(TAG, "Payload sent video_start");

            MediaCodecInfo codecInfo = selectCodec(MIME_TYPE);
            assert codecInfo != null;
            MediaFormat format = MediaFormat.createVideoFormat(MIME_TYPE, w, h);
//            MediaCodecInfo.VideoCapabilities videoCapabilities = codecInfo.getCapabilitiesForType(MIME_TYPE).getVideoCapabilities();
            codec = MediaCodec.createEncoderByType(MIME_TYPE);
            format.setInteger(MediaFormat.KEY_COLOR_FORMAT, MediaCodecInfo.CodecCapabilities.COLOR_FormatSurface);
            format.setInteger(MediaFormat.KEY_BIT_RATE, /*videoCapabilities.getBitrateRange().getUpper()*/ 128_000);
            format.setInteger(MediaFormat.KEY_FRAME_RATE, /*videoCapabilities.getSupportedFrameRatesFor(w, h).getLower().intValue()*/30);
            format.setInteger(MediaFormat.KEY_I_FRAME_INTERVAL, 1);
            format.setInteger(MediaFormat.KEY_REPEAT_PREVIOUS_FRAME_AFTER, 1000);
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                format.setInteger(MediaFormat.KEY_PRIORITY, 0);
            }
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                format.setInteger(MediaFormat.KEY_LATENCY, 0);
            }

            Log.d(TAG, format.toString());
            codec.setCallback(new MediaCodec.Callback() {
                MediaFormat mOutputFormat;
                byte[] qq = new byte[w * h * 2];

                @Override
                public void onInputBufferAvailable(MediaCodec codec, int index) {
                }

                @Override
                public void onOutputBufferAvailable(MediaCodec codec, int outputBufferId, MediaCodec.BufferInfo info) {
                    ByteBuffer outputBuffer = codec.getOutputBuffer(outputBufferId);
                    MediaFormat bufferFormat = codec.getOutputFormat(outputBufferId);
                    ByteBuffer bb = ByteBuffer.allocate(8).order(ByteOrder.BIG_ENDIAN);
                    // needed?
                    outputBuffer.position(info.offset);
                    outputBuffer.limit(info.offset + info.size);
                    try {
                        Log.d(TAG, String.format("%d", info.size));
                        outputBuffer.get(qq, 0, info.size);
                        outputBuffer.clear();
                        Log.d(TAG, "Going to write to stream");
                        data.write(qq, 0, info.size);
                        Log.d(TAG, "Wrote to stream");
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                    codec.releaseOutputBuffer(outputBufferId, false);
                }

                @Override
                public void onError(MediaCodec codec, MediaCodec.CodecException e) {
                }

                @Override
                public void onOutputFormatChanged(MediaCodec mc, MediaFormat format) {
                    mOutputFormat = format;
                }
            });

            codec.configure(format, null, null, MediaCodec.CONFIGURE_FLAG_ENCODE);
            si = codec.createInputSurface();
            mMediaProjection.createVirtualDisplay(
                    TAG,
                    w,
                    h,
                    mScreenDensity,
                    DisplayManager.VIRTUAL_DISPLAY_FLAG_PUBLIC,
                    si,
                    null,
                    null
            );
            Log.d(TAG, "Created Media Encoder");
            codec.start();
            Log.d(TAG, "Start encoding");
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
        codec.stop();
        codec.release();
        si.release();
        //mMediaRecorder.release(); //If used: mMediaRecorder object cannot
        // be reused again
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
            mVirtualDisplay.release();
            mMediaProjection.unregisterCallback(mMediaProjectionCallback);
            mMediaProjection.stop();
            mMediaProjection = null;
        }
        Log.i(TAG, "MediaProjection Stopped");
    }


    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String permissions[], @NonNull int[] grantResults) {
    }
}
