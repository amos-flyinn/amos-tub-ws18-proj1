package com.amos.server;

import android.os.Bundle;
import android.view.KeyEvent;
import android.view.inputmethod.EditorInfo;
import android.widget.EditText;
import android.widget.TextView;

import com.amos.server.wifibroadcaster.WifiHijackBase;

public class ConnectToClientActivity extends WifiHijackBase {
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
                    onNameEnterFinished(name);
                    return true;
                }
                return false;
            }
        });


    }



    private void onNameEnterFinished(String name) {
        this.changeName(name);
    }



}
