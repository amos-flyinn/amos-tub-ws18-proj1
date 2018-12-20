package com.amos.server.wifibroadcaster;

import android.app.Activity;
import android.net.wifi.p2p.WifiP2pManager;

/**
 * Manage WifiServiceManager instances for the whole application.
 * <p>
 * Only one connection needs to be handled at any time, but the object itself can change depending
 * on disconnects and other operations. This wrapper class handles global access to the WifiServiceManager object.
 */
public class WifiBroadcasterSingleton {
    private static final WifiBroadcasterSingleton ourInstance = new WifiBroadcasterSingleton();
    private WifiServiceManager s;

    /**
     * Get current WifiBroadcaster.
     * <p>
     * This follows a standard singleton pattern for java. Other objects have to reference to this
     * class via a getter, thus avoiding creating additional instances of this class across the application.
     *
     * @return
     */
    public static WifiBroadcasterSingleton getInstance() {
        return ourInstance;
    }

    /**
     * Get the current WifiBroadcaster
     *
     * @return
     */
    public WifiServiceManager getBroadcaster() {
        return this.s;
    }

    /**
     * Create a new WifiServiceManager using given P2P objects.
     *
     * @param manager
     * @param channel
     * @param activity
     */
    public void setInstance(WifiP2pManager manager, WifiP2pManager.Channel channel, Activity activity) {
        this.s = new WifiServiceManager(manager, channel, activity);
    }
}
