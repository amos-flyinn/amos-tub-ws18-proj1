package com.amos.server;

import android.Manifest;
import android.app.Activity;
import android.content.Context;
import android.content.IntentFilter;
import android.content.pm.PackageManager;
import android.net.wifi.p2p.WifiP2pManager;
import android.os.Build;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.view.Menu;
import android.widget.Toast;

import com.amos.server.wifimanager.WifiBradcasterSingelton;

import java.lang.reflect.Method;


public class P2PActivityServer extends Activity {
    private final IntentFilter intentFilter = new IntentFilter();
    private WifiP2pManager.Channel mChannel;
    private WifiP2pManager mManager;
    private static final int COARSE_LOCATION = 1001;


    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M && checkSelfPermission(Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            Toast.makeText(this, "Requesting permission for peers", Toast.LENGTH_SHORT).show();
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
        this.setWifiName("1234");

        //Making the smartphone in discovery mode for other peers
        this.mManager.discoverPeers(mChannel, new WifiP2pManager.ActionListener() {
            @Override
            public void onSuccess() {
                Toast.makeText(P2PActivityServer.this, "Listening to Peers", Toast.LENGTH_SHORT).show();
            }

            @Override
            public void onFailure(int i) {
                Toast.makeText(P2PActivityServer.this, "Error listening to Peers", Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void setWifiName(String name) {
        String fullName = "flyinn-" + name;
        try {
            Method method = this.mManager.getClass().getMethod(
                    "setDeviceName",
                    new Class[]{WifiP2pManager.Channel.class, String.class,
                            WifiP2pManager.ActionListener.class});

            method.invoke(this.mManager, this.mChannel, fullName, new WifiP2pManager.ActionListener() {
                public void onSuccess() {
                }

                public void onFailure(int reason) {
                }
            });
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        return super.onCreateOptionsMenu(menu);
    }


    @Override
    public void onResume() {
        super.onResume();
        WifiBradcasterSingelton.getInstance().setInstance(mManager, mChannel, this);
        registerReceiver(WifiBradcasterSingelton.getInstance().getBroadcaster(), intentFilter);
    }

    @Override
    public void onPause() {
        super.onPause();
        unregisterReceiver(WifiBradcasterSingelton.getInstance().getBroadcaster());
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        if (requestCode == COARSE_LOCATION && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
            Toast.makeText(this, "Permission for peers listening given", Toast.LENGTH_SHORT).show();
        }

    }
}
