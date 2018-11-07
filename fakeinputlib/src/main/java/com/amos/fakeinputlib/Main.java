package com.amos.fakeinputlib;

import android.util.Log;

public class Main {
    public static void main(String[] in) throws Exception {
        FakeInputReceiver server = new FakeInputReceiver();
        FakeInput handler = new FakeInput();

        server.listen(handler);
        // FakeInput fi = new FakeInput();
        // for (int i = 0; i < 10; i++) {
        //     fi.sendTap(0,0);
        //     Thread.sleep(10000);
        // }
        // Log.i("FAKE-INPUT", "Suxxessful ecit!");
    }
}
