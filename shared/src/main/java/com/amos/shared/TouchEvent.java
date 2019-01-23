package com.amos.shared;

import android.graphics.Point;
import android.os.SystemClock;
import android.text.method.Touch;
import android.util.Log;
import android.view.MotionEvent;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

/**
 * Serializable version of TouchEvents which contain enough information to
 * transfer touch inputs between different devices.
 *
 * Only MotionEvent information relevant to reconstructing usable MotionEvents are sent.
 * X and Y are expected to be normalized to the range between 0 and 1.
 */
public class TouchEvent implements Serializable {
    public float x;
    public float y;
    public int action;
    public long downTime;
    private int counterPointer;
    private float max = 1;
    private List<SubTouchEvent> subEvents = new ArrayList<>();

    /**
     * Create Single Touchpoint event.
     * @param x
     * @param y
     * @param action
     * @param downTime
     */
    public TouchEvent(float x, float y, int action, long downTime) {
        Log.d("ToucEventLog", "TouchEvent: Test" );

        this.x = x;
        this.y = y;
        this.action = action;
        this.downTime = downTime;
    }

    /**
     * Create Single Touchpoint event directly from MotionEvent.
     * @param m
     */
    public TouchEvent(MotionEvent m) {
        Log.d("ToucEventLog", "TouchEvent: " + m.getPointerCount());

        if(m.getPointerCount() == 1)
        {

            this.counterPointer = 1;

            this.action = m.getAction();
            this.downTime = m.getDownTime();

            float tempX = m.getX() ;
            float tempY = m.getY();

            this.x = tempX;
            this.y = tempY;

            SubTouchEvent event = new SubTouchEvent(0,tempX,tempY,m.getToolType(0),1);

            this.subEvents.add(event);


        }
        else
        {
            this.counterPointer = m.getPointerCount();
            this.action = m.getAction();
            this.downTime = m.getDownTime();

            for(int x = 0;x<m.getPointerCount() ; x++)
            {
                int indexPointer = m.findPointerIndex(x);

                float tempX = m.getX(indexPointer);
                float tempY = m.getY(indexPointer);

                SubTouchEvent event = new SubTouchEvent(m.getPointerId(indexPointer),tempX,tempY,m.getToolType(indexPointer),1);

                this.subEvents.add(event);
            }

        }

    }
    /**
     * Rescale x and y with given screensize before assignment
     * @param m
     * @param screenSize
     */
    public TouchEvent(MotionEvent m, Point screenSize) {
        Log.d("ToucEventLog", "TouchEvent: " + m.getPointerCount());
        if(m.getPointerCount() == 1)
        {

            this.counterPointer = 1;
            this.action = m.getAction();
            this.downTime = m.getDownTime();

            float tempX = m.getX() / screenSize.x;
            float tempY = m.getY() / screenSize.y;

            this.x = tempX;
            this.y = tempY;

            SubTouchEvent subEvent = new SubTouchEvent(0,tempX,tempY,m.getToolType(0),1);

            this.subEvents.add(subEvent);
        }
        else
        {
            this.counterPointer = m.getPointerCount();
            this.action = m.getAction();
            this.downTime = m.getDownTime();

            for(int x = 0;x<m.getPointerCount() ; x++)
            {
                int indexPointer = m.findPointerIndex(x);

                float tempX = m.getX(indexPointer) / screenSize.x;
                float tempY = m.getY(indexPointer) / screenSize.y;

                SubTouchEvent subEvent = new SubTouchEvent(m.getPointerId(indexPointer),tempX,tempY,m.getToolType(indexPointer),1);

                this.subEvents.add(subEvent);

            }

        }

    }

    private MotionEvent.PointerCoords[] generateCoordsWithData(List<SubTouchEvent> subEvents){



        MotionEvent.PointerCoords cordsArray[] = new MotionEvent.PointerCoords[this.subEvents.size()];

        for(int x = 0;x<cordsArray.length ; x++)
        {
            SubTouchEvent event = this.subEvents.get(x);

            MotionEvent.PointerProperties prop = new MotionEvent.PointerProperties();
            MotionEvent.PointerCoords cords = new MotionEvent.PointerCoords();


            cords.size = event.getSize();
            cords.x = event.getX();
            cords.y = event.getY();

            cordsArray[x] = cords;
        }


        return cordsArray;
    }

    private MotionEvent.PointerProperties[] generatePropertiesWithData(List<SubTouchEvent> subEvents){


        MotionEvent.PointerProperties propsArray[] = new MotionEvent.PointerProperties[this.subEvents.size()];

        for(int x = 0;x<propsArray.length ; x++)
        {
            SubTouchEvent event = this.subEvents.get(x);

            MotionEvent.PointerProperties prop = new MotionEvent.PointerProperties();

            prop.toolType = event.getTooltyp();
            prop.id = event.getId();

            propsArray[x] = prop;
        }

        return propsArray;

    }

    private void calculateCorrectCords(MotionEvent.PointerCoords[]cordsArray,int maxX,int maxY)
    {

        for(MotionEvent.PointerCoords cords : cordsArray)
        {
            cords.x = (cords.x/max) * maxX;
            cords.y = (cords.y/max) * maxY;
        }

    }

    /**
     * Create MotionEvent from given TouchEvent.
     * @param maxX
     * @param maxY
     * @return
     */
    public MotionEvent getConstructedMotionEvent(int maxX, int maxY) {

        MotionEvent.PointerProperties[] pointerProperties = this.generatePropertiesWithData(this.subEvents);

        MotionEvent.PointerCoords[] pointerCoords = this.generateCoordsWithData(this.subEvents);

        this.calculateCorrectCords(pointerCoords,maxX,maxY);

        return MotionEvent.obtain(downTime, SystemClock.uptimeMillis(),
                action, counterPointer, pointerProperties,
                pointerCoords, 0,  0, 1, 1, 0, 0, 0, 0 );
    }

    /**
     * Create String representation of TouchEvent.
     * @return
     */
    @Override
    public String toString() {
        return String.format("TE: X: %f Y: %f", x, y);
    }
}




