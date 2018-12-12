package com.amos.server;

import android.os.Bundle;
import android.view.inputmethod.EditorInfo;
import android.widget.EditText;

/**
 * Server for FlyInn authentication. Lets user input a string which is then used to try to
 * connect to the client identified by that string. Connection itself is handled by superclass.
 */
public class ServerConnectionConnAuth extends ServerConnAuthActivity {

    /**
     * Displays a text field to the user. On input of a code (which should be displayed
     * on the client app) it is given to the superclass to try to connect to a client
     * identified by the code.
     *
     * @param savedInstanceState
     */
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_connect_to_client);
        EditText text = findViewById(R.id.connect_editText);

        text.setOnEditorActionListener((v, actionId, event) -> {
            // client (app) code input by user
            if (actionId == EditorInfo.IME_ACTION_DONE) {
                String name = v.getText().toString();
                super.connectToClient(name); // returns boolean on success
                return true;
            }
            return false;
        });
    }
}
