package com.amos.server.eventsender;

import android.graphics.Point;
import android.view.MotionEvent;

import com.amos.shared.TouchEvent;

import java.io.IOException;
import java.io.ObjectOutputStream;
import java.io.OutputStream;


public class EventWriter {
    private OutputStream ostream;
    private ObjectOutputStream output;
    private Point screenSize;

    public EventWriter(OutputStream os, Point screen) throws IOException {
        ostream = os;
        screenSize = screen;
        output = new ObjectOutputStream(ostream);
    }

    public void write(MotionEvent e) throws IOException {
        TouchEvent te = new TouchEvent(e, screenSize);
        write(te);
    }

    public void write(TouchEvent e) throws IOException {
        output.writeObject(e);
        output.flush();
    }

    public void close() throws IOException {
        output.close();
        ostream.close();
    }
}
