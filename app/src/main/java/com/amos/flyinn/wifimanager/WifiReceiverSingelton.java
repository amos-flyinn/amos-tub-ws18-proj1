package com.amos.flyinn.wifimanager;

import android.app.Activity;
import android.net.wifi.p2p.WifiP2pManager;

public class WifiReceiverSingelton {
    private static final WifiReceiverSingelton ourInstance = new WifiReceiverSingelton();
    private WifiReceiverP2P receiver;

    public static WifiReceiverSingelton getInstance() {
        return ourInstance;
    }

    private WifiReceiverSingelton() {
    }

    public WifiReceiverP2P getWifiReceiverP2P() {
        return this.receiver;
    }

    public void setWifiReceiverP2P(WifiP2pManager manager, WifiP2pManager.Channel channel, Activity activity) {
        this.receiver = new WifiReceiverP2P(manager, channel, activity);
    }
}
