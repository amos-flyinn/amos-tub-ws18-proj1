package com.amos.server.wifibroadcaster;

import android.app.Activity;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.net.NetworkInfo;
import android.net.wifi.p2p.WifiP2pManager;
import android.util.Log;

/**
 * Use intent broadcasts to trigger activity changes and further connection setup.
 */
class WifiServiceManager extends BroadcastReceiver {
    private WifiP2pManager mManager;
    private WifiP2pManager.Channel mChannel;
    private WifiConnectionService connectionService;

    /**
     * Create a new WifiService manager.
     * <p>
     * This constructor should only be called by the broadcaster.
     *
     * @param manager
     * @param channel
     * @param activity
     */
    public WifiServiceManager(WifiP2pManager manager, WifiP2pManager.Channel channel, Activity activity) {
        this.mManager = manager;
        this.mChannel = channel;
        this.connectionService = new WifiConnectionService(activity, mManager, mChannel);
    }

    /**
     * Try to get connection information on connection setup message from Wifi P2P
     * <p>
     * Further processing is handled by WifiConnectionService, which acts on the resulting connection information.
     *
     * @param context
     * @param intent
     */
    @Override
    public void onReceive(Context context, Intent intent) {
        String action = intent.getAction();
        if (WifiP2pManager.WIFI_P2P_CONNECTION_CHANGED_ACTION.equals(action)) {
            if (mManager != null) {
                NetworkInfo infoNet = intent.getParcelableExtra(WifiP2pManager.EXTRA_NETWORK_INFO);
                if (infoNet.isConnected()) {
                    try {
                        Log.d("TestHere", "test");
                        mManager.requestConnectionInfo(mChannel, this.connectionService);
                    } catch (Exception e) {
                        e.printStackTrace();
                        Log.d("onReceive", "requestConnectionInfo fatal error");
                    }
                }
            }
            Log.d("WifiReceiverP2P", "Connection Changed Action");
        }
    }
}
