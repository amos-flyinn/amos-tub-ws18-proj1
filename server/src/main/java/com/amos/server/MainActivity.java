package com.amos.server;

import android.app.Activity;
import android.content.Intent;
import android.os.Handler;
import android.os.Looper;
import android.os.Message;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.MotionEvent;
import android.widget.Button;
import android.widget.TextView;
import android.content.Intent;

import org.w3c.dom.Text;
import org.webrtc.PeerConnection;
import org.webrtc.SurfaceViewRenderer;

import android.view.View;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;

import com.amos.server.eventsender.EventServer;
import com.amos.server.signaling.Emitter;
import com.amos.server.signaling.WebServer;
import com.amos.server.webrtc.IPeer;
import com.amos.server.webrtc.PeerWrapper;
import com.amos.shared.TouchEvent;

import java.util.concurrent.BlockingQueue;
import java.util.concurrent.LinkedBlockingDeque;
import java.util.concurrent.LinkedBlockingQueue;

public class MainActivity extends Activity {
    TextView connectionInfo;
    Button threadStarter;
    Thread senderRunner;
    EventServer eventSender;
    BlockingQueue<TouchEvent> msgQueue;
    Handler uiHandler;
    SurfaceViewRenderer view;

    private PeerConnection localConnection;
    private WebServer webSocketServer;
    private PeerWrapper peerWrapper;
    private Button buttonInit;
    private SurfaceViewRenderer remoteRender;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        connectionInfo = findViewById(R.id.connectionInfo);
        connectionInfo.setVisibility(View.INVISIBLE);
        view = findViewById(R.id.surface_remote_viewer);
        // create touch listener components
        msgQueue = new LinkedBlockingQueue<>();
        uiHandler = new Handler(Looper.getMainLooper()) {
            @Override
            public void handleMessage(Message msg) {
                super.handleMessage(msg);
                connectionInfo.setText((String) msg.obj);
            }
        };
        eventSender = new EventServer(msgQueue, uiHandler);
        threadStarter = findViewById(R.id.threadStarter);
        threadStarter.setVisibility(View.INVISIBLE);
        // threadStarter.setOnClickListener((View v) -> {
        //     if (senderRunner == null) {
        //         senderRunner = new Thread(eventSender);
        //         senderRunner.start();
        //         threadStarter.setText("Stop server");
        //     } else {
        //         eventSender.close();
        //         try {
        //             senderRunner.join();
        //             threadStarter.setText("Start server");
        //             senderRunner = null;
        //         } catch (InterruptedException e) {
        //         }
        //     }
        // });

        //init WebRTC Signaling server
        this.initViews();
        this.peerWrapper = new PeerWrapper(this);
        this.webSocketServer = new WebServer((IPeer) this.peerWrapper);
        this.peerWrapper.setEmitter((Emitter)this.webSocketServer);
        this.webSocketServer.start();

        senderRunner = new Thread(eventSender);
        senderRunner.start();
        view.setOnTouchListener(
                (View v, MotionEvent e) -> {
                    e.setLocation(e.getX() / view.getWidth(), e.getY() / view.getHeight());
                    TouchEvent te = new TouchEvent(e.getX(), e.getY(), e.getAction(), e.getDownTime());
                    msgQueue.add(te);
                    return true;
                }
        );
    }

    public SurfaceViewRenderer getRender(){
        return remoteRender;
    }

    private void initViews(){
        remoteRender = findViewById(R.id.surface_remote_viewer);
    }

    @Override
    protected void onStart() {
        super.onStart();
        connectionInfo.setText("Waiting for P2P Wifi connection");
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        MenuInflater inflater = getMenuInflater();
        inflater.inflate(R.menu.main_menu, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        Intent intent;
        switch (item.getItemId()) {
            case R.id.p2pserver_activity:
                intent = new Intent(this,P2PActivityServer.class);
                break;
            case R.id.webrtc_server_activity:
                intent = new Intent(this,WebRTCServerActivity.class);
                break;
            case R.id.event_grab_activity:
                intent = new Intent(MainActivity.this, EventGrabDemo.class);
                break;
            case R.id.event_sender_activity:
                intent = new Intent(MainActivity.this, EventSenderDemo.class);
                break;
            case R.id.build_info_activity:
                intent = new Intent(this, BuildInfoActivity.class);
                break;
            default:
                return super.onOptionsItemSelected(item);
        }

        startActivity(intent);
        return super.onOptionsItemSelected(item);
    }



}
