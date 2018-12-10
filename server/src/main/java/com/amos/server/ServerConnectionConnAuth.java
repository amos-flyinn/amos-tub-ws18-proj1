package com.amos.server;

import android.content.Intent;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.KeyEvent;
import android.view.inputmethod.EditorInfo;
import android.widget.EditText;
import android.widget.TextView;

/**
 * Server-Class soll tun:
 *  - Starte Service
 *  - Class schickt Eingabe an Service
 */

public class ServerConnectionConnAuth extends ServerConnAuthActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_connect_to_client);
        EditText text = findViewById(R.id.connect_editText);
        text.setOnEditorActionListener(new TextView.OnEditorActionListener() {
            @Override
            public boolean onEditorAction(TextView v, int actionId, KeyEvent event) {
                if (actionId == EditorInfo.IME_ACTION_DONE) {
                    String name = v.getText().toString(); // Get the String
                    /*
                    Intent serviceIntent = new Intent(ServerConnectionConnAuth.this,
                            ServerConnAuthService.class);

                    Bundle b = new Bundle();
                    //b.putParcelable();
                    //b.putString("name", appName); // Übergebe String
                    serviceIntent.putExtras(b); //Put your id to your next Intent
                    startService(serviceIntent);
                    */
                    return true;
                }
                return false;
            }
        });
    }
}
