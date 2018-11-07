package com.amos.server.signaling;

import android.util.Log;

import com.amos.server.webrtc.IPeer;

import org.java_websocket.WebSocket;
import org.java_websocket.handshake.ClientHandshake;
import org.java_websocket.server.WebSocketServer;
import org.json.JSONException;
import org.json.JSONObject;
import org.webrtc.IceCandidate;
import org.webrtc.SessionDescription;

import java.net.InetSocketAddress;

public class WebServer extends WebSocketServer implements Emitter {

    private IPeer peer;

    public WebServer(IPeer peer){

        super( new InetSocketAddress( 8080 ));
        this.peer = peer;
    }

    @Override
    public void onOpen(WebSocket conn, ClientHandshake handshake) {

    }

    @Override
    public void onClose(WebSocket conn, int code, String reason, boolean remote) {

    }

    @Override
    public void onMessage(WebSocket conn, String message) {
        Log.d("WebServer","I received this : " + message);
        try{
            JSONObject receivedObject = new JSONObject(message);

            String typeMessage = receivedObject.getString("type-message");

            if(typeMessage.equals("offerClient"))
            {
                String sdp = receivedObject.getString("sdp");
                SessionDescription offerSessionDescriptionClient = new SessionDescription(SessionDescription.Type.OFFER,sdp);
                this.peer.setRemoteDescriptorPeer(offerSessionDescriptionClient);
            }
            else if(typeMessage.equals("candidate-client")) {
                Log.d("WebServer","Getting candidate from client : " + receivedObject.toString());
                String id = receivedObject.getString("id");
                int label = receivedObject.getInt("label");
                String candidate = receivedObject.getString("candidate");
                IceCandidate newCandidate = new IceCandidate(id,label,candidate);
                this.peer.setRemoteIceCandidate(newCandidate);
            }

        }
        catch (Exception e)
        {
            e.printStackTrace();
        }

    }

    @Override
    public void onError(WebSocket conn, Exception ex) {

    }

    @Override
    public void onStart() {

    }

    @Override
    public void shareSessionDescription(SessionDescription session) {
        Log.d("WebServer","Sending session answer : " + session);
        try {
            Log.d("WebServer", "sending session description = [" + session + "]");
            JSONObject sessionObject = new JSONObject();
            sessionObject.put("type-message","answerServer");
            sessionObject.put("type", session.type.canonicalForm());
            sessionObject.put("sdp", session.description);
            Log.d("WebServer","simulating the description message sending answer : " + sessionObject);
            this.broadcast(sessionObject.toString());
        } catch (JSONException e) {
            e.printStackTrace();
        }
    }

    @Override
    public void shareIceCandidate(IceCandidate candidate) {
        Log.d("WebServer","I am sharing this candidate : " + candidate);
        try{
            Log.d("WebServer","Sending icecandidate! -- " + candidate);
            JSONObject jsonIceCandidate = new JSONObject();
            jsonIceCandidate.put("type-message","candidate-server");
            jsonIceCandidate.put("type", "candidate");
            jsonIceCandidate.put("label", candidate.sdpMLineIndex);
            jsonIceCandidate.put("id", candidate.sdpMid);
            jsonIceCandidate.put("candidate", candidate.sdp);
            this.broadcast(jsonIceCandidate.toString());
            Log.d("WebServer","simulating the icecandidate message sending : " + jsonIceCandidate);
        }catch (Exception e) {
            e.printStackTrace();
        }
    }
}
