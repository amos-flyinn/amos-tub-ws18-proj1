package com.amos.server.wifibroadcaster;

import android.app.Activity;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.net.NetworkInfo;
import android.net.wifi.p2p.WifiP2pManager;
import android.util.Log;

class WifiServiceManager extends BroadcastReceiver {
    private WifiP2pManager mManager;
    private WifiP2pManager.Channel mChannel;
    private WifiConnectionService connectionService;

    public WifiServiceManager(WifiP2pManager manager, WifiP2pManager.Channel channel, Activity activity) {
        this.mManager = manager;
        this.mChannel = channel;
        this.connectionService = new WifiConnectionService(activity, mManager, mChannel);
    }

    @Override
    public void onReceive(Context context, Intent intent) {
        String action = intent.getAction();
        if (WifiP2pManager.WIFI_P2P_CONNECTION_CHANGED_ACTION.equals(action)) {
            if (mManager != null) {
                NetworkInfo infoNet = intent.getParcelableExtra(WifiP2pManager.EXTRA_NETWORK_INFO);
                if (infoNet.isConnected()) {
                    try {
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
