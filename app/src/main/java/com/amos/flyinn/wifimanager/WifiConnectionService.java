package com.amos.flyinn.wifimanager;

import android.app.Activity;
import android.net.wifi.p2p.WifiP2pInfo;
import android.net.wifi.p2p.WifiP2pManager;
import android.util.Log;
import android.widget.Toast;

import com.amos.flyinn.WifiP2PActivity;

import java.net.InetAddress;
import java.net.NetworkInterface;
import java.net.SocketException;
import java.util.Enumeration;

public class WifiConnectionService implements WifiP2pManager.ConnectionInfoListener {

    private WifiP2PActivity activity;
    private WifiP2pManager.Channel mChannel;
    private WifiP2pManager mManager;

    public WifiConnectionService(Activity activity,WifiP2pManager manager,WifiP2pManager.Channel channel) {

        this.activity = (WifiP2PActivity)activity;
        this.mChannel = channel;
        this.mManager = manager;

    }

    @Override
    public void onConnectionInfoAvailable(WifiP2pInfo wifiP2pInfo) {


        InetAddress address = wifiP2pInfo.groupOwnerAddress;
        Toast.makeText(activity,"Connected to the address : " + address.getHostAddress(),Toast.LENGTH_LONG).show();
        Toast.makeText(activity,"Closing discovery mode for peers" + address.getHostAddress(),Toast.LENGTH_LONG).show();
        mManager.stopPeerDiscovery(mChannel, new WifiP2pManager.ActionListener() {
            @Override
            public void onSuccess() {
                Toast.makeText(activity,"Discovery mode closed Succeessfully",Toast.LENGTH_LONG).show();
            }

            @Override
            public void onFailure(int i) {
                Toast.makeText(activity,"Failed to close Discovery mode",Toast.LENGTH_LONG).show();
            }
        });
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
