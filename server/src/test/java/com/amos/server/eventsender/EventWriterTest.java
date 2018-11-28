package com.amos.server.eventsender;

import android.graphics.Point;
import android.os.SystemClock;
import android.text.method.Touch;
import android.view.MotionEvent;

import com.amos.shared.TouchEvent;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.robolectric.RobolectricTestRunner;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;

import static org.junit.Assert.*;

@RunWith(RobolectricTestRunner.class)
public class EventWriterTest {

    EventWriter ew;

    ByteArrayOutputStream output;

    Point size = new Point(1024, 1024);
    MotionEvent test = MotionEvent.obtain(
            100, SystemClock.uptimeMillis(),
            MotionEvent.ACTION_DOWN, 100, 100, 0);

    private void assertEventEquals(MotionEvent e1, MotionEvent e2) {
        assertEquals(e1.getX(), e2.getX(), 0.001);
        assertEquals(e1.getY(), e2.getY(), 0.001);
        assertEquals(e1.getAction(), e2.getAction());
        assertEquals(e1.getEventTime(), e2.getEventTime());
    }

    private MotionEvent obtainOutput() throws IOException {
        return obtainOutput(size);
    }

    private MotionEvent obtainOutput(Point size) throws IOException {
        MotionEvent me;
        ObjectInputStream objs = new ObjectInputStream(new ByteArrayInputStream(output.toByteArray()));
        try {
            TouchEvent te = (TouchEvent) objs.readObject();
            me = te.getConstructedMotionEvent(size.x, size.y);
        } catch (ClassNotFoundException err) { throw new IOException("Wrong object type."); }
        return me;
    }

    private TouchEvent serializeEvent(MotionEvent m) {
        return new TouchEvent(m, size);
    }

    @Before
    public void setUp() throws Exception {
        // output = new ByteArrayOutputStream();
        output = new ByteArrayOutputStream();
        ew = new EventWriter(output);
    }

    /**
     * Test writing motionevents
     */
    @Test
    public void writeMotionEvents() throws IOException {
        ew.write(test);
        MotionEvent result = obtainOutput(new Point(1, 1));
        assertEventEquals(test, result);
    }

    /**
     * Test writing touchevents, which are serializable motionevents
     */
    @Test
    public void writeTouchEvents() throws IOException{
        TouchEvent testevent = serializeEvent(test);
        ew.write(testevent);
        MotionEvent result = obtainOutput();
        assertEventEquals(test, result);
    }

    /**
     * Test closing outputstream
     */
    @Test
    public void close() throws IOException {
        ew.close();
    }
}