package com.amos.server;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.view.inputmethod.EditorInfo;
import android.widget.Button;
import android.widget.EditText;

import com.amos.server.SettingsScreen.SettingsActivity;

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
        setSettingsListener();
    }

    private void setSettingsListener() {
        Button btn = findViewById(R.id.settings_btn);
        Context context = this;
        btn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(context, SettingsActivity.class);
                startActivity(intent);
            }
        });
    }
}
