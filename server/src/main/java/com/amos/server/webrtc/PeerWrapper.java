package com.amos.server.webrtc;

import android.content.Context;

import com.amos.server.signaling.Emitter;

import org.webrtc.DefaultVideoDecoderFactory;
import org.webrtc.DefaultVideoEncoderFactory;
import org.webrtc.EglBase;
import org.webrtc.IceCandidate;
import org.webrtc.MediaConstraints;
import org.webrtc.PeerConnection;
import org.webrtc.PeerConnectionFactory;
import org.webrtc.SessionDescription;

import java.util.ArrayList;

public class PeerWrapper implements IPeer {

    private Emitter emitter;
    private Context appContext;
    private PeerConnection connection;
    private PeerConnectionFactory peerFactory;
    private PeerConnectionFactory.InitializationOptions webRTCConfig;
    private EglBase rootEglBase;

    public PeerWrapper(Emitter emitter, Context appContext) {

        this.emitter = emitter;
        this.appContext = appContext;
        this.configPeerConnection();
        this.createPeer();
    }

    private void configPeerConnection() {

        this.rootEglBase = EglBase.create();
        this.webRTCConfig = PeerConnectionFactory.InitializationOptions.builder(this.appContext)
                .setEnableVideoHwAcceleration(true)
                .createInitializationOptions();


        PeerConnectionFactory.initialize(webRTCConfig);
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
                onIceCandidateReceived(iceCandidate);
            }
        });

    }


    public void beginTransactionWithOffer() {

        MediaConstraints sdpConstraints = new MediaConstraints();
        /*
        sdpConstraints.mandatory.add(
                new MediaConstraints.KeyValuePair("OfferToReceiveAudio", "true"));
        sdpConstraints.mandatory.add(new MediaConstraints.KeyValuePair(
                "OfferToReceiveVideo", "true"));
        */
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
        this.connection.addIceCandidate(candidate);
    }


    @Override
    public void setRemoteDescriptorPeer(SessionDescription descriptorPeer) {
        this.connection.setRemoteDescription(new SdpObserver(), descriptorPeer);
    }
}
