package com.amos.flyinn.wificonnector;

import android.net.wifi.p2p.WifiP2pManager;

/**
 * Globally manage our wifi state.
 */
public class WifiConnectorSingelton {
    private static final WifiConnectorSingelton ourInstance = new WifiConnectorSingelton();
    private WifiStateMachine receiver;
    private WifiP2pManager manager;
    private WifiP2pManager.Channel channel;

    /**
     * Get the global WifiConnector object.
     * @return
     */
    public static WifiConnectorSingelton getInstance() {
        return ourInstance;
    }

    /**
     * Destroy wifi connection groups.
     */
    public void destroyConnection() {
        // TODO: remove group does not work to remove persistent P2P groups
        this.manager.removeGroup(this.channel, null);
    }

    /**
     * Get Wifi receiver.
     * @return
     */
    public WifiStateMachine getWifiReceiverP2P() {
        return this.receiver;
    }

    /**
     * Create a new Wifi State machine from given P2P objects.
     * @param manager
     * @param channel
     * @param activity
     */
    void setWifiReceiverP2P(WifiP2pManager manager, WifiP2pManager.Channel channel, WifiConnectorBase activity) {
        this.manager = manager;
        this.channel = channel;
        this.receiver = new WifiStateMachine(manager, channel, activity);
    }
}
