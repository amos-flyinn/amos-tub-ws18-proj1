package com.amos.server.wifimanager;

import android.app.Activity;
import android.net.wifi.p2p.WifiP2pManager;

public class WifiBradcasterSingelton {
    private static final WifiBradcasterSingelton ourInstance = new WifiBradcasterSingelton();
    private WifiServiceManager s;

    public static WifiBradcasterSingelton getInstance() {
        return ourInstance;
    }

    public  WifiServiceManager getBroadcaster() {
        return this.s;
    }

    public void setInstance(WifiP2pManager manager, WifiP2pManager.Channel channel , Activity activity) {
        this.s = new WifiServiceManager(manager, channel, activity);
    }
}
