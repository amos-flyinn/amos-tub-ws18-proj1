package com.amos.flyinn.wificonnector;

import android.net.wifi.p2p.WifiP2pDevice;
import android.net.wifi.p2p.WifiP2pDeviceList;
import android.net.wifi.p2p.WifiP2pManager;
import android.util.Log;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

class PeersListenerService implements WifiP2pManager.PeerListListener {
    private List<WifiP2pDevice> listOfPeers = new ArrayList<>();
    private WifiConnectorBase base;

    PeersListenerService(WifiConnectorBase activity) {
        base = activity;
    }

    @Override
    public void onPeersAvailable(WifiP2pDeviceList wifiP2pDeviceList) {
        Log.d("PeersListenerService", "Called onPeersAvailable");
        wifiP2pDeviceList.getDeviceList();
        Collection<WifiP2pDevice> collectionDevices = wifiP2pDeviceList.getDeviceList();
        listOfPeers.clear();
        listOfPeers.addAll(collectionDevices);
        this.base.setPeers(this.listOfPeers);
    }
}
