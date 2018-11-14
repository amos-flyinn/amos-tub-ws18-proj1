package com.amos.fakeinputlib;

import android.util.Log;

public class Main {
    public static void main(String[] in) throws Exception {
        FakeInputReceiver server = new FakeInputReceiver(new FakeInput(), Integer.parseInt(in[1]), Integer.parseInt(in[2]));
        server.connectToHost(in[0]);
        Log.d("FakeInput", "Stopping Server");
    }
}