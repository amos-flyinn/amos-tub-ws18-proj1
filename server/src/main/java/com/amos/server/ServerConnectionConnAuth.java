package com.amos.server;

import android.os.Bundle;
import android.view.inputmethod.EditorInfo;
import android.widget.EditText;

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
}
