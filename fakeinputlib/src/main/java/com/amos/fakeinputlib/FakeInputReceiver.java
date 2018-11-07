package com.amos.fakeinputlib;

import android.net.LocalSocket;
import android.net.LocalServerSocket;

import java.io.ObjectInputStream;
import java.io.IOException;
import android.view.MotionEvent;

public class FakeInputReceiver {
    private final LocalServerSocket socket;

    private final String socket_str = "com.flyinn.fakeinput";

    public FakeInputReceiver() throws Exception {
        socket = new LocalServerSocket(socket_str);
    }

    public void listen(FakeInput handler) {
        LocalSocket connection;
        ObjectInputStream istream;
        MotionEvent event;
        while (true) {
            try {
                connection = socket.accept();
                istream = new ObjectInputStream(connection.getInputStream());
                while (true) {
                    try {
                        event = (MotionEvent) istream.readObject();
                        handler.sendMotionEvent(event);
                    } catch (Exception e) {
                        break;
                    }
                }
            } catch (IOException e) {
            }
        }
    }
}
