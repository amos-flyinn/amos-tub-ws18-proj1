package com.amos.flyinn;

import android.app.Activity;

import java.net.URI;

import android.os.Bundle;
import android.support.annotation.Nullable;
import android.util.Log;
import android.view.View;
import android.widget.Button;

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

public class WebRTCActivity extends Activity {

    private PeerConnection localConnection;
    private ClientSocket clientSocket;
    private PeerWrapper peerWrapper;
    private Button buttonInit;
    private SurfaceViewRenderer render;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_webrtc);
        initViews();
        this.peerWrapper = new PeerWrapper(this);
        this.clientSocket = new ClientSocket(URI.create("ws://192.168.49.205:8080"),this.peerWrapper);
        this.clientSocket.connect();
        this.peerWrapper.setEmitter((Emitter) this.clientSocket);
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


}
