package com.amos.flyinn.signaling;

import android.util.Log;

import com.amos.flyinn.webrtc.IPeer;

import org.java_websocket.client.WebSocketClient;
import org.java_websocket.handshake.ServerHandshake;
import org.json.JSONObject;
import org.webrtc.IceCandidate;
import org.webrtc.SessionDescription;

import java.net.URI;

public class ClientSocket extends WebSocketClient implements Emitter {

    private IPeer peer;

    public ClientSocket(URI serverUri, IPeer peer) {

        super(serverUri);
        this.peer = peer;
    }

    @Override
    public void onOpen(ServerHandshake handshakedata) {

    }


    /**
     * This method receive the information that was sent by the signaling server.
     * It is also responsible to unserialize the messages and set the session descriptor and
     * the ice candidate when received
     *
     * @param message the message information as string that contains the serialized message.
     */
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

    /**
     * This method is useful to serialize an ice candidate object that is supposed to be read
     * by the signaling server.
     *
     * @param candidate candidate that is going to be serialized
     * @return JsonObject returns the JsonObject that contains information about some ice candidate
     */
    public JSONObject serializeIceCandidate(IceCandidate candidate){
        try{
            JSONObject jsonIceCandidate = new JSONObject();
            jsonIceCandidate.put("type-message","candidate-client");
            jsonIceCandidate.put("type", "candidate");
            jsonIceCandidate.put("label", candidate.sdpMLineIndex);
            jsonIceCandidate.put("id", candidate.sdpMid);
            jsonIceCandidate.put("candidate", candidate.sdp);
            return jsonIceCandidate;
        }catch (Exception e){
            e.printStackTrace();
        }

        return null;
    }

    /**
     * This method is responsible to send the serialized ice candidate to the signaling server.
     *
     * @param candidate candidate that is going to be shared with the signaling server.
     */
    @Override
    public void shareIceCandidate(IceCandidate candidate) {
            Log.d("ClientSocket","Sending icecandidate! -- " + candidate);
            JSONObject jsonIceCandidate = this.serializeIceCandidate(candidate);
            this.send(jsonIceCandidate.toString());

    }
    /**
     * This method is useful to serialize serial descriptor object that is supposed to be read
     * by the signaling server.
     *
     * @param session Session descriptor that is going to be serialized
     * @return JsonObject returns the JsonObject that contains information about some session descriptor
     */
    public JSONObject serializeSessionDescription(SessionDescription session)
    {
        try{
            JSONObject sessionObject = new JSONObject();
            sessionObject.put("type-message","offerClient");
            sessionObject.put("type", session.type.canonicalForm());
            sessionObject.put("sdp", session.description);
            return sessionObject;
        }
        catch (Exception e)
        {
            e.printStackTrace();
        }

        return null;
    }


    /**
     * This method is responsible to send the serialized session descriptor to the signaling server.
     *
     * @param session session descriptor that is going to be shared with the signaling server.
     */
    @Override
    public void shareSessionDescription(SessionDescription session) {

            Log.d("ClientSocket", "sending session description = [" + session + "]");
            JSONObject sessionObject = this.serializeSessionDescription(session);
            this.send(sessionObject.toString());

    }
}
