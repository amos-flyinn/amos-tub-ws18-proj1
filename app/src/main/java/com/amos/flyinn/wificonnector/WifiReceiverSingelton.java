package com.amos.flyinn.wificonnector;

import android.net.wifi.p2p.WifiP2pManager;

public class WifiReceiverSingelton {
    private static final WifiReceiverSingelton ourInstance = new WifiReceiverSingelton();
    private WifiReceiverP2P receiver;

    public static WifiReceiverSingelton getInstance() {
        return ourInstance;
    }

    public WifiReceiverP2P getWifiReceiverP2P() {
        return this.receiver;
    }

    public void setWifiReceiverP2P(WifiP2pManager manager, WifiP2pManager.Channel channel, Wifibase activity) {
        this.receiver = new WifiReceiverP2P(manager, channel, activity);
    }
}
