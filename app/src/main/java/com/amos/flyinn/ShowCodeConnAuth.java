package com.amos.flyinn;

import android.os.Bundle;
import android.widget.TextView;

import java.security.SecureRandom;

/**
 * Client for FlyInn authentication. Displays a random 4-digit app code and
 * generates the client name (containing the code). All network connection goes through
 * superclass via nearby connection library.
 */
public class ShowCodeConnAuth extends ClientConnAuthActivity {

    /** Length of the numeric app code displayed to the user, used to connect client and server. */
    private final int CODE_LENGTH = 4;

    /** The numeric app code displayed to the user, used to connect client and server. */
    private final String appCode = generateNumber();

    private TextView display;


    /**
     * Returns the numeric client ID displayed to the user.
     *
     * @return The number identifying the client with CODE_LENGTH digits as string (e.g., 0000-9999)
     */
    protected String getAppCode() { return appCode; }


    /**
     * Displays the 4-digit app code to the user, in addition to calling super.onCreate().
     *
     * @param savedInstanceState
     */
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        setContentView(R.layout.activity_show_code);
        Thread.setDefaultUncaughtExceptionHandler(new ExceptionHandler(this));


        display = findViewById(R.id.textView2);
        display.setText(appCode);


        super.onCreate(savedInstanceState);
    }


    /**
     * Generates the random app code that will be displayed to the user and
     * utilised to connect client and server.
     *
     * @return The 4-digit app code as string ("0000" to "9999").
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
     * Returns the name of the client, consisting of R.string.flyinn_name and the 4-digit app code.
     *
     * @return The name of this client, containing the app code as final characters.
     */
    @Override
    protected String generateName() {
        return this.getString(R.string.flyinn_name) + appCode;
    }
}
