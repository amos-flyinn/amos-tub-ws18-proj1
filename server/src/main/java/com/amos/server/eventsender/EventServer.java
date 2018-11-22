package com.amos.server.eventsender;

import android.os.Handler;
import android.os.Message;
import android.os.SystemClock;
import android.util.Log;
import android.view.MotionEvent;

import com.amos.server.signaling.SocketServer;
import com.amos.shared.TouchEvent;

import java.io.IOException;
import java.util.concurrent.BlockingQueue;

public class EventServer implements Runnable{
    SocketServer server;
    EventWriter writer;
    Handler uiHandler;

    BlockingQueue<TouchEvent> queue;
    Boolean accepting = true;

    public EventServer(BlockingQueue<TouchEvent> mq) {
        queue = mq;
    }

    public EventServer(BlockingQueue<TouchEvent> mq, Handler ui) {
        queue = mq;
        uiHandler = ui;
    }

    public void accept() {
        Log.d("EventServer", "Waiting for connection");
        try {
            writer = new EventWriter(server.getStream());
            Log.d("EventServer", "Accepted connection");
            long time = SystemClock.uptimeMillis();
            MotionEvent e = MotionEvent.obtain(time, time, MotionEvent.ACTION_DOWN, 0, 0, 1.0f, 1.0f, 0, 1.0f, 1.0f, 0, 0);
            writer.write(e);
            Log.d("EventServer", "Sent motion event");
            writer.close();
        } catch (IOException e) {
        }
    }

    public void close() {
        try {
            if (server != null) {
                server.close();
            }
        } catch (IOException e) {
            Log.d("EventServer", "Error closing writer or server.");
        }
    }

    public void acceptQueue() {
        while (accepting) {
            try {
                Log.d("EventServer", "Waiting for connection");
                writer = new EventWriter(server.getStream());
                Log.d("EventServer", "Accepted connection");
                while (true) {
                    try {
                        TouchEvent e = queue.take();
                        if (e != null) {
                            writer.write(e);
                        }
                    } catch (IOException e) {
                        Log.d("EventServer", "IO Except");
                        break;
                    } catch (InterruptedException e) {
                        Log.d("EventServer", "Interrupted");
                        break;
                    }
                }
                writer.close();
            } catch (IOException e) {
                Log.d("EventServer", "Waiting for connections interrupted");
                break;
            }
        }
    }

    private void sendMessage(String msg) {
        if (uiHandler != null) {
            Message m = uiHandler.obtainMessage(0, msg);
            m.sendToTarget();
        }
    }

    @Override
    public void run() {
        sendMessage("Starting server");
        try {
            server = new SocketServer();
            Log.d("EventServer", "Thread started");
            sendMessage("Accepting connections");
            acceptQueue();
            sendMessage("Closing server");
            server.close();
            sendMessage("Server closed. Restart application to initiate new connection.");
        } catch (IOException e) {
        }
    }
}

