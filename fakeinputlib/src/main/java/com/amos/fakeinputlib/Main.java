package com.amos.fakeinputlib;

import android.util.Log;

public class Main {
    public static void main(String[] in) throws Exception {
        Log.d("FakeInput", "Start bin");
        FakeInput handler = new FakeInput();
        Log.d("FakeInput", String.format("Start params %s=%d %s=%d", in[0], Integer.parseInt(in[0]), in[1], Integer.parseInt(in[1])));
        FakeInputReceiver server = new FakeInputReceiver(handler, Integer.parseInt(in[0]), Integer.parseInt(in[1]));
        while (true) {
            Log.d("FakeInput", "Starting server");
            server.connectToHost();
            Log.d("FakeInput", "Stopping Server");
        }
    }
}