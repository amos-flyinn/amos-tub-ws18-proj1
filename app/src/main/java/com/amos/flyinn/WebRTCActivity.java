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

import java.util.ArrayList;
import java.util.List;

public class WebRTCActivity extends Activity {

    private PeerConnection localConnection;
    private ClientSocket clientSocket;
    private PeerWrapper peerWrapper;
    private Button buttonInit;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_webrtc);

        this.clientSocket = new ClientSocket(URI.create("ws://192.168.178.23:8080"));
        this.clientSocket.connect();
        this.peerWrapper = new PeerWrapper((Emitter) this.clientSocket, this.getApplicationContext());
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


}
