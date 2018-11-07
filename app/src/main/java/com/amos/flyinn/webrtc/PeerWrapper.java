package com.amos.flyinn.webrtc;

import android.app.Activity;
import android.content.Context;
import android.util.Log;
import android.view.View;

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
import org.webrtc.SurfaceViewRenderer;
import org.webrtc.VideoCapturer;
import org.webrtc.VideoSource;
import org.webrtc.VideoTrack;

import java.util.ArrayList;

public class PeerWrapper implements  IPeer {


    private SurfaceViewRenderer activityRender;
    private WebRTCActivity activity;
    private DataChannel localChannel;
    private VideoTrack localVideoTrack;
    private Emitter emitter;
    private Context appContext;
    private PeerConnection connection;
    private PeerConnectionFactory peerFactory;
    private PeerConnectionFactory.InitializationOptions webRTCConfig;
    private EglBase rootEglBase;
    private VideoSource videoSource;

    public PeerWrapper(Activity app) {

        this.appContext = app.getApplicationContext();
        this.activity = (WebRTCActivity)app;
        this.configPeerConnection();
        this.createPeer();
        this.activityRender = this.activity.getRender();
        this.initComponents();
    }

    public void setEmitter(Emitter emitter) {
        this.emitter = emitter;
    }


    private void initComponents(){
        this.activityRender.setZOrderMediaOverlay(true);
        this.activityRender.init(this.rootEglBase.getEglBaseContext(),null);
        VideoCapturer videoCapturerAndroid;
        videoCapturerAndroid = createCameraCapturer(new Camera1Enumerator(false));

        if (videoCapturerAndroid != null) {
            videoSource = peerFactory.createVideoSource(videoCapturerAndroid);
        }
        localVideoTrack = peerFactory.createVideoTrack("101",videoSource);

        if (videoCapturerAndroid != null) {
            videoCapturerAndroid.startCapture(1024, 720, 30);
        }
        this.activityRender.setVisibility(View.VISIBLE);
        localVideoTrack.addSink(this.activityRender);
        this.activityRender.setMirror(true);

        this.addCameraStreamToPeerConnection();
    }

    private VideoCapturer createCameraCapturer(CameraEnumerator enumerator) {
        final String[] deviceNames = enumerator.getDeviceNames();

        // First, try to find front facing camera
        Logging.d("PeerWrapperClient", "Looking for front facing cameras.");
        for (String deviceName : deviceNames) {
            if (enumerator.isFrontFacing(deviceName)) {
                Logging.d("PeerWrapperClient", "Creating front facing camera capturer.");
                VideoCapturer videoCapturer = enumerator.createCapturer(deviceName, null);

                if (videoCapturer != null) {
                    return videoCapturer;
                }
            }
        }



        // Front facing camera not found, try something else
        Logging.d("PeerWrapperClient", "Looking for other cameras.");
        for (String deviceName : deviceNames) {
            if (!enumerator.isFrontFacing(deviceName)) {
                Logging.d("PeerWrapperClient", "Creating other camera capturer.");
                VideoCapturer videoCapturer = enumerator.createCapturer(deviceName, null);

                if (videoCapturer != null) {
                    return videoCapturer;
                }
            }
        }

        return null;
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
        });



    }

    private void addCameraStreamToPeerConnection(){
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

        this.connection.createOffer(new SdpObserver() {
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
        this.connection.setRemoteDescription(new SdpObserver(),descriptorPeer);
    }

    @Override
    public void setRemoteIceCandidate(IceCandidate candidate) {
        this.connection.addIceCandidate(candidate);
    }
}
