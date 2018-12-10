package com.amos.flyinn;

import android.content.Intent;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
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
    private final int CODE_LENGTH = 4;

    /** */
    private final String appCode = generateNumber(CODE_LENGTH);

    private TextView display;

    /**
     *
     * @param savedInstanceState
     */
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        setContentView(R.layout.activity_show_code);
        display = findViewById(R.id.textView2);
        display.setText(appCode);
        super.onCreate(savedInstanceState);
    }

    /**
     *
     * @param length
     * @return
     */
    private String generateNumber(int length) {
        SecureRandom rnd = new SecureRandom();
        String number = "";
        for (int i = 0; i < length; i++) {
            number += rnd.nextInt(10);
        }
        return number;
    }

    /**
     *
     * @return
     */
    @Override
    protected String generateName() {
        return R.string.flyinn_name + appCode;
    }
}
