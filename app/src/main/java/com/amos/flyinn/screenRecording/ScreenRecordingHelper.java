package com.amos.flyinn.screenRecording;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.hardware.display.DisplayManager;
import android.hardware.display.VirtualDisplay;
import android.media.MediaRecorder;
import android.media.projection.MediaProjection;
import android.media.projection.MediaProjectionManager;
import android.os.Environment;
import android.os.ParcelFileDescriptor;
import android.util.DisplayMetrics;
import android.util.SparseIntArray;
import android.view.Surface;

import com.amos.flyinn.ScreenSharingClient;

import java.io.IOException;


public class ScreenRecordingHelper {

    private static final String TAG = "ScreenRecordingHelper";
    private static final int REQUEST_CODE = 1000;
    private int mScreenDensity;
    private MediaProjectionManager mProjectionManager;
    private static final int DISPLAY_WIDTH = 720;
    private static final int DISPLAY_HEIGHT = 1280;
    private MediaProjection mMediaProjection;
    private VirtualDisplay mVirtualDisplay;
    private MediaRecorder mMediaRecorder;
    private static final SparseIntArray ORIENTATIONS = new SparseIntArray();

    public ParcelFileDescriptor writeFD;

    public Activity screenSharingActivity;

    static {
        ORIENTATIONS.append(Surface.ROTATION_0, 90);
        ORIENTATIONS.append(Surface.ROTATION_90, 0);
        ORIENTATIONS.append(Surface.ROTATION_180, 270);
        ORIENTATIONS.append(Surface.ROTATION_270, 180);
    }

    public ScreenRecordingHelper(Activity activity, ParcelFileDescriptor writeFD){
        this.screenSharingActivity = activity;
        this.writeFD = writeFD;

    }

    public void setup(){


        DisplayMetrics metrics = new DisplayMetrics();
        screenSharingActivity.getWindowManager().getDefaultDisplay().getMetrics(metrics);
        mScreenDensity = metrics.densityDpi;

        mMediaRecorder = new MediaRecorder();

        mProjectionManager = (MediaProjectionManager) screenSharingActivity.getSystemService
                (Context.MEDIA_PROJECTION_SERVICE);

    }


    public void shareScreen() {
        if (mMediaProjection == null) {
            screenSharingActivity.startActivityForResult(mProjectionManager.createScreenCaptureIntent(), REQUEST_CODE);
            return;
        }
        mVirtualDisplay = createVirtualDisplay();
        mMediaRecorder.start();
    }


    private VirtualDisplay createVirtualDisplay() {
        return mMediaProjection.createVirtualDisplay("RecordingActivity",
                DISPLAY_WIDTH, DISPLAY_HEIGHT, mScreenDensity,
                DisplayManager.VIRTUAL_DISPLAY_FLAG_AUTO_MIRROR,
                mMediaRecorder.getSurface(), null /*Callbacks*/, null
                /*Handler*/);
    }

    public void initRecorder() {
        try {
            mMediaRecorder.setAudioSource(MediaRecorder.AudioSource.MIC);
            mMediaRecorder.setVideoSource(MediaRecorder.VideoSource.SURFACE);
            mMediaRecorder.setOutputFormat(MediaRecorder.OutputFormat.THREE_GPP);
            //mMediaRecorder.setOutputFile(Environment
            //        .getExternalStoragePublicDirectory(Environment
            //                .DIRECTORY_DOWNLOADS) + "/video.mp4"); // Location of file
            mMediaRecorder.setOutputFile(writeFD.getFileDescriptor());
            mMediaRecorder.setVideoSize(DISPLAY_WIDTH, DISPLAY_HEIGHT);
            mMediaRecorder.setVideoEncoder(MediaRecorder.VideoEncoder.H264);
            mMediaRecorder.setAudioEncoder(MediaRecorder.AudioEncoder.AMR_NB);
            mMediaRecorder.setVideoEncodingBitRate(512 * 1000);
            mMediaRecorder.setVideoFrameRate(30);
            //int rotation = getWindowManager().getDefaultDisplay().getRotation();
            //int orientation = ORIENTATIONS.get(rotation + 90);
            //mMediaRecorder.setOrientationHint(orientation);
            mMediaRecorder.prepare();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public void onActivityResult(ScreenSharingClient.MediaProjectionCallback mMediaProjectionCallback, int resultCode, Intent data){

        mMediaProjection = mProjectionManager.getMediaProjection(resultCode, data);
        mMediaProjection.registerCallback(mMediaProjectionCallback, null);
        mVirtualDisplay = createVirtualDisplay();
        mMediaRecorder.start();
    }


    public void resetMediaRecorder(){
        mMediaRecorder.stop();
        mMediaRecorder.reset();
    }

    public void stopMediaProjection(){
        mMediaProjection = null;
    }


}
