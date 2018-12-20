package com.amos.server.webrtc;

import com.amos.server.signaling.WebServer;

import org.java_websocket.WebSocket;
import org.json.JSONException;
import org.json.JSONObject;
import org.junit.Before;
import org.junit.Test;
import org.mockito.ArgumentCaptor;
import org.mockito.Mockito;
import org.webrtc.IceCandidate;
import org.webrtc.SessionDescription;

import static org.junit.Assert.assertEquals;

/**
 * Test the signaling server for WebRTC negotiation
 */
public class SignalingMessagesTest {

    private WebServer socketSevice;
    private String simulatedIP = "ws://127.0.0.1";
    private IPeer peer;
    private WebSocket socketWeb;

    @Before
    public void initSignalingService() {
        peer = Mockito.mock(IPeer.class);
        socketSevice = new WebServer(peer);
        socketWeb = Mockito.mock(WebSocket.class);
    }

    @Test
    public void signaling_messages_remote_descriptor() {
        ArgumentCaptor<SessionDescription> argument = ArgumentCaptor.forClass(SessionDescription.class);
        JSONObject sessionDescriptorMessage = new JSONObject();
        try {
            sessionDescriptorMessage.put("type-message", "offerClient");
            sessionDescriptorMessage.put("sdp", "TestSDP");
        } catch (JSONException e) {
            e.printStackTrace();
        }
        this.socketSevice.onMessage(socketWeb, sessionDescriptorMessage.toString());

        Mockito.verify(peer).setRemoteDescriptorPeer(argument.capture());

        SessionDescription sessionAfter = argument.getValue();

        assertEquals(sessionAfter.description, "TestSDP");
        assertEquals(sessionAfter.type, SessionDescription.Type.OFFER);
    }

    @Test
    public void signaling_messages_ice_candidate() {
        ArgumentCaptor<IceCandidate> argument = ArgumentCaptor.forClass(IceCandidate.class);
        JSONObject sessionDescriptorMessage = new JSONObject();
        try {
            sessionDescriptorMessage.put("type-message", "candidate-client");
            sessionDescriptorMessage.put("id", "idIceCandidate");
            sessionDescriptorMessage.put("label", 20);
            sessionDescriptorMessage.put("candidate", "IceCandidate");
        } catch (JSONException e) {
            e.printStackTrace();
        }
        this.socketSevice.onMessage(socketWeb, sessionDescriptorMessage.toString());

        Mockito.verify(peer).setRemoteIceCandidate(argument.capture());

        IceCandidate iceCandidateAfter = argument.getValue();

        assertEquals(iceCandidateAfter.sdp, "IceCandidate");
        assertEquals(iceCandidateAfter.sdpMid, "idIceCandidate");
        assertEquals(iceCandidateAfter.sdpMLineIndex, 20);

    }


    @Test
    public void signaling_messages_serialize_session_description() {
        SessionDescription session = new SessionDescription(SessionDescription.Type.ANSWER, "DescriptionTest");

        JSONObject objectSerialize = socketSevice.serializeDescription(session);
        String description = null;
        String typeMessage = null;
        String type = null;
        try {
            description = objectSerialize.getString("sdp");
            typeMessage = objectSerialize.getString("type-message");
            type = objectSerialize.getString("type");
        } catch (JSONException e) {
            e.printStackTrace();
        }

        assertEquals(description, session.description);
        assertEquals(type, session.type.canonicalForm());
        assertEquals(typeMessage, "answerServer");

    }

    @Test
    public void signaling_messages_serialize_ice_candidate() {
        IceCandidate candidate = new IceCandidate("idIceCandidate", 20, "iceCandidate");

        JSONObject objectSerialize = this.socketSevice.serializeIceCandidate(candidate);
        String typeMessage = null;
        String type = null;
        int label = 0;
        String id = null;
        String candidateMessage = null;
        try {
            typeMessage = objectSerialize.getString("type-message");
            type = objectSerialize.getString("type");
            label = objectSerialize.getInt("label");
            id = objectSerialize.getString("id");
            candidateMessage = objectSerialize.getString("candidate");

        } catch (JSONException e) {
            e.printStackTrace();
        }


        assertEquals(typeMessage, "candidate-server");
        assertEquals(type, "candidate");
        assertEquals(label, candidate.sdpMLineIndex);
        assertEquals(id, candidate.sdpMid);
        assertEquals(candidateMessage, candidate.sdp);

    }

    @Test
    public void fail_signaling_messages_serialize_ice_candidate() {
        JSONObject candidate = null;
        assertEquals(candidate, null);
    }

    @Test
    public void fail_signaling_messages_serialize_session_descriptor() {
        JSONObject sessionObject = null;
        assertEquals(sessionObject, null);
    }


}