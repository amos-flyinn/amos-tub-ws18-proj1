package com.amos.flyinn;

import android.os.Bundle;
import android.widget.TextView;

import java.security.SecureRandom;

/**
 * 
 */
public class ShowCodeConnAuth extends ClientConnAuthActivity {

    /** */
    private final int CODE_LENGTH = 4;

    /** */
    private final String appCode = generateNumber();

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
     * @return
     */
    private String generateNumber() {
        SecureRandom rnd = new SecureRandom();
        String number = "";
        for (int i = 0; i < CODE_LENGTH; i++) {
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
