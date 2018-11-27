package com.amos.server.signaling;

import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;
import java.io.OutputStream;

public class SocketServer {

    private ServerSocket socket;
    private Socket client;

    public SocketServer() throws IOException {
        socket = new ServerSocket(1337);
    }

    public OutputStream getStream() throws IOException {
        client = socket.accept();
        return client.getOutputStream();
    }

    public void close() throws IOException {
        if (client != null) {
            client.close();
        }
        socket.close();
    }
}
