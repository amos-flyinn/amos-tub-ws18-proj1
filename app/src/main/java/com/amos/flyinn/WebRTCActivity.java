package com.amos.flyinn;

import android.Manifest;
import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.media.projection.MediaProjectionManager;
import android.os.Bundle;
import android.os.IBinder;
import android.support.annotation.Nullable;
import android.support.design.widget.Snackbar;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.util.Log;
import android.view.View;
import android.widget.Button;

import com.amos.flyinn.signaling.ClientSocket;
import com.amos.flyinn.signaling.Emitter;
import com.amos.flyinn.webrtc.PeerWrapper;

import org.webrtc.PeerConnection;
import org.webrtc.SurfaceViewRenderer;

import java.net.URI;


public class WebRTCActivity extends Activity {

    private PeerConnection localConnection;
    private ClientSocket clientSocket;
    private PeerWrapper peerWrapper;
    private IBinder binderParam;
    private Button buttonInit;
    private SurfaceViewRenderer render;
    private MediaProjectionManager mProjectionManager;


    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_webrtc);

        mProjectionManager = (MediaProjectionManager) getSystemService
                (Context.MEDIA_PROJECTION_SERVICE);

        startActivityForResult(mProjectionManager.createScreenCaptureIntent(), 42);

        this.buttonInit = (Button) this.findViewById(R.id.webrtc_init);
        this.buttonInit.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                initWebRTC();
            }
        });


        //not necessary atm
        requestNeededPermissions();
    }

    private void requestNeededPermissions() {
        if (ContextCompat.checkSelfPermission(WebRTCActivity.this,
                Manifest.permission.WRITE_EXTERNAL_STORAGE) + ContextCompat
                .checkSelfPermission(WebRTCActivity.this,
                        Manifest.permission.RECORD_AUDIO)
                != PackageManager.PERMISSION_GRANTED) {
            if (ActivityCompat.shouldShowRequestPermissionRationale
                    (WebRTCActivity.this, Manifest.permission.WRITE_EXTERNAL_STORAGE) ||
                    ActivityCompat.shouldShowRequestPermissionRationale
                            (WebRTCActivity.this, Manifest.permission.RECORD_AUDIO)) {
                Snackbar.make(findViewById(android.R.id.content), "permission",
                        Snackbar.LENGTH_INDEFINITE).setAction("ENABLE",
                        new View.OnClickListener() {
                            @Override
                            public void onClick(View v) {
                                ActivityCompat.requestPermissions(WebRTCActivity.this,
                                        new String[]{Manifest.permission
                                                .WRITE_EXTERNAL_STORAGE, Manifest.permission.RECORD_AUDIO},
                                        10);
                            }
                        }).show();
            } else {
                ActivityCompat.requestPermissions(WebRTCActivity.this,
                        new String[]{Manifest.permission
                                .WRITE_EXTERNAL_STORAGE, Manifest.permission.RECORD_AUDIO},
                        10);
            }
        } else {
            //onToggleScreenShare(v);
        }

    }


    private void initWebRTC() {
        Log.d("WebRTCActivity", "initWEBRTC");
        this.peerWrapper.beginTransactionWithOffer();
    }


    public SurfaceViewRenderer getRender() {
        return render;
    }

    private void initViews() {
        render = findViewById(R.id.surface_local_viewer);
    }


    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {

        initViews();
        this.peerWrapper = new PeerWrapper(this, data);
        this.clientSocket = new ClientSocket(URI.create("ws://192.168.49.205:8080"), this.peerWrapper);
        this.clientSocket.connect();
        this.peerWrapper.setEmitter((Emitter) this.clientSocket);


    }

}
