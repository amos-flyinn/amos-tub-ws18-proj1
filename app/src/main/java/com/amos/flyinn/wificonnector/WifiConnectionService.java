package com.amos.flyinn.wificonnector;

import android.content.Context;
import android.content.Intent;
import android.net.wifi.p2p.WifiP2pInfo;
import android.net.wifi.p2p.WifiP2pManager;
import android.util.Log;
import android.widget.Toast;


import com.amos.flyinn.ConnectionSetupActivity;

import java.net.InetAddress;
import java.net.NetworkInterface;
import java.net.SocketException;
import java.util.Enumeration;

/**
 * Handle an established P2P connection.
 */
class WifiConnectionService implements WifiP2pManager.ConnectionInfoListener {
    private Context activity;
    private WifiP2pManager.Channel mChannel;
    private WifiP2pManager mManager;
    private InetAddress address;

    /**
     * Create a new connection service.
     * @param activity
     * @param manager
     * @param channel
     */
    WifiConnectionService(Context activity, WifiP2pManager manager, WifiP2pManager.Channel channel) {
        this.activity = activity;
        this.mChannel = channel;
        this.mManager = manager;
    }

    /**
     * Get address of the server we are connected to.
     * @return
     * @throws Exception
     */
    public String getServerAddress() throws Exception {
        if (address != null) {
            return address.getHostAddress();
        }
        throw new Exception("Host not ready");
    }

    /**
     * Display connection information and trigger activity change.
     * @param wifiP2pInfo
     */
    @Override
    public void onConnectionInfoAvailable(WifiP2pInfo wifiP2pInfo) {
        this.address = wifiP2pInfo.groupOwnerAddress;
        Log.d("P2P", "Connected to the address : " + address.getHostAddress());
        Log.d("P2P", "Closing discovery mode for peers");

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

        setupNext();

    }


    /**
     * Switch to connection setup.
     */
    private void setupNext(){
        Intent intent = new Intent(activity,ConnectionSetupActivity.class);
        activity.startActivity(intent);
    }

}
