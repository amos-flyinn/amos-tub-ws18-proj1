package com.amos.flyinn;

import android.content.Context;
import android.content.Intent;
import android.media.projection.MediaProjectionManager;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v7.app.AppCompatActivity;
import android.widget.TextView;
import android.widget.Toast;

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
    private TextView progressText;
    private MediaProjectionManager mProjectionManager;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_connection_setup);
        progressText = findViewById(R.id.progressText);
        initScreenCapturePermissions();
    }

    /**
     * This method is going to be called after the screen capture permission is asked.
     * It gives the permissions necessary for the WebRTC screen capture to work
     *
     * @param requestCode the permission code for the Android permission
     * @param resultCode  the result of the operation
     * @param data        information about the screen capture permissions
     */
    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        if (resultCode != RESULT_OK) {
            Toast.makeText(this,
                    "Screen Cast Permission Denied", Toast.LENGTH_SHORT).show();
            return;
        }
    }

    private void initScreenCapturePermissions() {
        mProjectionManager = (MediaProjectionManager) getSystemService
                (Context.MEDIA_PROJECTION_SERVICE);
        startActivityForResult(mProjectionManager.createScreenCaptureIntent(), 42);
    }
}
