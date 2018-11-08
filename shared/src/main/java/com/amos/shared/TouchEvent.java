package com.amos.shared;

import java.io.Serializable;

public class TouchEvent implements Serializable {
    public float x;
    public float y;
    public int action;
    public long downTime, eventTime;

    public TouchEvent(float x, float y, int action, long downTime, long eventTime) {
        this.x = x;
        this.y = y;
        this.action = action;
        this.downTime = downTime;
        this.eventTime = eventTime;
    }

    TouchEvent(){}
}

