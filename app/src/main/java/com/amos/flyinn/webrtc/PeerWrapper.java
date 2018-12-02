package com.amos.flyinn.webrtc;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.media.projection.MediaProjection;
import android.os.IBinder;
import android.util.Log;
import android.view.Surface;
import android.view.View;

import com.amos.flyinn.ConnectionSetupActivity;
import com.amos.flyinn.MainActivity;
import com.amos.flyinn.WebRTCActivity;
import com.amos.flyinn.signaling.Emitter;

import org.webrtc.Camera1Enumerator;
import org.webrtc.CameraEnumerator;
import org.webrtc.DataChannel;
import org.webrtc.DefaultVideoDecoderFactory;
import org.webrtc.DefaultVideoEncoderFactory;
import org.webrtc.EglBase;
import org.webrtc.IceCandidate;
import org.webrtc.Logging;
import org.webrtc.MediaConstraints;
import org.webrtc.MediaStream;
import org.webrtc.PeerConnection;
import org.webrtc.PeerConnectionFactory;
import org.webrtc.SessionDescription;
import org.webrtc.SurfaceTextureHelper;
import org.webrtc.SurfaceViewRenderer;
import org.webrtc.VideoCapturer;
import org.webrtc.VideoFrame;
import org.webrtc.VideoSource;
import org.webrtc.VideoTrack;
import org.webrtc.ScreenCapturerAndroid;

import java.util.ArrayList;

public class PeerWrapper implements IPeer {


    private SurfaceViewRenderer activityRender;
    private ConnectionSetupActivity activity;
    private SurfaceTextureHelper mTextureHelper;
    private Intent intentWithThing;
    private DataChannel localChannel;
    private VideoTrack localVideoTrack;
    private Emitter emitter;
    private Context appContext;
    private PeerConnection connection;
    private PeerConnectionFactory peerFactory;
    private PeerConnectionFactory.InitializationOptions webRTCConfig;
    private EglBase rootEglBase;
    private VideoSource videoSource;

    public PeerWrapper(Activity app, Intent intent) {

        this.intentWithThing = intent;
        this.appContext = app.getApplicationContext();
        this.activity = (ConnectionSetupActivity) app;
        this.configPeerConnection();
        this.createPeer();
        this.activityRender = this.activity.getRender();
        this.initComponents();
    }

    public void setEmitter(Emitter emitter) {
        this.emitter = emitter;
    }

    private void initComponents() {

        VideoCapturer videoCapturer = new ScreenCapturerAndroid(this.intentWithThing, new MediaProjection.Callback() {
            @Override
            public void onStop() {
                super.onStop();
                Log.d("MediaProjectionCallback", "onStop: ");
            }
        });

        this.activityRender.init(this.rootEglBase.getEglBaseContext(), null);

        this.activityRender.setZOrderMediaOverlay(true);

        peerFactory.setVideoHwAccelerationOptions(rootEglBase.getEglBaseContext(), rootEglBase.getEglBaseContext());

        videoSource = peerFactory.createVideoSource(videoCapturer);

        localVideoTrack = peerFactory.createVideoTrack("101", videoSource);

        videoCapturer.startCapture(1024, 720, 30);

        this.activityRender.setVisibility(View.GONE);
        localVideoTrack.addSink(this.activityRender);
        //this.activityRender.setMirror(true);

        this.addCameraStreamToPeerConnection();


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
        peerFactory = new PeerConnectionFactory(options, defaultVideoEncoderFactory, defaultVideoDecoderFactory);


    }

    private void createPeer() {


        this.connection = peerFactory.createPeerConnection(new ArrayList<>(), new PeerObserver() {
            @Override
            public void onIceCandidate(IceCandidate iceCandidate) {
                super.onIceCandidate(iceCandidate);
                Log.d("PeerWrapper", "Here is the ice Candidate : " + iceCandidate);
                onIceCandidateReceived(iceCandidate);
            }
        });


    }

    private void addCameraStreamToPeerConnection() {
        MediaStream stream = peerFactory.createLocalMediaStream("102");
        stream.addTrack(localVideoTrack);
        this.connection.addStream(stream);
    }


    public void beginTransactionWithOffer() {

        MediaConstraints sdpConstraints = new MediaConstraints();


        sdpConstraints.mandatory.add(
                new MediaConstraints.KeyValuePair("OfferToReceiveAudio", "true"));
        sdpConstraints.mandatory.add(new MediaConstraints.KeyValuePair(
                "OfferToReceiveVideo", "true"));

        this.connection.createOffer(new SdpObserver("LocalDescriptor", activity, SdpObserver.LOCAL_SDP) {
            @Override
            public void onCreateSuccess(SessionDescription sessionDescription) {
                super.onCreateSuccess(sessionDescription);
                connection.setLocalDescription(new SdpObserver("LocalDescriptor", activity, SdpObserver.LOCAL_SDP), sessionDescription);
                emitter.shareSessionDescription(sessionDescription);
            }
        }, sdpConstraints);
    }


    private void onIceCandidateReceived(IceCandidate candidate) {
        this.emitter.shareIceCandidate(candidate);
    }


    @Override
    public void setRemoteDescriptorPeer(SessionDescription descriptorPeer) {
        this.connection.setRemoteDescription(new SdpObserver("RemoteDescriptor", activity, SdpObserver.REMOTE_SDP), descriptorPeer);
    }

    @Override
    public void setRemoteIceCandidate(IceCandidate candidate) {
        this.connection.addIceCandidate(candidate);
    }
}
