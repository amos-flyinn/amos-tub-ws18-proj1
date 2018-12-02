package com.amos.flyinn.wificonnector;

import android.app.Activity;
import android.content.Context;
import android.content.IntentFilter;
import android.net.wifi.p2p.WifiP2pConfig;
import android.net.wifi.p2p.WifiP2pDevice;
import android.net.wifi.p2p.WifiP2pManager;
import android.util.Log;
import android.widget.Toast;

import java.util.List;

public abstract class Wifibase extends Activity {
    private final IntentFilter intentFilter = new IntentFilter();
    private WifiP2pManager.Channel mChannel;
    private WifiP2pManager mManager;

    protected void createWifiManager() {
        // Indicates a change in the Wi-Fi P2P status.
        intentFilter.addAction(WifiP2pManager.WIFI_P2P_STATE_CHANGED_ACTION);

        // Indicates a change in the list of available peers.
        intentFilter.addAction(WifiP2pManager.WIFI_P2P_PEERS_CHANGED_ACTION);

        // Indicates the state of Wi-Fi P2P connectivity has changed.
        intentFilter.addAction(WifiP2pManager.WIFI_P2P_CONNECTION_CHANGED_ACTION);

        // Indicates this device's details have changed.
        intentFilter.addAction(WifiP2pManager.WIFI_P2P_THIS_DEVICE_CHANGED_ACTION);
        this.mManager = (WifiP2pManager) getSystemService(Context.WIFI_P2P_SERVICE);
        this.mChannel = mManager.initialize(this, getMainLooper(), null);
        this.mManager.discoverPeers(mChannel, new WifiP2pManager.ActionListener() {
            @Override
            public void onSuccess() {
                Toast.makeText(Wifibase.this, "Listening to Peers", Toast.LENGTH_SHORT).show();

            }

            @Override
            public void onFailure(int i) {
                Toast.makeText(Wifibase.this, "Error listening to Peers", Toast.LENGTH_SHORT).show();
                Log.d("WifiP2PActivity", "Error listening to peers : " + i);
            }
        });
    }

    protected void connectToPeer(WifiP2pDevice deviceToConnect) {
        WifiP2pConfig newConfigWifi = new WifiP2pConfig();
        newConfigWifi.deviceAddress = deviceToConnect.deviceAddress;

        this.mManager.connect(mChannel, newConfigWifi, new WifiP2pManager.ActionListener() {
            @Override
            public void onSuccess() {
                Toast.makeText(Wifibase.this, "Connection Successfull", Toast.LENGTH_SHORT).show();
            }

            @Override
            public void onFailure(int i) {
                Toast.makeText(Wifibase.this, "Error connecting to device!", Toast.LENGTH_SHORT).show();
            }
        });
    }

    @Override
    public void onResume() {
        super.onResume();
        WifiReceiverSingelton.getInstance().setWifiReceiverP2P(mManager, mChannel, this);
        registerReceiver(WifiReceiverSingelton.getInstance().getWifiReceiverP2P(), intentFilter);
    }

    @Override
    public void onPause() {
        super.onPause();
        unregisterReceiver(WifiReceiverSingelton.getInstance().getWifiReceiverP2P());
    }


    abstract public void setPeers(List<WifiP2pDevice> listOfPeers);

//    @Override
//    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
//        private static final int COARSE_LOCATION = 1001;
//        // Muell
//        if(Build.VERSION.SDK_INT >= Build.VERSION_CODES.M && checkSelfPermission(Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED){
//            Toast.makeText(this,"Requesting permission for peers",Toast.LENGTH_SHORT).show();
//            requestPermissions(new String[]{Manifest.permission.ACCESS_COARSE_LOCATION}, COARSE_LOCATION);
//        }
//        // Wichtig
//        if (requestCode == COARSE_LOCATION && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
//            Toast.makeText(this,"Permission for peers listening given",Toast.LENGTH_SHORT).show();
//        }
//    }
}
