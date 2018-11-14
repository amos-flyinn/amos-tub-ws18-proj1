package com.amos.fakeinputlib;

import android.util.Log;
import android.view.MotionEvent;

import com.amos.shared.TouchEvent;

import java.io.ObjectInputStream;
import java.net.Socket;

class FakeInputReceiver {
    private final FakeInput handler;
    private final int maxX;
    private final int maxY;

    FakeInputReceiver(FakeInput handler, int maxX, int maxY) {
        this.handler = handler;
        this.maxX = maxX;
        this.maxY = maxY;
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

    private void runEvalLoop(FakeInput handler, Socket connection) throws Exception {
        Log.d("FakeInput", "Starting fakeInputEvalLoop");
        ObjectInputStream istream = new ObjectInputStream(connection.getInputStream());

        TouchEvent e;
        while (true) {
            e = (TouchEvent) istream.readObject();
            Log.d("FakeInput", "Got Event: "+e.toString());
            MotionEvent ev = e.getConstructedMotionEvent(this.maxX, this.maxY);
            Log.d("FakeInput", String.format("Event: (%f, %f, %f, %f)", ev.getX(), ev.getY(), ev.getRawX(), ev.getRawX()));
            handler.sendMotionEvent(ev);
        }
    }
}
