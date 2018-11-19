package com.amos.flyinn.requirementsCheck;

import android.content.Intent;
import android.provider.Settings;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.widget.Toast;

import com.amos.flyinn.R;

public class RequirementsCheckAvtivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_requirements_check_avtivity);

        checkForDebuggingMode();
    }

    private void checkForDebuggingMode() {

        if(Settings.Secure.getInt(this.getContentResolver(), Settings.Secure.ADB_ENABLED, 0) == 1) {
            
            Toast.makeText(this, "debugging mode is enabled", Toast.LENGTH_SHORT).show();
        } else {

            //this will open the developer options menu from the settings, needs more testing if the user
            //needs to click on the build number multiple times or it works without it.
            startActivity(new Intent(android.provider.Settings.ACTION_APPLICATION_DEVELOPMENT_SETTINGS));
        }
    }
}
