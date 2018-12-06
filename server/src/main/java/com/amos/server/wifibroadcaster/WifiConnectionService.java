package com.amos.server.wifibroadcaster;

import android.app.Activity;
import android.content.Intent;
import android.net.wifi.p2p.WifiP2pInfo;
import android.net.wifi.p2p.WifiP2pManager;
import android.util.Log;
import android.widget.Toast;

import com.amos.server.ConnectionSetupServerActivity;

import java.net.InetAddress;
import java.net.NetworkInterface;
import java.net.SocketException;
import java.util.Enumeration;

/**
 * Process wifi connection information and setup further activity switch to setup connections between Event and Screen sharing processes.
 */
class WifiConnectionService implements WifiP2pManager.ConnectionInfoListener {
    private Activity activity;
    private WifiP2pManager.Channel mChannel;
    private WifiP2pManager mManager;

    /**
     * Create a new connection service.
     * @param activity
     * @param manager
     * @param channel
     */
    WifiConnectionService(Activity activity, WifiP2pManager manager, WifiP2pManager.Channel channel) {
        this.activity = activity;
        this.mChannel = channel;
        this.mManager = manager;
    }

    /**
     * Display information on established connection (ie accepted invitation to P2P network) and trigger activity change.
     * @param wifiP2pInfo
     */
    @Override
    public void onConnectionInfoAvailable(WifiP2pInfo wifiP2pInfo) {
        InetAddress address = wifiP2pInfo.groupOwnerAddress;
        Toast.makeText(activity, "Connected to the address : " + address.getHostAddress(), Toast.LENGTH_LONG).show();
        Toast.makeText(activity, "Closing discovery mode for peers" + address.getHostAddress(), Toast.LENGTH_LONG).show();
        mManager.stopPeerDiscovery(mChannel, new WifiP2pManager.ActionListener() {
            @Override
            public void onSuccess() {
                Toast.makeText(activity, "Discovery mode closed Succeessfully", Toast.LENGTH_LONG).show();
            }

            @Override
            public void onFailure(int i) {
                Toast.makeText(activity, "Failed to close Discovery mode", Toast.LENGTH_LONG).show();
            }
        });
        try {
            Enumeration<NetworkInterface> en = NetworkInterface.getNetworkInterfaces();
            while (en.hasMoreElements()) {
                NetworkInterface inet = en.nextElement();
                Log.d("WifiConnectionService", inet.getInterfaceAddresses().toString());
            }
        } catch (SocketException e) {
            e.printStackTrace();
        }
        // TODO(ich): Handle staring connection
        setupNext();
    }

    /**
     * Start ConnectionSetupActivity.
     *
     * This function is called after connection information has been made available, which is a callback
     * defined in the WifiP2P library.
     */
    private void setupNext(){
        Intent intent = new Intent(activity,ConnectionSetupServerActivity.class);
        activity.startActivity(intent);
    }
}
