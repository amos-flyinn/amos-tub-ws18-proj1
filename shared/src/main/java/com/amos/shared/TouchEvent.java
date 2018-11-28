package com.amos.shared;

import android.os.SystemClock;
import android.view.MotionEvent;

import java.io.Serializable;

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
    private float max = 1;

    /**
     * Create Single Touchpoint event.
     * @param x
     * @param y
     * @param action
     * @param downTime
     */
    public TouchEvent(float x, float y, int action, long downTime) {
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
        this.x = m.getX();
        this.y = m.getY();
        this.action = m.getAction();
        this.downTime = m.getDownTime();
    }

    /**
     * Create MotionEvent from given TouchEvent.
     * @param maxX
     * @param maxY
     * @return
     */
    public MotionEvent getConstructedMotionEvent(int maxX, int maxY) {
        return MotionEvent.obtain(downTime, SystemClock.uptimeMillis(), action, (x / max) * maxX, (y / max) * maxY, 0);
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




