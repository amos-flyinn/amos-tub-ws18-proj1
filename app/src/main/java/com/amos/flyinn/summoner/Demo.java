package com.amos.flyinn.summoner;

import android.content.Context;
import android.graphics.Point;
import android.os.SystemClock;
import android.view.MotionEvent;

import java.io.IOException;

public class Demo {
    private static void periodicNagging(FakeInputSender s) throws IOException, InterruptedException {
        MotionEvent event;
        for (int i = 0; i < 10; i++) {
            long time = SystemClock.uptimeMillis();
            long nextTime = time + 500;

            event = MotionEvent.obtain(time, time, MotionEvent.ACTION_DOWN, 0, 0, 1.0f, 1.0f, 0, 1.0f, 1.0f, 0, 0);
            s.sendMotionEvent(event);
            event = MotionEvent.obtain(nextTime, nextTime, MotionEvent.ACTION_UP, 0, 0, 1.0f, 1.0f, 0, 1.0f, 1.0f, 0, 0);
            s.sendMotionEvent(event);
            Thread.sleep(5000);
        }
    }


    public static void start(Context context,  Point p) {
        Daemon d = new Daemon(context, p);
        FakeInputSender s = new FakeInputSender();
        try {
            d.writeFakeInputToFilesystem();
            d.spawn_adb();
            s.connect();
            periodicNagging(s);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
