package com.amos.server;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.util.Log;
import android.widget.TextView;

import com.amos.server.mediadecoder.MediaDecoderController;
import com.amos.server.nearby.ConnectCallback;
import com.amos.server.nearby.ServerConnection;

public class ConnectionSetupServerActivity extends Activity {

    private TextView progressText;

    /**
     * Connection singleton managing nearby connection
     */
    private ServerConnection connection;

    private static final String TAG = "ConnectionSetup";

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_connection_setup);
        Thread.setDefaultUncaughtExceptionHandler(new ExceptionHandler(this));

        progressText = findViewById(R.id.progressText);

        connection = ServerConnection.getInstance();

        // only correct actions will be processed
        Intent intent = getIntent();
        if ("connect".equals(intent.getAction())) {
            buildConnection(intent.getStringExtra("name"));
        } else {
            toInitialActivity();
        }
    }

    private void toInitialActivity() {
        Intent intent = new Intent(this, ConnectToClientActivity.class);
        startActivity(intent);
    }

    private void toConnectedActivity() {
        Intent intent = new Intent(this, ConnectedActivity.class);
        startActivity(intent);
    }

    /**
     * Create connection to the given destination server
     */
    private void buildConnection(String name) {
        setProgressText("Connecting to " + name);
        // connection.connectTo(name, new ConnectCallback() {
        connection.discoverConnect(name, new ConnectCallback() {
            @Override
            public void success(String message) {
                Log.d(TAG, "Successfully connected to " + name);
                // toast(String.format("Successfully connected to %s", name));
                toast(message);
                toConnectedActivity();
            }

            @Override
            public void failure(String message) {
                Log.d(TAG, "Failed to connect to " + name);
                // toast(String.format("Failed to connect to %s", name));
                toast(message);
                MediaDecoderController.getInstance().reset();
                toInitialActivity();
            }
        });
    }

    private void toast(String message) {
        Toast toast = Toast.makeText(this, message, Toast.LENGTH_SHORT);
        toast.show();
    }

    private void setProgressText(String message) {
        progressText.setText(message);
    }
}
