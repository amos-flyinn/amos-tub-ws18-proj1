package com.amos.shared;

import android.os.SystemClock;
import android.view.MotionEvent;

import java.io.Serializable;

public class TouchEvent implements Serializable {
    public float x;
    public float y;
    public int action;
    public long downTime;
    private float max = 1;

    public TouchEvent(float x, float y, int action, long downTime) {
        this.x = x;
        this.y = y;
        this.action = action;
        this.downTime = downTime;
    }

    public MotionEvent getConstructedMotionEvent(int maxX, int maxY) {
        return MotionEvent.obtain(downTime, SystemClock.uptimeMillis(), action, (x / max) * maxX, (y / max) * maxY, 0);
    }

    @Override
    public String toString() {
        return String.format("TE: X: %f Y: %f", x, y);
    }
}




