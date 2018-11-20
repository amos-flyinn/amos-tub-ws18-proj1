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
        if(Settings.Secure.getInt(this.getContentResolver(), Settings.Global.ADB_ENABLED, 0) == 1) {
            Toast.makeText(this, "debugging mode is enabled", Toast.LENGTH_SHORT).show();
        } else {
            try {
                startActivity(new Intent(android.provider.Settings.ACTION_APPLICATION_DEVELOPMENT_SETTINGS));
                Toast.makeText(this, "Please enable USB debugging.", Toast.LENGTH_SHORT).show();
            } catch(Exception ex) {
                Toast.makeText(this, "Please enable first the debugging mode and then there the USB debugging.\n" +
                        "Mostly it works with tapping multiple times the 'Software Build number' label.", Toast.LENGTH_SHORT).show();
                startActivity(new Intent(Settings.ACTION_DEVICE_INFO_SETTINGS));
            }
        }
    }
}
