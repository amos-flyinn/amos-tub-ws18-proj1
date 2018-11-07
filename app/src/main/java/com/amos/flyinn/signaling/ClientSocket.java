package com.amos.flyinn.signaling;

import android.util.Log;

import org.java_websocket.client.WebSocketClient;
import org.java_websocket.handshake.ServerHandshake;
import org.json.JSONException;
import org.json.JSONObject;
import org.webrtc.IceCandidate;
import org.webrtc.SessionDescription;

import java.net.URI;

public class ClientSocket extends WebSocketClient implements Emitter {


    public ClientSocket(URI serverUri) {

        super(serverUri);
    }

    @Override
    public void onOpen(ServerHandshake handshakedata) {

    }

    @Override
    public void onMessage(String message) {

    }

    @Override
    public void onClose(int code, String reason, boolean remote) {

    }

    @Override
    public void onError(Exception ex) {
        ex.printStackTrace();
    }


    @Override
    public void shareIceCandidate(IceCandidate candidate) {
        try{
            Log.d("ClientSocket","Sending icecandidate! -- " + candidate);
            JSONObject jsonIceCandidate = new JSONObject();
            jsonIceCandidate.put("type", "candidate-client");
            jsonIceCandidate.put("label", candidate.sdpMLineIndex);
            jsonIceCandidate.put("id", candidate.sdpMid);
            jsonIceCandidate.put("candidate", candidate.sdp);
            this.send(jsonIceCandidate.toString());
            Log.d("ClientSocket","simulating the icecandidate message sending : " + jsonIceCandidate);
        }catch (Exception e)
        {
            e.printStackTrace();
        }

    }

    @Override
    public void shareSessionDescription(SessionDescription session) {
        try {
            Log.d("ClientSocket", "sending session description = [" + session + "]");
            JSONObject sessionObject = new JSONObject();
            sessionObject.put("type", session.type.canonicalForm());
            sessionObject.put("sdp", session.description);
            Log.d("ClientSocket","simulating the description message sending : " + sessionObject);
            this.send(sessionObject.toString());
        } catch (JSONException e) {
            e.printStackTrace();
        }
    }
}
