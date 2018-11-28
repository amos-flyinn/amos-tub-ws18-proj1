package com.amos.shared;

import android.os.SystemClock;
import android.view.MotionEvent;
import android.graphics.Point;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.robolectric.RobolectricTestRunner;

import static org.junit.Assert.*;

@RunWith(RobolectricTestRunner.class)
public class TouchEventTest {

    /**
     * Check that two motion event objects are similar in relevant aspects.
     * @param m1
     * @param m2
     */
    private void assertMotionEventEqual(MotionEvent m1, MotionEvent m2) {
        assertEquals(m1.getX(), m2.getX(), 0.00001);
        assertEquals(m1.getY(), m2.getY(), 0.00001);
        assertEquals(m1.getAction(), m2.getAction());
        assertEquals(m1.getEventTime(), m2.getEventTime());
    }

    /**
     * Create a motion event object back from TouchEvent
     */
    @Test
    public void getConstructedMotionEvent() {
        MotionEvent m = MotionEvent.obtain(
                100, SystemClock.uptimeMillis(),
                MotionEvent.ACTION_DOWN, 100, 200, 0);
        TouchEvent t = new TouchEvent(m);
        MotionEvent mnew = t.getConstructedMotionEvent(1, 1);
        assertMotionEventEqual(mnew, m);
    }

    /**
     * Check that the string representation is as expected.
     */
    @Test
    public void toStringTests() {
        MotionEvent m = MotionEvent.obtain(
                100, SystemClock.uptimeMillis(),
                MotionEvent.ACTION_DOWN, 100, 200, 0);
        TouchEvent t = new TouchEvent(m);
        assertEquals(t.toString(), "TE: X: 100.000000 Y: 200.000000");
    }
}