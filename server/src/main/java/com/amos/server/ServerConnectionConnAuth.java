package com.amos.server;

import android.content.Intent;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.KeyEvent;
import android.view.inputmethod.EditorInfo;
import android.widget.EditText;
import android.widget.TextView;

import java.security.SecureRandom;

/**
 * Server-Class soll tun:
 *  - Starte Service
 *  - Class schickt Eingabe an Service
 *
 *  TODO javadoc
 */

public class ServerConnectionConnAuth extends ServerConnAuthActivity {

    /**
     *
     * @param savedInstanceState
     */
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_connect_to_client);
        EditText text = findViewById(R.id.connect_editText);

        text.setOnEditorActionListener((v, actionId, event) -> {
            // client code input by user
            if (actionId == EditorInfo.IME_ACTION_DONE) {
                String name = v.getText().toString();
                super.connectToClient(name); // returns boolean on success
                return true;
            }
            return false;
        });
    }

    /**
     * Generates a name for the server.
     * @return The server name, consisting of R.string.flyinn_server_name + a random string
     */
    @Override
    protected String generateName() {
        int suffix = 5;
        String AB = "0123456789ABCDEFGHIJKLMNOPQRSTUVWXYZabcdefghijklmnopqrstuvwxyz";
        SecureRandom rnd = new SecureRandom();

        StringBuilder sb = new StringBuilder(suffix);
        for (int i = 0; i < suffix; i++) {
            sb.append(AB.charAt(rnd.nextInt(AB.length())));
        }

        return R.string.flyinn_server_name + sb.toString();
    }
}
