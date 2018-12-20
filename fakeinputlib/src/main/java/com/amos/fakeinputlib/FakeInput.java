package com.amos.fakeinputlib;

import android.annotation.SuppressLint;
import android.hardware.input.InputManager;
import android.os.SystemClock;
import android.util.Log;
import android.view.InputDevice;
import android.view.InputEvent;
import android.view.MotionEvent;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;

public class FakeInput {

    private static final String TAG = FakeInput.class.getName();

    private final Method injectInputEventMethod;
    private final InputManager im;


    @SuppressLint("PrivateApi")
    FakeInput() throws Exception {
        /* https://omerjerk.in/index.php/2016/11/07/create-touch-events-programmatically-in-android */
        String methodName = "getInstance";
        Object[] objArr = new Object[0];
        im = (InputManager) InputManager.class.getDeclaredMethod(methodName).invoke(null, objArr);

        methodName = "obtain";
        MotionEvent.class.getDeclaredMethod(methodName).setAccessible(true);

        methodName = "injectInputEvent";
        injectInputEventMethod = InputManager.class.getMethod(methodName, InputEvent.class, Integer.TYPE);
    }

    public boolean sendMotionEvent(MotionEvent e) {
        e.setSource(InputDevice.SOURCE_TOUCHSCREEN);

        try {
            injectInputEventMethod.invoke(im, e, 0);
        } catch (IllegalAccessException e1) {
            Log.wtf(TAG, "failed to invoke motion event", e1);
            return false;
        } catch (IllegalArgumentException e1) {
            Log.wtf(TAG, "failed to invoke motion event", e1);
            return false;
        } catch (InvocationTargetException e1) {
            Log.e(TAG, "failed to invoke motion event", e1.getCause());
            return false;
        }

        return true;
    }

    void sendTap(int x, int y) throws Exception {
        long time = SystemClock.uptimeMillis();
        MotionEvent eventDown = MotionEvent.obtain(time, time, MotionEvent.ACTION_DOWN, x, y, 1.0f, 1.0f, 0, 1.0f, 1.0f, 0, 0);
        eventDown.setSource(InputDevice.SOURCE_TOUCHSCREEN);

        long nextTime = time + 500;
        MotionEvent eventUp = MotionEvent.obtain(nextTime, nextTime, MotionEvent.ACTION_UP, x, y, 1.0f, 1.0f, 0, 1.0f, 1.0f, 0, 0);
        eventUp.setSource(InputDevice.SOURCE_TOUCHSCREEN);

        injectInputEventMethod.invoke(im, eventDown, 0);
        injectInputEventMethod.invoke(im, eventUp, 0);
    }
}
