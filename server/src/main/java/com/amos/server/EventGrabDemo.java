package com.amos.server;

import android.os.Bundle;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.Snackbar;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.widget.TextView;
import android.view.View;
import android.view.MotionEvent;
import android.view.View.OnTouchListener;
import android.view.View.OnKeyListener;
import android.view.KeyEvent;
import android.widget.ScrollView;

class MockOutput {
    TextView output;

    public MockOutput(TextView poutput) {
        output = poutput;
    }

    public void write(String msg) {
        output.append(msg + "\n");
    }
}

public class EventGrabDemo extends AppCompatActivity {

    View tracker;
    MockOutput output;
    ScrollView scroller;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_event_grab_demo);
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        FloatingActionButton fab = (FloatingActionButton) findViewById(R.id.fab);
        fab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Snackbar.make(view, "Replace with your own action", Snackbar.LENGTH_LONG)
                        .setAction("Action", null).show();
            }
        });
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);

        scroller = findViewById(R.id.scroller);

        output = new MockOutput((TextView)findViewById(R.id.debug));

        tracker = findViewById(R.id.inputgrab);
        tracker.setOnTouchListener(new OnTouchListener(){
            @Override
            public boolean onTouch(View v, MotionEvent e) {
                String msg;
                int count = e.getPointerCount();
                int action = e.getActionMasked();
                int action_index = e.getActionIndex();
                int id;
                float x, y;
                for (int i = 0; i < count; i++) {
                    id = e.getPointerId(i);
                    x = e.getX(i);
                    y = e.getY(i);
                    msg = String.format("Count %d Index %d", count, i);
                    output.write(msg);
                    scroller.dispatchTouchEvent(
                            MotionEvent.obtain(
                                    e.getDownTime(), e.getEventTime(),
                                    action,
                                    x + tracker.getHeight(), y,
                                    e.getMetaState()
                                    )
                    );
                }
                return true;
            }
        });
        tracker.setOnKeyListener(new OnKeyListener(){
            @Override
            public boolean onKey(View v, int k, KeyEvent e) {
                String msg;
                int action = e.getAction();
                int keycode = e.getKeyCode();
                int meta = e.getMetaState();
                msg = String.format("Keycode: %d Action: %d Meta: %d", keycode, action, meta);
                output.write(msg);
                return true;
            }
        });

    }

}
