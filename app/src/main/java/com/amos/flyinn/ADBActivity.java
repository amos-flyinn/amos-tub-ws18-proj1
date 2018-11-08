package com.amos.flyinn;

import android.os.Bundle;
import android.support.design.widget.FloatingActionButton;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.MotionEvent;
import android.view.View;
import android.os.SystemClock;
import com.amos.flyinn.summoner.Daemon;
import com.amos.flyinn.summoner.FakeInputSender;
import java.io.IOException;

public class ADBActivity extends AppCompatActivity {

    private void periodicNagging(FakeInputSender s) throws IOException, InterruptedException {
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

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_adb);
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        FloatingActionButton fab = (FloatingActionButton) findViewById(R.id.fab);
        fab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Daemon d = new Daemon(getApplicationContext());
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
        });
    }

}
