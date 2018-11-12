package com.amos.fakeinputlib;

import android.util.Log;

public class Main {
    public static void main(String[] in) throws Exception {
        FakeInputReceiver server = new FakeInputReceiver(new FakeInput());
        server.connectToHost(in[0]);
        Log.d("FakeInput", "Stopping Server");
    }
}