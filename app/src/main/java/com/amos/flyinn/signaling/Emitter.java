package com.amos.flyinn.signaling;

import org.webrtc.IceCandidate;
import org.webrtc.SessionDescription;

public interface Emitter {

    public void shareSessionDescription(SessionDescription session);

    public void shareIceCandidate(IceCandidate candidate);
}
