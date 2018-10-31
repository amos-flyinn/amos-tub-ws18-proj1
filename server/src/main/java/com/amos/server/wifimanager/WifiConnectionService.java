package com.amos.server.wifimanager;

import android.app.Activity;
import android.net.wifi.p2p.WifiP2pInfo;
import android.net.wifi.p2p.WifiP2pManager;
import android.util.Log;
import android.widget.Toast;

import com.amos.server.P2PActivityServer;

import java.net.InetAddress;
import java.net.NetworkInterface;
import java.net.SocketException;
import java.util.Enumeration;

public class WifiConnectionService implements WifiP2pManager.ConnectionInfoListener {

    private P2PActivityServer activity;

    public WifiConnectionService(Activity activity) {
       this.activity = (P2PActivityServer)activity;
    }

    @Override
    public void onConnectionInfoAvailable(WifiP2pInfo wifiP2pInfo) {


        InetAddress address = wifiP2pInfo.groupOwnerAddress;
        Toast.makeText(activity,"Connected to the address : " + address.getHostAddress(),Toast.LENGTH_LONG).show();
        try {
            Enumeration<NetworkInterface> en = NetworkInterface.getNetworkInterfaces();
            while(en.hasMoreElements()) {
                NetworkInterface inet = en.nextElement();
                Log.d("WifiConnectionService",inet.getInterfaceAddresses().toString());
            }
        } catch (SocketException e) {
            e.printStackTrace();
        }

    }
}
