package com.amos.fakeinputlib;

import android.util.Log;

import com.amos.shared.TouchEvent;

import java.io.ObjectInputStream;
import java.net.ServerSocket;
import java.net.Socket;

class FakeInputReceiver {
    private final FakeInput handler;

    FakeInputReceiver(FakeInput handler) {
        this.handler = handler;
    }

    void connectToHost(String addr) throws Exception {
        Socket fd = null;
        for (int i = 0; i < 5; i++) {
            Log.d("FakeInput", "Trying to connect to the host " + addr);
            try {
                fd = new Socket(addr, 1337);
            } catch (Exception e) {
                e.printStackTrace();

                // Exponential backoff
                Thread.sleep((int) Math.pow(2.5, i));
                continue;
            }
            break;
        }
        if (fd == null) {
            throw new Exception("repeatedly failed to connect");
        }
        Log.d("FakeInput", "Connected to Host");
        runEvalLoop(handler, fd);
    }

    void listen() throws Exception {
        ServerSocket fd = new ServerSocket(1000);

        fd.setSoTimeout(10000);
        fd.setReuseAddress(true);
        Socket connection = fd.accept();
        runEvalLoop(handler, connection);
    }

    private void runEvalLoop(FakeInput handler, Socket connection) throws Exception {
        Log.d("FakeInput", "Starting fakeInputEvalLoop");
        ObjectInputStream istream = new ObjectInputStream(connection.getInputStream());

        TouchEvent e;
        while (true) {
            e = (TouchEvent) istream.readObject();
            Log.d("FakeInput", "Got Event");
            handler.sendMotionEvent(e.getConstructedMotionEvent());
        }
    }
}
