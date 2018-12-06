package com.amos.flyinn.wificonnector;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.net.NetworkInfo;
import android.net.wifi.p2p.WifiP2pManager;
import android.util.Log;

/**
 * Handle changes in the state of our connection setup and manage changes between stages.
 */
public class WifiStateMachine extends BroadcastReceiver {
    private WifiP2pManager mManager;
    private WifiP2pManager.Channel mChannel;
    private PeersListenerService peersLister;
    private WifiConnectorBase base;
    private WifiConnectionService connectionService;

    /**
     * Create a new state machine object.
     * @param manager
     * @param channel
     * @param activity
     */
    protected WifiStateMachine(WifiP2pManager manager, WifiP2pManager.Channel channel, WifiConnectorBase activity) {
        this.base = activity;
        this.mManager = manager;
        this.mChannel = channel;
        this.peersLister = new PeersListenerService(activity);
        this.connectionService = new WifiConnectionService(activity, this.mManager, this.mChannel);
    }

    /**
     * Get the server host address from an existing connection.
     * @return
     * @throws Exception
     */
    public String getHostAddr() throws Exception {
        return this.connectionService.getServerAddress();
    }

    /**
     * Handle received intents. These are used to trigger state changes.
     * @param context
     * @param intent
     */
    @Override
    public void onReceive(Context context, Intent intent) {
        String action = intent.getAction();
        if (mManager != null) {
            mManager.requestPeers(mChannel, peersLister);
        }
        if (WifiP2pManager.WIFI_P2P_PEERS_CHANGED_ACTION.equals(action)) {
            // The peer list has changed! We should probably do something about
            // that.
            Log.d("WifiStateMachine", "Peers Changed Action");
            if (mManager != null) {
                mManager.requestPeers(mChannel, peersLister);
            }
        } else if (WifiP2pManager.WIFI_P2P_CONNECTION_CHANGED_ACTION.equals(action)) {
            // Connection state changed! We should probably do something about
            // that.
            if (mManager != null) {
                NetworkInfo infoNet = intent.getParcelableExtra(WifiP2pManager.EXTRA_NETWORK_INFO);
                if (infoNet.isConnected()) {
                    mManager.requestConnectionInfo(mChannel, this.connectionService);
                } else {
                    this.base.onDisconnected();
                }
            }
            Log.d("WifiStateMachine", "Connection Changed Action");
        } else if (WifiP2pManager.WIFI_P2P_THIS_DEVICE_CHANGED_ACTION.equals(action)) {
            Log.d("WifiStateMachine", "This Device Changed Action");
        }
    }
}
