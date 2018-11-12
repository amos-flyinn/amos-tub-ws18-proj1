package com.amos.server.eventsender;

import com.amos.shared.TouchEvent;

import java.io.IOException;
import java.io.OutputStream;
import java.io.ObjectOutputStream;
import android.view.MotionEvent;


public class EventWriter {
    private OutputStream ostream;
    private ObjectOutputStream output;

    public EventWriter(OutputStream os) throws IOException {
        ostream = os;
        output = new ObjectOutputStream(ostream);
    }

    public void write(MotionEvent e) throws IOException {
        output.writeObject(
                new TouchEvent(e.getX(), e.getY(), e.getAction(), e.getDownTime(), e.getEventTime())
        );
        output.flush();
    }

    public void close() throws IOException {
        output.close();
        ostream.close();
    }
}
