package com.amos.server.eventsender;

import android.graphics.Point;
import android.util.Log;
import android.view.MotionEvent;

import com.amos.shared.TouchEvent;

import java.io.IOException;
import java.io.ObjectOutputStream;
import java.io.OutputStream;


public class EventWriter {
    private static final String TAG = "EventWriter";
    private OutputStream outputStream;
    private ObjectOutputStream output;
    private Point screenSize;

    public EventWriter(OutputStream os, Point screen) throws IOException {
        outputStream = os;
        screenSize = screen;
        output = new ObjectOutputStream(outputStream);
    }

    public void write(MotionEvent e) throws IOException {
        Log.d(TAG, screenSize.toString());
        TouchEvent te = new TouchEvent(e, screenSize);
        write(te);
    }

    void write(TouchEvent e) throws IOException {
        Log.d(TAG, e.toString());
        output.writeObject(e);
        output.flush();
    }

    public void close() throws IOException {
        output.close();
        outputStream.close();
    }
}
