package com.amos.fakeinputlib;

public class Main {
    public static void main(String[] in) throws Exception {
            FakeInputReceiver server = new FakeInputReceiver();
            FakeInput handler = new FakeInput();

            server.listen(handler);
    }
}
