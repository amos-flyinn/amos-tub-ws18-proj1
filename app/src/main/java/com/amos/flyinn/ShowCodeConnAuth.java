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
 */

public class ShowCodeConnAuth extends AppCompatActivity {


    private final String appCode = generateNumber();
    private final String appName = R.string.flyinn_name + appCode;

    private TextView display;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_show_code);
        display = findViewById(R.id.textView2);
        display.setText(appCode);

        Intent serviceIntent = new Intent(this, ClientConnAuthService.class);

        Bundle b = new Bundle();
        //b.putParcelable();
        b.putString("name", appName); // Übergebe String
        serviceIntent.putExtras(b); //Put your id to your next Intent
        startService(serviceIntent);
    }


    private String generateNumber() {
        SecureRandom rnd = new SecureRandom();
        String number = "";
        for (int i = 0; i < 4; i++) {
            number += rnd.nextInt(10);
        }
        return number;
    }

}
