package com.amos.flyinn.webrtc;

import android.util.Log;

import org.webrtc.SessionDescription;



public class SdpObserver implements org.webrtc.SdpObserver {

    private String tag = "SdpObserver";

    @Override
    public void onCreateSuccess(SessionDescription sessionDescription) {
        Log.d(tag, "onCreateSuccess() called with: sessionDescription = [" + sessionDescription + "]");
        Log.d(tag,sessionDescription.description);
        Log.d(tag,sessionDescription.type.canonicalForm());
    }

    @Override
    public void onSetSuccess() {
        Log.d(tag, "onSetSuccess() called");
    }

    @Override
    public void onCreateFailure(String s) {
        Log.d(tag, "onCreateFailure() called with: s = [" + s + "]");
    }

    @Override
    public void onSetFailure(String s) {
        Log.d(tag, "onSetFailure() called with: s = [" + s + "]");
    }

}
