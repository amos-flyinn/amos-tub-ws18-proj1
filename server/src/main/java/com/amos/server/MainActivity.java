package com.amos.server;

import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.MotionEvent;
import android.widget.TextView;
import android.view.View;
import android.view.View.OnTouchListener;
import android.view.View.OnKeyListener;
import android.view.KeyEvent;

import org.w3c.dom.Text;

class MockOutput {
    TextView output;

    public MockOutput(TextView poutput) {
        output = poutput;
    }

    public void write(String msg) {
        output.append(msg + "\n");
    }
}

public class MainActivity extends AppCompatActivity {

    MockOutput output;
    View tracker;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        output = new MockOutput((TextView)findViewById(R.id.debugOutput));

        tracker = findViewById(R.id.tracker);
        tracker.setOnTouchListener(new OnTouchListener(){
            @Override
            public boolean onTouch(View v, MotionEvent e) {
                String msg;
                int count = e.getPointerCount();
                int action = e.getActionMasked();
                int action_index = e.getActionIndex();
                int id;
                double x, y;
                for (int i = 0; i < count; i++) {
                    id = e.getPointerId(i);
                    x = e.getX(i);
                    y = e.getY(i);
                    msg = String.format("Count %d Index %d", count, i);
                    output.write(msg);
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

    @Override
    protected void onStart() {
        super.onStart();
    }
}
