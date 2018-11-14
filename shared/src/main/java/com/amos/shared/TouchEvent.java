package com.amos.shared;

import android.content.res.Resources;
import android.os.SystemClock;
import android.view.MotionEvent;

import java.io.Serializable;

public class TouchEvent implements Serializable {
    public float x;
    public float y;
    public int action;
    public long downTime;
    private int max = 1;

    public TouchEvent(float x, float y, int action, long downTime) {
        this.x = x;
        this.y = y;
        this.action = action;
        this.downTime = downTime;
    }

    public MotionEvent getConstructedMotionEvent() {
        int maxX = Resources.getSystem().getDisplayMetrics().widthPixels;
        int maxY = Resources.getSystem().getDisplayMetrics().heightPixels;

        return MotionEvent.obtain(downTime, SystemClock.uptimeMillis(), action, (x / max) * maxX, (y / max) * maxY, 0);
    }

    TouchEvent(MotionEvent e) {
        this(e.getX(), e.getY(), e.getAction(), e.getDownTime());
    }

    TouchEvent() {
    }
}




