package com.amos.shared;

import android.os.SystemClock;
import android.util.Log;
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

    private void assertMotionEventEqualMultiTouch(MotionEvent m1,MotionEvent m2)
    {
        int counter = m1.getPointerCount();
        int counter2 = m2.getPointerCount();

        assertEquals(counter,counter2);

        for(int x = 0; x<counter ; x++)
        {
            int pointerIndex1 = m1.findPointerIndex(x);
            int pointerIndex2 = m2.findPointerIndex(x);
            assertEquals(m1.getX(pointerIndex1),m2.getX(pointerIndex2),0.00001);
            assertEquals(m1.getY(pointerIndex1),m2.getY(pointerIndex2),0.00001);
            assertEquals(m1.getToolType(pointerIndex1),m2.getToolType(pointerIndex2));
            assertEquals(m1.getAction(),m2.getAction());
            assertEquals(m1.getEventTime(),m2.getEventTime());
        }

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
     * Check multitouch event consistency
     */
    @Test
    public void assertGetConstructedMotionEventMultiTouch() {
        int eventCounter = 3;

        MotionEvent.PointerCoords cordsArray[] = new MotionEvent.PointerCoords[3];
        MotionEvent.PointerProperties propertiesArray[] = new MotionEvent.PointerProperties[3];

        MotionEvent.PointerProperties prop1 = new MotionEvent.PointerProperties();
        prop1.id = 0;
        prop1.toolType = MotionEvent.TOOL_TYPE_FINGER;

        MotionEvent.PointerProperties prop2 = new MotionEvent.PointerProperties();
        prop2.id = 1;
        prop2.toolType = MotionEvent.TOOL_TYPE_FINGER;


        MotionEvent.PointerProperties prop3 = new MotionEvent.PointerProperties();
        prop3.id = 2;
        prop3.toolType = MotionEvent.TOOL_TYPE_FINGER;

        MotionEvent.PointerCoords cord1 = new MotionEvent.PointerCoords();
        cord1.x = 100;
        cord1.y = 200;
        cord1.size = 1;

        MotionEvent.PointerCoords cord2 = new MotionEvent.PointerCoords();
        cord1.x = 300;
        cord1.y = 400;
        cord1.size = 1;

        MotionEvent.PointerCoords cord3 = new MotionEvent.PointerCoords();
        cord1.x = 500;
        cord1.y = 600;
        cord1.size = 1;


        cordsArray[0] = cord1;
        cordsArray[1] = cord2;
        cordsArray[2] = cord3;

        propertiesArray[0] = prop1;
        propertiesArray[1] = prop2;
        propertiesArray[2] = prop3;


        MotionEvent m = MotionEvent.obtain(
                100, SystemClock.uptimeMillis(),
                MotionEvent.ACTION_DOWN,eventCounter,propertiesArray,cordsArray,0,
                0,1,1,0,0,0,0);

        Log.d("TEst", "getConstructedMotionEventMultiTouch: " + m);
        TouchEvent t = new TouchEvent(m);
        MotionEvent mnew = t.getConstructedMotionEvent(1, 1);
        assertMotionEventEqualMultiTouch(mnew, m);
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