package com.amos.server;

import com.amos.server.wifimanager.WifiServiceManager;
import com.amos.server.eventsender.EventServer;
import com.amos.shared.TouchEvent;

import android.Manifest;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.text.method.Touch;
import android.view.MotionEvent;
import android.view.View;
import android.view.View.OnTouchListener;
import android.util.Log;
import android.os.Build;
import android.widget.Toast;
import android.content.Context;
import android.content.IntentFilter;
import android.content.pm.PackageManager;
import android.net.wifi.p2p.WifiP2pManager;

import java.util.concurrent.BlockingQueue;
import java.util.concurrent.LinkedBlockingQueue;


public class EventSenderDemo extends AppCompatActivity {

    View base;

    BlockingQueue<TouchEvent> mq;

    private final IntentFilter intentFilter = new IntentFilter();
    private WifiP2pManager.Channel mChannel;
    private WifiP2pManager mManager;
    private WifiServiceManager receiver;
    private static final int COARSE_LOCATION = 1001;

    protected void createP2P(Context context) {
        if(Build.VERSION.SDK_INT >= Build.VERSION_CODES.M && checkSelfPermission(Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED){
            Toast.makeText(this,"Requesting permission for peers",Toast.LENGTH_SHORT).show();
            requestPermissions(new String[]{Manifest.permission.ACCESS_COARSE_LOCATION}, COARSE_LOCATION);
        }
        // Indicates a change in the Wi-Fi P2P status.
        intentFilter.addAction(WifiP2pManager.WIFI_P2P_STATE_CHANGED_ACTION);
        // Indicates a change in the list of available peers.
        intentFilter.addAction(WifiP2pManager.WIFI_P2P_PEERS_CHANGED_ACTION);
        // Indicates the state of Wi-Fi P2P connectivity has changed.
        intentFilter.addAction(WifiP2pManager.WIFI_P2P_CONNECTION_CHANGED_ACTION);
        // Indicates this device's details have changed.
        intentFilter.addAction(WifiP2pManager.WIFI_P2P_THIS_DEVICE_CHANGED_ACTION);
        this.mManager = (WifiP2pManager) getSystemService(Context.WIFI_P2P_SERVICE);
        this.mChannel = mManager.initialize(this, getMainLooper(), null);
        //Making the smartphone in discovery mode for other peers
        this.mManager.discoverPeers(mChannel, new WifiP2pManager.ActionListener() {
            @Override
            public void onSuccess() {
                Toast.makeText(context, "Listening to Peers", Toast.LENGTH_SHORT).show();
            }
            @Override
            public void onFailure(int i) {
                Toast.makeText(context,"Error listening to Peers", Toast.LENGTH_SHORT).show();
            }
        });

    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_event_sender_demo);
        base = findViewById(R.id.senderlayout);
        mq = new LinkedBlockingQueue<TouchEvent>();
        createP2P(this);
    }

    @Override
    protected void onStart() {
        super.onStart();
        new Thread(new EventServer(mq)).start();
        base.setOnTouchListener(
                new OnTouchListener() {
                    @Override
                    public boolean onTouch(View v, MotionEvent event) {
                        event.setLocation(event.getX() / base.getWidth(), event.getY() / base.getHeight());
                        TouchEvent te = new TouchEvent(event.getX(), event.getY(), event.getAction(), event.getDownTime());
                        mq.add(te);
                        return true;
                    }
                }
        );
    }

    @Override
    protected void onStop() {
        super.onStop();
    }
}
