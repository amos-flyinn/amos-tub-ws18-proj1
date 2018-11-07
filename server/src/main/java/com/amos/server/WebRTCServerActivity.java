package com.amos.server;

import android.app.Activity;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.view.View;
import android.widget.Button;

import com.amos.server.signaling.Emitter;
import com.amos.server.signaling.WebServer;
import com.amos.server.webrtc.PeerWrapper;

import org.java_websocket.server.WebSocketServer;
import org.webrtc.PeerConnection;

import java.net.URI;

public class WebRTCServerActivity extends Activity {

    private PeerConnection localConnection;
    private WebServer webSocketServer;
    private PeerWrapper peerWrapper;
    private Button buttonInit;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_server_webrtc);
        this.webSocketServer = new WebServer();
        this.webSocketServer.start();
        this.peerWrapper = new PeerWrapper((Emitter) this.webSocketServer, this.getApplicationContext());
    }
}
