package com.amos.server.wifimanager;

import android.app.Activity;
import android.net.wifi.p2p.WifiP2pDevice;
import android.net.wifi.p2p.WifiP2pDeviceList;
import android.net.wifi.p2p.WifiP2pManager;

import com.amos.server.P2PActivityServer;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

public class PeersListenerService implements WifiP2pManager.PeerListListener {
    private List<WifiP2pDevice> listOfPeers = new ArrayList<>();
    private P2PActivityServer activity;
    public PeersListenerService(Activity activity) {
        this.activity = (P2PActivityServer)activity;
    }

    @Override
    public void onPeersAvailable(WifiP2pDeviceList wifiP2pDeviceList) {

    }

    public List<WifiP2pDevice> getListOfPeers() {
        return listOfPeers;
    }
}
