package com.amos.flyinn.summoner;

import android.view.MotionEvent;

import com.amos.shared.TouchEvent;

import java.io.IOException;
import java.io.ObjectOutputStream;
import java.net.Socket;


public class FakeInputSender {
    private ObjectOutputStream output;

    public FakeInputSender() {
    }

    public void connect() throws IOException {
        Socket socket = new Socket("127.0.0.1", 1337);
        this.output = new ObjectOutputStream(socket.getOutputStream());
    }

    public void sendMotionEvent(MotionEvent e) throws IOException {
        this.output.writeObject(new TouchEvent(e.getX(), e.getY(), e.getAction(), e.getDownTime(), e.getEventTime()));
    }
}
