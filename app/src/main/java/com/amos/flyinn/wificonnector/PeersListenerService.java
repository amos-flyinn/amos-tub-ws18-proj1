package com.amos.flyinn.wificonnector;

import android.net.wifi.p2p.WifiP2pDevice;
import android.net.wifi.p2p.WifiP2pDeviceList;
import android.net.wifi.p2p.WifiP2pManager;
import android.util.Log;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

/**
 * Handle our list of available peers for P2P.
 */
class PeersListenerService implements WifiP2pManager.PeerListListener {
    private List<WifiP2pDevice> listOfPeers = new ArrayList<>();
    private WifiConnectorBase base;

    /**
     * Create a new listener.
     *
     * @param activity
     */
    PeersListenerService(WifiConnectorBase activity) {
        base = activity;
    }

    /**
     * Trigger changes to our list of peers after an update to available peers.
     *
     * @param wifiP2pDeviceList
     */
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
