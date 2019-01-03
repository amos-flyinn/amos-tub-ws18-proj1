package com.amos.server.eventsender;

import android.graphics.Point;
import android.view.MotionEvent;

import com.amos.shared.TouchEvent;

import java.io.IOException;
import java.io.ObjectOutputStream;
import java.io.OutputStream;


public class EventWriter {
    private OutputStream outputStream;
    private ObjectOutputStream output;
    private Point screenSize;

    public EventWriter(OutputStream os, Point screen) throws IOException {
        outputStream = os;
        screenSize = screen;
        output = new ObjectOutputStream(outputStream);
    }

    public void write(MotionEvent e) throws IOException {
        TouchEvent te = new TouchEvent(e, screenSize);
        write(te);
    }

    void write(TouchEvent e) throws IOException {
        output.writeObject(e);
        output.flush();
    }

    public void close() throws IOException {
        output.close();
        outputStream.close();
    }
}
