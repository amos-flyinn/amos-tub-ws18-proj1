package com.amos.flyinn;

import android.Manifest;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.media.projection.MediaProjection;
import android.net.Uri;
import android.os.Bundle;
import android.app.Activity;
import android.os.Environment;
import android.os.IBinder;
import android.os.ParcelFileDescriptor;
import android.provider.Settings;
import android.support.annotation.NonNull;
import android.support.design.widget.Snackbar;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.util.Log;
import android.view.View;
import android.widget.Toast;
import android.widget.ToggleButton;

import com.amos.flyinn.screenRecording.ScreenRecordingHelper;
import com.google.android.gms.nearby.Nearby;
import com.google.android.gms.nearby.connection.Payload;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;

public class ScreenSharingClient extends Activity {

    private static final String TAG = "ScreenSharingClient";
    private static final int REQUEST_CODE = 1000;
    private MediaProjectionCallback mMediaProjectionCallback;
    private ToggleButton mToggleButton;
    private static final int REQUEST_PERMISSIONS = 10;

    ParcelFileDescriptor readFD;
    ParcelFileDescriptor writeFD;
    private ScreenRecordingHelper helper;

    private File tempFileVideo;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_recodring);

        try {
            tempFileVideo = File.createTempFile("videoStream",".mp4",Environment
                            .getExternalStoragePublicDirectory(Environment
                                    .DIRECTORY_DOWNLOADS));
        } catch (IOException e) {
            e.printStackTrace();
        }

        if(!tempFileVideo.exists())
        {
            Log.d(TAG, "onCreate: Error");

        }


        //Initialize parcel descriptors
        try {

            readFD = ParcelFileDescriptor.open(tempFileVideo,ParcelFileDescriptor.MODE_READ_WRITE);
            writeFD = ParcelFileDescriptor.open(tempFileVideo,ParcelFileDescriptor.MODE_READ_WRITE);
            Log.d(TAG, "onCreate: FilesDescriptors created");
        } catch (IOException e) {
            e.printStackTrace();
        }





        helper = new ScreenRecordingHelper(this, writeFD);
        helper.setup();


        mToggleButton = (ToggleButton) findViewById(R.id.toggle);
        mToggleButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (ContextCompat.checkSelfPermission(ScreenSharingClient.this,
                        Manifest.permission.WRITE_EXTERNAL_STORAGE) + ContextCompat
                        .checkSelfPermission(ScreenSharingClient.this,
                                Manifest.permission.RECORD_AUDIO)
                        != PackageManager.PERMISSION_GRANTED) {
                    if (ActivityCompat.shouldShowRequestPermissionRationale
                            (ScreenSharingClient.this, Manifest.permission.WRITE_EXTERNAL_STORAGE) ||
                            ActivityCompat.shouldShowRequestPermissionRationale
                                    (ScreenSharingClient.this, Manifest.permission.RECORD_AUDIO)) {
                        mToggleButton.setChecked(false);
                        Snackbar.make(findViewById(android.R.id.content), "permission",
                                Snackbar.LENGTH_INDEFINITE).setAction("ENABLE",
                                new View.OnClickListener() {
                                    @Override
                                    public void onClick(View v) {
                                        ActivityCompat.requestPermissions(ScreenSharingClient.this,
                                                new String[]{Manifest.permission
                                                        .WRITE_EXTERNAL_STORAGE, Manifest.permission.RECORD_AUDIO},
                                                REQUEST_PERMISSIONS);
                                    }
                                }).show();
                    } else {
                        ActivityCompat.requestPermissions(ScreenSharingClient.this,
                                new String[]{Manifest.permission
                                        .WRITE_EXTERNAL_STORAGE, Manifest.permission.RECORD_AUDIO},
                                REQUEST_PERMISSIONS);
                    }
                } else {
                    onToggleScreenShare(v);
                }
            }
        });

    }

    @Override
    public void onRequestPermissionsResult(int requestCode,
                                           @NonNull String permissions[],
                                           @NonNull int[] grantResults) {
        switch (requestCode) {
            case REQUEST_PERMISSIONS: {
                if ((grantResults.length > 0) && (grantResults[0] +
                        grantResults[1]) == PackageManager.PERMISSION_GRANTED) {
                    onToggleScreenShare(mToggleButton);
                } else {
                    mToggleButton.setChecked(false);
                    Snackbar.make(findViewById(android.R.id.content), "Permission",
                            Snackbar.LENGTH_INDEFINITE).setAction("ENABLE",
                            new View.OnClickListener() {
                                @Override
                                public void onClick(View v) {
                                    Intent intent = new Intent();
                                    intent.setAction(Settings.ACTION_APPLICATION_DETAILS_SETTINGS);
                                    intent.addCategory(Intent.CATEGORY_DEFAULT);
                                    intent.setData(Uri.parse("package:" + getPackageName()));
                                    intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                                    intent.addFlags(Intent.FLAG_ACTIVITY_NO_HISTORY);
                                    intent.addFlags(Intent.FLAG_ACTIVITY_EXCLUDE_FROM_RECENTS);
                                    startActivity(intent);
                                }
                            }).show();
                }
                return;
            }
        }
    }


    public void onToggleScreenShare(View view) {
        if (((ToggleButton) view).isChecked()) {
            helper.initRecorder();
            helper.shareScreen();

            Payload filePayload = Payload.fromStream(readFD);
            String endPoint = getIntent().getExtras().getString("endPoint");
            Nearby.getConnectionsClient(getApplicationContext()).sendPayload(endPoint, filePayload);

        } else {
            helper.resetMediaRecorder();
            Log.v(TAG, "Stopping Recording");
        }
    }


    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {

        super.onActivityResult(requestCode, resultCode, data);
        IBinder iBinder = data.getExtras().getBinder("android.media.projection.extra.EXTRA_MEDIA_PROJECTION");
        Log.i("Ibinder Debug", "starts: ");
        if (iBinder != null) {
            Log.i("Ibinder Debug", "Existieeeert");
        }

        if (requestCode != REQUEST_CODE) {
            Log.e(TAG, "Unknown request code: " + requestCode);
            return;
        }
        if (resultCode != RESULT_OK) {
            Toast.makeText(this,
                    "Screen Cast Permission Denied", Toast.LENGTH_SHORT).show();
            mToggleButton.setChecked(false);
            return;
        }


        mMediaProjectionCallback = new MediaProjectionCallback();
        helper.onActivityResult(mMediaProjectionCallback, resultCode, data);

    }


    public class MediaProjectionCallback extends MediaProjection.Callback {
        @Override
        public void onStop() {
            if (mToggleButton.isChecked()) {
                mToggleButton.setChecked(false);

                helper.resetMediaRecorder();
                Log.v(TAG, "Recording Stopped");
            }

            helper.stopMediaProjection();
        }
    }

}
