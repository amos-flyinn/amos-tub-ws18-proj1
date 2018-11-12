package com.amos.flyinn.wifimanager;

public class WifiManager {
    private static final WifiManager ourInstance = new WifiManager();
    private WifiReceiverP2P receiver;

    public static WifiManager getInstance() {
        return ourInstance;
    }

    private WifiManager() {
    }

    public WifiReceiverP2P getWifiReceiverP2P() {
        return this.receiver;
    }

    public void setWifiReceiverP2P(WifiReceiverP2P receiver) {
        this.receiver = receiver;
    }
}
