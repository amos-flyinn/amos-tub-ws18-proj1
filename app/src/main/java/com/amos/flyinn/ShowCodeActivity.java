package com.amos.flyinn;

import android.net.wifi.p2p.WifiP2pDevice;
import android.os.Bundle;

import com.amos.flyinn.wificonnector.WifiConnectorBase;

import java.util.List;


public class ShowCodeActivity extends WifiConnectorBase {
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_show_code);
    }

    @Override
    public void setPeers(List<WifiP2pDevice> listOfPeers) {
        for (WifiP2pDevice device : listOfPeers) {
            if (device.deviceName.endsWith("flyinn-" + "1234")) {
                this.connectToPeer(device);
            }
        }
    }
}
