package com.amos.flyinn.signaling;

import android.util.Log;

import com.amos.flyinn.webrtc.IPeer;

import org.java_websocket.client.WebSocketClient;
import org.java_websocket.handshake.ServerHandshake;
import org.json.JSONException;
import org.json.JSONObject;
import org.webrtc.IceCandidate;
import org.webrtc.SessionDescription;

import java.net.URI;
import java.security.spec.ECField;

public class ClientSocket extends WebSocketClient implements Emitter {

    private IPeer peer;

    public ClientSocket(URI serverUri, IPeer peer) {

        super(serverUri);
        this.peer = peer;
    }

    @Override
    public void onOpen(ServerHandshake handshakedata) {

    }

    @Override
    public void onMessage(String message) {
        Log.d("ClientSocket","OnMessage -- Received : " + message);
        try{
            JSONObject receiveObject = new JSONObject(message);

            String typeMessage = receiveObject.getString("type-message");

            if (typeMessage.equals("answerServer")) {
                String sdp = receiveObject.getString("sdp");
                SessionDescription answerDescriptionServer = new SessionDescription(SessionDescription.Type.ANSWER, sdp);
                this.peer.setRemoteDescriptorPeer(answerDescriptionServer);
                Log.d("ClientSocket", "Answer SDP setted");
            }else if(typeMessage.equals("candidate-server")){
                Log.d("ClientSocket","Getting candidate from client : " + receiveObject.toString());
                String id = receiveObject.getString("id");
                int label = receiveObject.getInt("label");
                String candidate = receiveObject.getString("candidate");
                IceCandidate newCandidate = new IceCandidate(id,label,candidate);
                this.peer.setRemoteIceCandidate(newCandidate);
            }

        }catch (Exception e) {
            e.printStackTrace();
        }
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
            jsonIceCandidate.put("type-message","candidate-client");
            jsonIceCandidate.put("type", "candidate");
            jsonIceCandidate.put("label", candidate.sdpMLineIndex);
            jsonIceCandidate.put("id", candidate.sdpMid);
            jsonIceCandidate.put("candidate", candidate.sdp);
            this.send(jsonIceCandidate.toString());
            Log.d("ClientSocket","simulating the icecandidate message sending : " + jsonIceCandidate);
        }catch (Exception e) {
            e.printStackTrace();
        }

    }

    @Override
    public void shareSessionDescription(SessionDescription session) {
        try {
            Log.d("ClientSocket", "sending session description = [" + session + "]");
            JSONObject sessionObject = new JSONObject();
            sessionObject.put("type-message","offerClient");
            sessionObject.put("type", session.type.canonicalForm());
            sessionObject.put("sdp", session.description);
            Log.d("ClientSocket","simulating the description message sending : " + sessionObject);
            this.send(sessionObject.toString());
        } catch (JSONException e) {
            e.printStackTrace();
        }
    }
}
