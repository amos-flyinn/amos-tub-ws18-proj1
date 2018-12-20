package com.amos.server.eventsender;

import android.view.MotionEvent;

import com.amos.shared.TouchEvent;

import org.junit.Before;
import org.junit.Test;

import java.io.IOException;
import java.io.ObjectInputStream;
import java.net.Socket;
import java.util.concurrent.LinkedBlockingQueue;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNull;

public class EventServerTest {

    EventServer es;

    LinkedBlockingQueue<TouchEvent> teq;

    @Before
    public void setUp() {
        teq = new LinkedBlockingQueue<>();
        es = new EventServer(teq);
    }

    /**
     * Test Accept with null server.
     */
    @Test
    public void accept() {
        es.accept();
    }

    /**
     * Test close with null server.
     */
    @Test
    public void close() {
        es.close();
        assertNull(es.server);
    }

    /**
     * Test acceptQueue with null server.
     */
    @Test
    public void acceptQueue() {
        es.acceptQueue();
    }

    /**
     * Test a single client connection and connection close.
     *
     * @throws IOException
     */
    @Test
    public void run_simple() throws IOException {
        Thread th = new Thread(es);
        th.start();
        Socket client = new Socket("localhost", 1337);
        ObjectInputStream output = new ObjectInputStream(client.getInputStream());
        client.close();
    }

    /**
     * Test i/o of touch events
     *
     * @throws IOException
     * @throws InterruptedException
     * @throws ClassNotFoundException
     */
    @Test
    public void run() throws IOException, InterruptedException, ClassNotFoundException {
        TouchEvent initial = new TouchEvent(100, 10, 10, MotionEvent.ACTION_DOWN);
        teq.put(initial);
        Thread th = new Thread(es);
        th.start();
        Socket client = new Socket("localhost", 1337);
        ObjectInputStream output = new ObjectInputStream(client.getInputStream());

        TouchEvent te = (TouchEvent) output.readObject();
        assertEquals(te.x, initial.x, 0.00001);
        assertEquals(te.y, initial.y, 0.00001);
        assertEquals(te.downTime, initial.downTime, 0.00001);
        assertEquals(te.action, initial.action, 0.00001);
        client.close();
    }
}