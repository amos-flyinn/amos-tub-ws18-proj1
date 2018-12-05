package com.amos.flyinn.wificonnector;

import android.net.wifi.p2p.WifiP2pManager;

public class WifiConnectorSingelton {
    private static final WifiConnectorSingelton ourInstance = new WifiConnectorSingelton();
    private WifiStateMachine receiver;
    private WifiP2pManager manager;
    private WifiP2pManager.Channel channel;

    public static WifiConnectorSingelton getInstance() {
        return ourInstance;
    }

    public void destroyConnection() {
        this.manager.removeGroup(this.channel, null);
    }

    WifiStateMachine getWifiReceiverP2P() {
        return this.receiver;
    }

    void setWifiReceiverP2P(WifiP2pManager manager, WifiP2pManager.Channel channel, WifiConnectorBase activity) {
        this.manager = manager;
        this.channel = channel;
        this.receiver = new WifiStateMachine(manager, channel, activity);
    }
}
