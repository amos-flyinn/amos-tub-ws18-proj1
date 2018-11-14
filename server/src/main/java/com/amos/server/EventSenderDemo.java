package com.amos.server;

import android.os.HandlerThread;
import android.os.Looper;
import android.os.Message;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.os.SystemClock;
import android.os.Handler;
import android.util.EventLog;
import android.view.MotionEvent;
import android.view.View;
import android.view.View.OnTouchListener;
import android.util.Log;

import com.amos.server.eventsender.EventServer;

import java.io.IOException;
import java.util.Collection;
import java.util.Iterator;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.TimeUnit;

public class EventSenderDemo extends AppCompatActivity {

    View base;

    BlockingQueue<MotionEvent> mq;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_event_sender_demo);
        base = findViewById(R.id.senderlayout);
        mq = new LinkedBlockingQueue<MotionEvent>();
    }

    @Override
    protected void onStart() {
        super.onStart();
        new Thread(new EventServer(mq)).start();
        base.setOnTouchListener(
                new OnTouchListener() {
                    @Override
                    public boolean onTouch(View v, MotionEvent event) {
                        // Log.d("Test", event.toString());
                        mq.add(event);
                        return true;
                    }
                }
        );
    }

    @Override
    protected void onStop() {
        super.onStop();
    }
}
