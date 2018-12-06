package com.amos.flyinn;

import android.net.wifi.p2p.WifiP2pDevice;
import android.os.Bundle;
import android.widget.TextView;

import com.amos.flyinn.wificonnector.WifiConnectorBase;

import java.util.List;
import java.util.concurrent.ThreadLocalRandom;


public class ShowCodeActivity extends WifiConnectorBase {
    private String nameNum = "1234";
    private TextView display;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_show_code);
        display = findViewById(R.id.textView2);
    }

    @Override
    public void onResume() {
        super.onResume();
        nameNum = String.valueOf(ThreadLocalRandom.current().nextInt(1000, 9998 + 1));
        display.setText(nameNum);
    }

    @Override
    public void setPeers(List<WifiP2pDevice> listOfPeers) {
        for (WifiP2pDevice device : listOfPeers) {
            if (device.deviceName.endsWith("flyinn-" + nameNum)) {
                this.connectToPeer(device);
            }
        }
    }
}
