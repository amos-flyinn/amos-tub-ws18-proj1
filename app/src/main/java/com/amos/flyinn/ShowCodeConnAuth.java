package com.amos.flyinn;

import android.content.Intent;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.widget.TextView;

import java.security.SecureRandom;

/**
 * Class zeigt CheckCode an
 * und geht rüber zum ClientConnAuth-Service, damit
 * eine Datenübertragung stattfinden kann.
 *
 * TODO javadoc
 */
public class ShowCodeConnAuth extends ClientConnAuthActivity {

    /** */


    private TextView display;
    private static final String CLIENT_NAME_TAG = "Client name is:";
    /**
     *
     * @param savedInstanceState
     */
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        setContentView(R.layout.activity_show_code);
        display = findViewById(R.id.textView2);
        display.setText(super.appCode);
        Log.i(CLIENT_NAME_TAG, "Started advertising " + super.clientName);
        super.onCreate(savedInstanceState);
    }

}
