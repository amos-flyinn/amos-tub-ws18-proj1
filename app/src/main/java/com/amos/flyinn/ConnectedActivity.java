package com.amos.flyinn;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.widget.Button;
import android.widget.TextView;

public class ConnectedActivity extends Activity {
    private Button switchToHomeScreenButton;
    private Button closeConnectionButton;
    private TextView connectedMessage;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_connected);

        closeConnectionButton = findViewById(R.id.close_connection);
        switchToHomeScreenButton = findViewById(R.id.switch_home_screen);
        connectedMessage = findViewById(R.id.connected_message);

        //Giving callback to the close connection button
        closeConnectionButton.setOnClickListener(view -> restartAPP());

        //Giving callback to the switch to home button
        switchToHomeScreenButton.setOnClickListener(view -> switchToHomeScreen());
    }

    /**
     * Method to minimize the app and go to the home screen
     */
    public void switchToHomeScreen() {
        Intent startMain = new Intent(Intent.ACTION_MAIN);
        startMain.addCategory(Intent.CATEGORY_HOME);
        startMain.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
        startActivity(startMain);
    }

    private void restartAPP() {
        Intent i = getBaseContext()
                .getPackageManager()
                .getLaunchIntentForPackage(getBaseContext().getPackageName());
        i.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
        startActivity(i);
    }
}
