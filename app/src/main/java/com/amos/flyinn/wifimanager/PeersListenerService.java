package com.amos.flyinn.wifimanager;

import android.app.Activity;
import android.net.wifi.p2p.WifiP2pDevice;
import android.net.wifi.p2p.WifiP2pDeviceList;
import android.net.wifi.p2p.WifiP2pManager;

import com.amos.flyinn.WifiP2PActivity;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

public class PeersListenerService implements WifiP2pManager.PeerListListener {
    private List<WifiP2pDevice> listOfPeers = new ArrayList<>();
    private WifiP2PActivity activity;
    public PeersListenerService(Activity activity) {
        this.activity = (WifiP2PActivity)activity;
    }

    @Override
    public void onPeersAvailable(WifiP2pDeviceList wifiP2pDeviceList) {

        Collection<WifiP2pDevice> collectionDevices = wifiP2pDeviceList.getDeviceList();

        if (!collectionDevices.equals(listOfPeers)) {
            listOfPeers.clear();
            listOfPeers.addAll(collectionDevices);
            this.activity.setListOfPeers(listOfPeers);
        }
    }

    public List<WifiP2pDevice> getListOfPeers() {
        return listOfPeers;
    }
}
