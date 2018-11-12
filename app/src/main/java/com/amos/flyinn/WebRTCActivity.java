package com.amos.flyinn;

import android.app.Activity;

import java.net.URI;

import android.content.Context;
import android.content.Intent;
import android.media.projection.MediaProjectionManager;
import android.os.Bundle;
import android.os.IBinder;
import android.support.annotation.Nullable;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.Toast;

import com.amos.flyinn.screenRecording.RecordingActivity;
import com.amos.flyinn.signaling.ClientSocket;
import com.amos.flyinn.signaling.Emitter;
import com.amos.flyinn.webrtc.PeerObserver;
import com.amos.flyinn.webrtc.PeerWrapper;
import com.amos.flyinn.webrtc.SdpObserver;

import org.webrtc.DefaultVideoDecoderFactory;
import org.webrtc.DefaultVideoEncoderFactory;
import org.webrtc.EglBase;
import org.webrtc.IceCandidate;
import org.webrtc.MediaConstraints;
import org.webrtc.PeerConnection;
import org.webrtc.PeerConnectionFactory;
import org.webrtc.SessionDescription;
import org.webrtc.SurfaceViewRenderer;

import java.util.ArrayList;
import java.util.List;

import static android.support.constraint.Constraints.TAG;

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

        startActivityForResult(mProjectionManager.createScreenCaptureIntent(), 10);

        this.buttonInit = (Button) this.findViewById(R.id.webrtc_init);
        this.buttonInit.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                initWebRTC();
            }
        });
    }



    private void initWebRTC(){
        Log.d("WebRTCActivity","initWEBRTC");
        this.peerWrapper.beginTransactionWithOffer();
    }


    public SurfaceViewRenderer getRender(){
        return render;
    }

    private void initViews(){
        render = findViewById(R.id.surface_local_viewer);
    }


    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        IBinder iBinder = data.getExtras().getBinder("android.media.projection.extra.EXTRA_MEDIA_PROJECTION");
        Log.i("Ibinder Debug", "starts: ");
        if (iBinder != null) {
            Log.i("Ibinder Debug", "Existieeeert");
        }

        if (requestCode != 10) {
            Log.e(TAG, "Unknown request code: " + requestCode);
            return;
        }
        if (resultCode != RESULT_OK) {
            Toast.makeText(this,
                    "Screen Cast Permission Denied", Toast.LENGTH_SHORT).show();

            return;
        }

        initViews();
        this.peerWrapper = new PeerWrapper(this,data);
        this.clientSocket = new ClientSocket(URI.create("ws://192.168.49.1:8080"),this.peerWrapper);
        this.clientSocket.connect();
        this.peerWrapper.setEmitter((Emitter) this.clientSocket);


    }

}
