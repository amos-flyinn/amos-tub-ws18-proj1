package com.amos.server;

import android.app.Activity;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.view.View;
import android.widget.Button;

import com.amos.server.signaling.Emitter;
import com.amos.server.signaling.WebServer;
import com.amos.server.webrtc.IPeer;
import com.amos.server.webrtc.PeerWrapper;

import org.java_websocket.server.WebSocketServer;
import org.webrtc.PeerConnection;
import org.webrtc.SurfaceViewRenderer;

import java.net.URI;

public class WebRTCServerActivity extends Activity {

    private PeerConnection localConnection;
    private WebServer webSocketServer;
    private PeerWrapper peerWrapper;
    private Button buttonInit;
    private SurfaceViewRenderer remoteRender;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_server_webrtc);
        this.initViews();
        this.peerWrapper = new PeerWrapper(this);
        this.webSocketServer = new WebServer((IPeer) this.peerWrapper);
        this.peerWrapper.setEmitter((Emitter)this.webSocketServer);
        this.webSocketServer.start();

    }

    public SurfaceViewRenderer getRender(){
        return remoteRender;
    }

    private void initViews(){
        remoteRender = findViewById(R.id.surface_remote_viewer);
    }


}
