package com.amos.flyinn.wifimanager;

public class WifiManagerSingelton {
    private static final WifiManagerSingelton ourInstance = new WifiManagerSingelton();
    private WifiReceiverP2P receiver;

    public static WifiManagerSingelton getInstance() {
        return ourInstance;
    }

    private WifiManagerSingelton() {
    }

    public WifiReceiverP2P getWifiReceiverP2P() {
        return this.receiver;
    }

    public WifiReceiverP2P setWifiReceiverP2P(WifiReceiverP2P receiver) {
        this.receiver = receiver;
    }
}
