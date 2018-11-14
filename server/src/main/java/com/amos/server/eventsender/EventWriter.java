package com.amos.server.eventsender;

import com.amos.shared.TouchEvent;

import java.io.IOException;
import java.io.OutputStream;
import java.io.ObjectOutputStream;
import android.view.MotionEvent;
import android.util.Log;


public class EventWriter {
    private OutputStream ostream;
    private ObjectOutputStream output;

    public EventWriter(OutputStream os) throws IOException {
        ostream = os;
        output = new ObjectOutputStream(ostream);
    }

    public void write(MotionEvent e) throws IOException {
        TouchEvent te = new TouchEvent(e.getX(), e.getY(), e.getAction(), e.getDownTime());
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
