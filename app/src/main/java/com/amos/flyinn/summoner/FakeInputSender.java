package com.amos.flyinn.summoner;

import android.net.LocalSocket;
import android.net.LocalSocketAddress;

import java.io.ObjectOutputStream;
import java.io.OutputStream;
import java.io.IOException;
import android.view.MotionEvent;

public class FakeInputSender {
    private final LocalSocket socket;
    private ObjectOutputStream output;

    private final String socket_addr = "com.flyinn.fakeinput";

    public FakeInputSender() {
        socket = new LocalSocket();
    }

    public void connect() throws IOException {
        LocalSocketAddress addr = new LocalSocketAddress(socket_addr);
        socket.connect(addr);
        output = new ObjectOutputStream(socket.getOutputStream());

    }

    public void sendMotionEvent(MotionEvent event) throws IOException {
        output.writeObject(event);
    }

    public void close() throws IOException {
        output.close();
        socket.close();
    }
}
