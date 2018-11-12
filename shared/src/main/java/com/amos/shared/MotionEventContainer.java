package com.amos.shared;

import android.view.MotionEvent;

import java.io.Serializable;

public class MotionEventContainer implements Serializable {
    public MotionEvent e;

    MotionEventContainer(MotionEvent event) {
        this.e = event;
    }
}
