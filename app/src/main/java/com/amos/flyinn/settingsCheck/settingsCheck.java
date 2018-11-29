package com.amos.flyinn.settingsCheck;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.os.StrictMode;
import android.provider.Settings;
import android.util.Log;
import android.widget.Toast;

import com.amos.flyinn.MainActivity;
import com.amos.flyinn.summoner.ADBService;
import com.tananaev.adblib.AdbBase64;
import com.tananaev.adblib.AdbConnection;
import com.tananaev.adblib.AdbCrypto;

import java.io.Console;
import java.io.IOException;
import java.net.Socket;
import java.util.HashSet;
import java.util.List;

import static android.support.v4.content.ContextCompat.startActivity;

public class settingsCheck {

    public enum SettingsType {
        Developer, ADBperUSB, ADBperNetwork
    }

    private Context context;
    private ADBService service = new ADBService();
    public settingsCheck(Context context) {

        this.context=context;
    }

    /**
     * Collect all disabled settings which are necessary to be enabled
     * for this app in a list
     * @return List<Settings> List of disabled settings.
     */
    public HashSet<SettingsType> GetMissingSettings() {
        HashSet<SettingsType> missingSettings=new HashSet<SettingsType>();

        // Developer settings
        if(android.provider.Settings.Secure.getInt(context.getContentResolver(),
                android.provider.Settings.Global.DEVELOPMENT_SETTINGS_ENABLED , 0) != 1) {
            missingSettings.add(SettingsType.Developer);
        }

        // ADBperUSB settings
        if(android.provider.Settings.Secure.getInt(context.getContentResolver(),
                android.provider.Settings.Global.ADB_ENABLED , 0) != 1) {
            missingSettings.add(SettingsType.ADBperUSB);
        }

        // ADBperNetwork settings
        StrictMode.ThreadPolicy policy = new StrictMode.ThreadPolicy.Builder().permitAll().build();
        StrictMode.setThreadPolicy(policy);
        try {
            AdbCrypto crypto = AdbCrypto.generateAdbKeyPair(new AdbBase64() {
                @Override
                public String encodeToString(byte[] data) {
                    return android.util.Base64.encodeToString(data, 16);
                }
            });
            AdbConnection connection = AdbConnection.create(new Socket("127.0.0.1", 5555), crypto);
            connection.connect();
            connection.close();
        } catch (Exception err) {
            //err.printStackTrace();
            missingSettings.add(SettingsType.ADBperNetwork);
        }

        /*// DEBUG
        Toast.makeText(context,
                missingSettings.toString(), Toast.LENGTH_LONG).show();
        Log.d("GetMissingSettings", missingSettings.toString());*/
        
        return missingSettings;
    }


    /**
     * Open menu with Software version for enabling developer settings
     */
    public void FixManualDeveloperSettings() {
        context.startActivity(new Intent(Settings.ACTION_DEVICE_INFO_SETTINGS));
    }

    /**
     * Open Developer menu for enabling usb
     */
    public void FixManualADBperUsbSettings() {
        context.startActivity(new Intent(Settings.ACTION_APPLICATION_DEVELOPMENT_SETTINGS));
    }


}

