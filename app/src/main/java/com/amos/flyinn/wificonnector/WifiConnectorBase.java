package com.amos.flyinn.wificonnector;

import android.Manifest;
import android.annotation.SuppressLint;
import android.content.Context;
import android.content.IntentFilter;
import android.content.pm.PackageManager;
import android.net.wifi.WifiManager;
import android.net.wifi.p2p.WifiP2pConfig;
import android.net.wifi.p2p.WifiP2pDevice;
import android.net.wifi.p2p.WifiP2pManager;
import android.os.Build;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.widget.Toast;

import java.lang.reflect.Method;
import java.util.List;

public abstract class WifiConnectorBase extends AppCompatActivity {
    private final IntentFilter intentFilter = new IntentFilter();
    private WifiP2pManager.Channel mChannel;
    private WifiP2pManager mManager;
    private static final int COARSE_LOCATION = 1001;

    private final Object lock = new Object();
    private boolean connected = false; // Locked

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M && checkSelfPermission(Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            Toast.makeText(this, "Requesting permission for peers", Toast.LENGTH_SHORT).show();
            requestPermissions(new String[]{Manifest.permission.ACCESS_COARSE_LOCATION}, COARSE_LOCATION);
        }
        intentFilter.addAction(WifiP2pManager.WIFI_P2P_STATE_CHANGED_ACTION);
        intentFilter.addAction(WifiP2pManager.WIFI_P2P_PEERS_CHANGED_ACTION);
        intentFilter.addAction(WifiP2pManager.WIFI_P2P_DISCOVERY_CHANGED_ACTION);
        intentFilter.addAction(WifiP2pManager.WIFI_P2P_CONNECTION_CHANGED_ACTION);
        intentFilter.addAction(WifiP2pManager.WIFI_P2P_THIS_DEVICE_CHANGED_ACTION);
        intentFilter.addAction(WifiP2pManager.EXTRA_WIFI_P2P_DEVICE);
        intentFilter.addAction(WifiP2pManager.WIFI_P2P_STATE_CHANGED_ACTION);

        this.mManager = (WifiP2pManager) getSystemService(Context.WIFI_P2P_SERVICE);
        this.mChannel = mManager.initialize(this, getMainLooper(), null);

        this.disconnect();

        this.mManager.discoverPeers(mChannel, new WifiP2pManager.ActionListener() {
            @Override
            public void onSuccess() {
                Toast.makeText(WifiConnectorBase.this, "Listening to Peers", Toast.LENGTH_SHORT).show();
            }

            @Override
            public void onFailure(int i) {
                Toast.makeText(WifiConnectorBase.this, "Error listening to Peers", Toast.LENGTH_SHORT).show();
                Log.d("WifiP2PActivity", "Error listening to peers : " + i);
            }
        });
        new Thread(new Runnable() {
            @Override
            public void run() {
                try {
                    while (true) {
                        Thread.sleep(1000 * 10);
                        synchronized (WifiConnectorBase.this.lock) {
                            if (!WifiConnectorBase.this.connected) {
                                totalRefresh();
                            }
                        }
                    }
                } catch (Exception e) {
                }
            }
        }).start();
    }

    protected void disconnect() {
        try {
            @SuppressLint("WifiManagerLeak")
            WifiManager wifiManager = (WifiManager) getSystemService(Context.WIFI_SERVICE);
            wifiManager.setWifiEnabled(true);
            wifiManager.setWifiEnabled(false);
        } catch (Exception e) {
        }

        try {
            Method[] methods = WifiP2pManager.class.getMethods();
            for (int i = 0; i < methods.length; i++) {
                if (methods[i].getName().equals("deletePersistentGroup")) {
                    // Delete any persistent group
                    for (int netid = 0; netid < 32; netid++) {
                        methods[i].invoke(mManager, mChannel, netid, null);
                    }
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    protected void onDisconnected() {
        synchronized (this.lock) {
            Log.d("onDisconnected", "just disconnected");
            this.connected = false;
        }
    }


    protected void connectToPeer(WifiP2pDevice deviceToConnect) {
        synchronized (this.lock) {
            if (this.connected)
                return;
            this.connected = true;
            Log.d("connectToPeer", "Connecting to" + deviceToConnect.deviceName);
            Toast.makeText(this, "Connected", Toast.LENGTH_SHORT).show();

            WifiP2pConfig newConfigWifi = new WifiP2pConfig();
            newConfigWifi.deviceAddress = deviceToConnect.deviceAddress;
            this.mManager.connect(mChannel, newConfigWifi, new WifiP2pManager.ActionListener() {
                @Override
                public void onSuccess() {
                    synchronized (WifiConnectorBase.this.lock) {
                        WifiConnectorBase.this.connected = true;
                    }
                    Toast.makeText(WifiConnectorBase.this, "Connection Successful", Toast.LENGTH_SHORT).show();
                }

                @Override
                public void onFailure(int i) {
                    synchronized (WifiConnectorBase.this.lock) {
                        WifiConnectorBase.this.connected = false;
                    }
                    Toast.makeText(WifiConnectorBase.this, "Error connecting to device!" + i, Toast.LENGTH_SHORT).show();
                }
            });
            this.mManager.stopPeerDiscovery(this.mChannel, null);
        }
    }

    public void totalRefresh() {
        try {
            unregisterReceiver(WifiConnectorSingelton.getInstance().getWifiReceiverP2P());
        } catch (Exception e) {
        }
        this.mManager.stopPeerDiscovery(this.mChannel, null);

        this.mManager = (WifiP2pManager) getSystemService(Context.WIFI_P2P_SERVICE);
        this.mChannel = mManager.initialize(this, getMainLooper(), null);
        this.mManager.discoverPeers(mChannel, new WifiP2pManager.ActionListener() {
            @Override
            public void onSuccess() {
                Toast.makeText(WifiConnectorBase.this, "Listening to Peers", Toast.LENGTH_SHORT).show();
            }

            @Override
            public void onFailure(int i) {
                Toast.makeText(WifiConnectorBase.this, "Error listening to Peers", Toast.LENGTH_SHORT).show();
                Log.d("WifiP2PActivity", "Error listening to peers : " + i);
            }
        });
        WifiConnectorSingelton.getInstance().setWifiReceiverP2P(mManager, mChannel, this);
        registerReceiver(WifiConnectorSingelton.getInstance().getWifiReceiverP2P(), intentFilter);
    }

    @Override
    public void onResume() {
        super.onResume();
        WifiConnectorSingelton.getInstance().setWifiReceiverP2P(mManager, mChannel, this);
        registerReceiver(WifiConnectorSingelton.getInstance().getWifiReceiverP2P(), intentFilter);
    }

    @Override
    public void onPause() {
        super.onPause();
        unregisterReceiver(WifiConnectorSingelton.getInstance().getWifiReceiverP2P());
    }

    abstract public void setPeers(List<WifiP2pDevice> listOfPeers);

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        if (requestCode == COARSE_LOCATION && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
            Toast.makeText(this, "Permission for peers listening given", Toast.LENGTH_SHORT).show();
        }
    }
}
