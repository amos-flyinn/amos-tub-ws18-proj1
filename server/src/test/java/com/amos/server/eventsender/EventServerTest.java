package com.amos.server.eventsender;

import android.view.MotionEvent;

import com.amos.shared.TouchEvent;

import org.junit.Before;
import org.junit.Test;

import java.util.concurrent.LinkedBlockingQueue;

import static org.junit.Assert.*;

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

    @Test
    public void close() {
        es.close();
        assertNull(es.server);
    }

    @Test
    public void acceptQueue() {
    }

    @Test
    public void run() {
    }
}