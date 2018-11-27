package com.amos.server.webrtc;

import android.app.Activity;
import android.app.Application;
import android.content.Context;
import android.util.Log;
import android.view.View;

import com.amos.server.MainActivity;
import com.amos.server.WebRTCServerActivity;
import com.amos.server.signaling.Emitter;

import org.webrtc.Camera1Enumerator;
import org.webrtc.DefaultVideoDecoderFactory;
import org.webrtc.DefaultVideoEncoderFactory;
import org.webrtc.EglBase;
import org.webrtc.IceCandidate;
import org.webrtc.MediaConstraints;
import org.webrtc.MediaStream;
import org.webrtc.PeerConnection;
import org.webrtc.PeerConnectionFactory;
import org.webrtc.SessionDescription;
import org.webrtc.VideoCapturer;
import org.webrtc.VideoTrack;

import java.util.ArrayList;

public class PeerWrapper implements IPeer {

    private MainActivity activity;
    private Emitter emitter;
    private Context appContext;
    private PeerConnection connection;
    private PeerConnectionFactory peerFactory;
    private PeerConnectionFactory.InitializationOptions webRTCConfig;
    private EglBase rootEglBase;

    public PeerWrapper(Activity app) {

        this.appContext = app.getApplicationContext();
        this.activity = (MainActivity) app;

        this.configPeerConnection();
        this.createPeer();
        this.initComponents();
    }

    private void initComponents(){
        this.activity.getRender().setZOrderMediaOverlay(true);
        this.activity.getRender().init(this.rootEglBase.getEglBaseContext(),null);
    }

    public void setEmitter(Emitter emitter){
        this.emitter = emitter;
    }

    private void configPeerConnection() {

        this.rootEglBase = EglBase.create();

        this.webRTCConfig = PeerConnectionFactory.InitializationOptions.builder(this.appContext)
                .setEnableVideoHwAcceleration(true)
                .createInitializationOptions();

        PeerConnectionFactory.initialize(webRTCConfig);
        PeerConnectionFactory.initializeFieldTrials("IncludeWifiDirect/Enabled/");

        PeerConnectionFactory.Options options = new PeerConnectionFactory.Options();
        DefaultVideoEncoderFactory defaultVideoEncoderFactory = new DefaultVideoEncoderFactory(
                rootEglBase.getEglBaseContext(),  /* enableIntelVp8Encoder */true,  /* enableH264HighProfile */true);
        DefaultVideoDecoderFactory defaultVideoDecoderFactory = new DefaultVideoDecoderFactory(rootEglBase.getEglBaseContext());
        peerFactory = new PeerConnectionFactory(options,defaultVideoEncoderFactory,defaultVideoDecoderFactory);



    }


    private void createPeer() {




        this.connection = peerFactory.createPeerConnection(new ArrayList<>(), new PeerObserver() {
            @Override
            public void onIceCandidate(IceCandidate iceCandidate) {
                super.onIceCandidate(iceCandidate);
                Log.d("PeerWrapper","Here is the ice Candidate : " + iceCandidate);
                onIceCandidateReceived(iceCandidate);
            }

            @Override
            public void onAddStream(MediaStream mediaStream) {
                Log.d("PeerWrapperServer","Get Remote Stream");
                super.onAddStream(mediaStream);
                setRemoteStream(mediaStream);
            }
        }


        );

    }

    public void setRemoteStream(MediaStream stream) {
        VideoTrack trackRemoteCamera = stream.videoTracks.get(0);
        this.activity.runOnUiThread(new Runnable() {
            @Override
            public void run() {
                activity.getRender().setVisibility(View.VISIBLE);
                trackRemoteCamera.addSink(activity.getRender());
            }
        });
    }

    public void beginTransactionWithAnswer() {

        MediaConstraints sdpConstraints = new MediaConstraints();
        this.connection.createAnswer(new SdpObserver() {
            @Override
            public void onCreateSuccess(SessionDescription sessionDescription) {
                super.onCreateSuccess(sessionDescription);
                connection.setLocalDescription(new SdpObserver(), sessionDescription);
                emitter.shareSessionDescription(sessionDescription);
            }
        }, sdpConstraints);
    }


    private void onIceCandidateReceived(IceCandidate candidate) {
        this.emitter.shareIceCandidate(candidate);
    }


    @Override
    public void setRemoteDescriptorPeer(SessionDescription descriptorPeer) {
        this.connection.setRemoteDescription(new SdpObserver(), descriptorPeer);
        this.beginTransactionWithAnswer();
    }

    @Override
    public void setRemoteIceCandidate(IceCandidate candidate) {
        this.connection.addIceCandidate(candidate);
    }
}
