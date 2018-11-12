package com.amos.server.eventsender;

import com.amos.server.EventSenderDemo;
import com.amos.server.signaling.SocketServer;

import android.os.SystemClock;
import android.util.Log;
import android.view.MotionEvent;

import java.io.IOException;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.LinkedBlockingQueue;

public class EventServer implements Runnable{
    SocketServer server;
    EventWriter writer;

    BlockingQueue<MotionEvent> queue;

    public EventServer(BlockingQueue<MotionEvent> mq) {
        queue = mq;
    }

    public void accept() {
        Log.d("Test", "Waiting for connection");
        try {
            writer = new EventWriter(server.getStream());
            Log.d("Test", "Accepted connection");
            long time = SystemClock.uptimeMillis();
            MotionEvent e = MotionEvent.obtain(time, time, MotionEvent.ACTION_DOWN, 0, 0, 1.0f, 1.0f, 0, 1.0f, 1.0f, 0, 0);
            writer.write(e);
            Log.d("Test", "Sent motion event");
            writer.close();
        } catch (IOException e) {
        }
    }

    public void acceptQueue() {
        try {
            writer = new EventWriter(server.getStream());
            while (true) {
                try {
                    MotionEvent e = queue.remove();
                    writer.write(e);
                } catch (Exception e) {
                    break;
                }
            }
            writer.close();
        } catch (IOException e) {
        }
    }

    @Override
    public void run() {
        try {
            server = new SocketServer();
            Log.d("Test", "Thread started");
            accept();
            server.close();
        } catch (IOException e) {
        }
    }
}
