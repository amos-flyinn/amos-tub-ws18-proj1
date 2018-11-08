package com.amos.fakeinputlib;

import android.util.Log;
import android.view.MotionEvent;

import com.amos.shared.TouchEvent;

import java.io.ObjectInputStream;
import java.net.ServerSocket;
import java.net.Socket;

class FakeInputReceiver {

    FakeInputReceiver() {
    }

    void listen(FakeInput handler) throws Exception {
        ServerSocket fd = new ServerSocket(1337);
        Socket connection = fd.accept();
        ObjectInputStream istream = new ObjectInputStream(connection.getInputStream());

        TouchEvent e;
        while (true) {
            e = (TouchEvent) istream.readObject();
            Log.i("Server", "Got Event");
            handler.sendMotionEvent(MotionEvent.obtain(e.downTime, e.eventTime,e.action, e.x, e.y, 0));
        }
   }
}
