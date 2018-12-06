package com.amos.server.wifibroadcaster;

import android.Manifest;
import android.annotation.SuppressLint;
import android.content.Context;
import android.content.IntentFilter;
import android.content.pm.PackageManager;
import android.net.wifi.WifiManager;
import android.net.wifi.p2p.WifiP2pManager;
import android.os.Build;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.widget.Toast;

import com.amos.server.R;

import java.lang.reflect.Method;

/**
 * Set up Wifimanager and handle Intents. Calls are passed through to the WifiBroadCasterSingleton.
 */
public abstract class WifiHijackBase extends AppCompatActivity {
    private final IntentFilter intentFilter = new IntentFilter();
    private WifiP2pManager.Channel mChannel;
    private WifiP2pManager mManager;
    private static final int COARSE_LOCATION = 1001;

    /**
     * Register handled intents and get all required permissions for Wifi P2P
     * @param savedInstanceState
     */
    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M && checkSelfPermission(Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            Toast.makeText(this, "Requesting permission for peers", Toast.LENGTH_SHORT).show();
            requestPermissions(new String[]{Manifest.permission.ACCESS_COARSE_LOCATION}, COARSE_LOCATION);
        }
        Log.d("onCreate", "called");

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

        this.disconnect();
    }

    protected void disconnect() {
        try {
            @SuppressLint("WifiManagerLeak")
            WifiManager wifiManager = (WifiManager) getSystemService(Context.WIFI_SERVICE);
            wifiManager.setWifiEnabled(false);
            Thread.sleep(100);
            wifiManager.setWifiEnabled(true);
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

    private void setWifiName(String name) {
        Log.d("ChangeWifiName", "New name:" + name);
        String fullName = getString(R.string.product_name_prefix_identifier) + name;
        try {
            Method method = this.mManager.getClass().getMethod("setDeviceName", new Class[]{WifiP2pManager.Channel.class, String.class, WifiP2pManager.ActionListener.class});
            method.invoke(this.mManager, this.mChannel, fullName, null);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    /**
     * Set the correct wifi name and initiate peer search. Further actions are handled in the
     * WifiBroadcasterSingleton.
     *
     * We change the name of our server network to the one the WifiP2P on the app is expecting.
     * This way we can establish the connection from the server side without further interaction
     * on the client.
     *
     * @param name
     */
    public void changeName(String name) {
        try {
            unregisterReceiver(WifiBroadcasterSingleton.getInstance().getBroadcaster());
            mManager.stopPeerDiscovery(mChannel, null);
        } catch (Exception e) {
            e.printStackTrace();
        }

        this.mChannel = mManager.initialize(this, getMainLooper(), null);
        this.setWifiName(name);
        WifiBroadcasterSingleton.getInstance().setInstance(mManager, mChannel, this);
        registerReceiver(WifiBroadcasterSingleton.getInstance().getBroadcaster(), this.intentFilter);

        Log.d("changeName", "Start discovering");
        this.mManager.discoverPeers(mChannel, new WifiP2pManager.ActionListener() {
            @Override
            public void onSuccess() {
                Toast.makeText(WifiHijackBase.this, "Listening to Peers", Toast.LENGTH_SHORT).show();
            }

            @Override
            public void onFailure(int i) {
                Toast.makeText(WifiHijackBase.this, "Error listening to Peers", Toast.LENGTH_SHORT).show();
            }
        });
    }

    /**
     * Rebind to WifiBroadCasterSingleton on resume.
     */
    @Override
    public void onResume() {
        super.onResume();
        WifiBroadcasterSingleton.getInstance().setInstance(mManager, mChannel, this);
        registerReceiver(WifiBroadcasterSingleton.getInstance().getBroadcaster(), intentFilter);
    }

    /**
     * Do not continue to listen on program pause.
     */
    @Override
    public void onPause() {
        super.onPause();
        unregisterReceiver(WifiBroadcasterSingleton.getInstance().getBroadcaster());
    }

    /**
     * Show visual feedback if permissions for WifiP2P have been correctly granted.
     *
     * @param requestCode
     * @param permissions
     * @param grantResults
     */
    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        if (requestCode == COARSE_LOCATION && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
            Toast.makeText(this, "Permission for peers listening given", Toast.LENGTH_SHORT).show();
        }
    }
}
