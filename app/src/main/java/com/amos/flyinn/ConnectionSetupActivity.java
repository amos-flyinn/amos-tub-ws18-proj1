package com.amos.flyinn;

import android.Manifest;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Point;
import android.hardware.display.DisplayManager;
import android.hardware.display.VirtualDisplay;
import android.media.MediaCodec;
import android.media.MediaCodecInfo;
import android.media.MediaFormat;
import android.media.projection.MediaProjection;
import android.media.projection.MediaProjectionManager;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AppCompatActivity;
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

public class ConnectionSetupActivity extends AppCompatActivity {
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
        }
        startScreenShare();
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

    private void initRecorder() {
        Point p = new Point();
        getWindowManager().getDefaultDisplay().getRealSize(p);
        try {
            PipedInputStream stream = new PipedInputStream();
            PipedOutputStream data = new PipedOutputStream(stream);
            VideoStreamSingleton.getInstance().os = stream;

            Intent intent = NearbyService.createNearbyIntent(NearbyService.VIDEO_START, this);
            startService(intent);

            MediaCodec codec = MediaCodec.createEncoderByType("video/avc");
            MediaFormat format = new MediaFormat();
            format.setString(MediaFormat.KEY_MIME, "video/avc");
            format.setInteger(MediaFormat.KEY_BIT_RATE, 512 * 1000);
            format.setInteger(MediaFormat.KEY_FRAME_RATE, 30);
            format.setInteger(MediaFormat.KEY_COLOR_FORMAT, MediaCodecInfo.CodecCapabilities.COLOR_FormatSurface);
            format.setInteger(MediaFormat.KEY_I_FRAME_INTERVAL, 10);
            format.setInteger(MediaFormat.KEY_WIDTH, p.x);
            format.setInteger(MediaFormat.KEY_HEIGHT, p.y);
            format.setLong(MediaFormat.KEY_REPEAT_PREVIOUS_FRAME_AFTER, 1_000_000 * 6 / 60);
            codec.configure(format, null, null, MediaCodec.CONFIGURE_FLAG_ENCODE);
            mMediaProjection.createVirtualDisplay(
                    TAG,
                    p.x,
                    p.y,
                    mScreenDensity,
                    DisplayManager.VIRTUAL_DISPLAY_FLAG_AUTO_MIRROR,
                    codec.createInputSurface(),
                    null,
                    null
            );
            codec.start();
            new Thread(() -> {
                try {
                    encode(codec, data);
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }).start();

            // display the very first frame, and recover from bad quality when no new frames
//
//            mMediaRecorder.setVideoSize(p.x, p.y);
//            mMediaRecorder.setVideoSource(MediaRecorder.VideoSource.SURFACE);
//            mMediaRecorder.setOutputFormat(MediaRecorder.OutputFormat.MPEG_4);
////            mMediaRecorder.setOutputFile(data);
//
//            mMediaRecorder.setVideoEncoder(MediaRecorder.VideoEncoder.H264);
//            mMediaRecorder.setVideoEncodingBitRate(512 * 1000);
//            mMediaRecorder.setVideoFrameRate(30);
//            int rotation = getWindowManager().getDefaultDisplay().getRotation();
//            int orientation = ORIENTATIONS.get(rotation + 90);
//            mMediaRecorder.setOrientationHint(orientation);
//            mMediaRecorder.prepare();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private boolean encode(MediaCodec codec, PipedOutputStream fd) throws IOException {
        boolean eof = false;
        MediaCodec.BufferInfo bufferInfo = new MediaCodec.BufferInfo();
        while (!eof) {
            int outputBufferId = codec.dequeueOutputBuffer(bufferInfo, -1);
            eof = (bufferInfo.flags & MediaCodec.BUFFER_FLAG_END_OF_STREAM) != 0;
            try {
                if (outputBufferId >= 0) {
                    ByteBuffer codecBuffer = codec.getOutputBuffer(outputBufferId);
//                    if (sendFrameMeta) {
//                        writeFrameMeta(fd, bufferInfo, codecBuffer.remaining());
//                    }
                    fd.write(ByteBuffer.allocate(4).order(ByteOrder.BIG_ENDIAN).putInt(codecBuffer.array().length).array());
                    fd.write(ByteBuffer.allocate(4).order(ByteOrder.BIG_ENDIAN).putInt(1).array());
                    fd.write(codecBuffer.array());
                }
            } finally {
                if (outputBufferId >= 0) {
                    codec.releaseOutputBuffer(outputBufferId, false);
                }
            }
        }

        return !eof;
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
        if (mVirtualDisplay == null) {
            return;
        }
        mVirtualDisplay.release();
        //mMediaRecorder.release(); //If used: mMediaRecorder object cannot
        // be reused again
        destroyMediaProjection();
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        destroyMediaProjection();
    }

    private void destroyMediaProjection() {
        if (mMediaProjection != null) {
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
