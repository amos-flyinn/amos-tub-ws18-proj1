package com.amos.server.signaling;

import android.util.Log;

import org.java_websocket.WebSocket;
import org.java_websocket.handshake.ClientHandshake;
import org.java_websocket.server.WebSocketServer;
import org.webrtc.IceCandidate;
import org.webrtc.SessionDescription;

import java.net.InetSocketAddress;

public class WebServer extends WebSocketServer implements Emitter {


    public WebServer(){
        super( new InetSocketAddress( 8080 ));
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
    }

    @Override
    public void onError(WebSocket conn, Exception ex) {

    }

    @Override
    public void onStart() {

    }

    @Override
    public void shareSessionDescription(SessionDescription session) {

    }

    @Override
    public void shareIceCandidate(IceCandidate candidate) {

    }
}
