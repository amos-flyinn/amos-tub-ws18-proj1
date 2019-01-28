package com.amos.flyinn.service;

import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.graphics.PixelFormat;
import android.os.Handler;
import android.os.IBinder;
import android.view.Gravity;
import android.view.View;
import android.view.WindowManager;
import android.widget.Button;

public class FPSOverlay extends Service {
    private View topLeftView;
    Handler handler;


    private Button overlayedButton;
    private WindowManager wm;
    WindowManager.LayoutParams params;

    public FPSOverlay() {
    }

    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }

    private void runOnUiThread(Runnable runnable) {
        handler.post(runnable);
    }

    @Override
    public void onCreate() {
        handler = new Handler();
        super.onCreate();
        wm = (WindowManager) getSystemService(Context.WINDOW_SERVICE);


        overlayedButton = new Button(this);
        overlayedButton.setText("");
        overlayedButton.setWidth(1);
        overlayedButton.setHeight(1);
        overlayedButton.setBackgroundColor(0xffffffff);
        overlayedButton.setAlpha(1.0f);


        params = new WindowManager.LayoutParams(WindowManager.LayoutParams.WRAP_CONTENT, WindowManager.LayoutParams.WRAP_CONTENT, WindowManager.LayoutParams.TYPE_APPLICATION_OVERLAY, WindowManager.LayoutParams.FLAG_NOT_FOCUSABLE | WindowManager.LayoutParams.FLAG_NOT_TOUCH_MODAL, PixelFormat.TRANSLUCENT);

        params.gravity = Gravity.LEFT | Gravity.TOP;
        params.x = 0;
        params.y = 0;
        wm.addView(overlayedButton, params);

        topLeftView = new View(this);
        WindowManager.LayoutParams topLeftParams = new WindowManager.LayoutParams(
                WindowManager.LayoutParams.WRAP_CONTENT,
                WindowManager.LayoutParams.WRAP_CONTENT,
                WindowManager.LayoutParams.TYPE_APPLICATION_OVERLAY,
                WindowManager.LayoutParams.FLAG_NOT_FOCUSABLE | WindowManager.LayoutParams.FLAG_NOT_TOUCH_MODAL,
                PixelFormat.TRANSLUCENT
        );
        topLeftParams.gravity = Gravity.LEFT | Gravity.TOP;
        topLeftParams.x = 0;
        topLeftParams.y = 0;
        topLeftParams.width = 1;
        topLeftParams.height = 1;
        wm.addView(topLeftView, topLeftParams);

        new Thread(() -> {
           while (true)  {
               try {
                   Thread.sleep((int) 1000/5);
               } catch (InterruptedException e) {
                   e.printStackTrace();
               }
               runOnUiThread(new Runnable() {
                   @Override
                   public void run() {
                       updateUI();
                   }
               });
           }
        }).start();
    }

    int color = 0;

    public void updateUI() {
        color = (0x02020202 + color) % 0xeeeeeeee;
        overlayedButton.setBackgroundColor(color);
        overlayedButton.setAlpha(1.0f);
        wm.updateViewLayout(overlayedButton, params);
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
    }
}
