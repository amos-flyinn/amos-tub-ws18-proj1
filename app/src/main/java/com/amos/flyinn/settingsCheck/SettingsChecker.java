package com.amos.flyinn.settingsCheck;

import android.content.Context;
import android.content.Intent;
import android.provider.Settings;

import com.amos.flyinn.summoner.ADBService;
import com.tananaev.adblib.AdbBase64;
import com.tananaev.adblib.AdbConnection;
import com.tananaev.adblib.AdbCrypto;

import java.net.Socket;
import java.util.HashSet;


/**
 * Checks the software settings which are necessary
 */
public class SettingsChecker {

    private Context context;
    private ADBService service = new ADBService();
    /**
     * Constructor sets context
     *
     * @param context The context where the settings should be checked
     */
    public SettingsChecker(Context context) {
        this.context = context;
    }

    /**
     * Collect all disabled settings which are necessary to be enabled
     * for this app in a list
     *
     * @return List<Settings> List of disabled settings.
     */
    public HashSet<SettingsType> GetMissingSettings() {
        HashSet<SettingsType> missingSettings = new HashSet<SettingsType>();

        // Developer settings
        if (android.provider.Settings.Secure.getInt(context.getContentResolver(),
                android.provider.Settings.Global.DEVELOPMENT_SETTINGS_ENABLED, 0) != 1) {
            missingSettings.add(SettingsType.Developer);
        }

        // ADBperUSB settings
        if (android.provider.Settings.Secure.getInt(context.getContentResolver(),
                android.provider.Settings.Global.ADB_ENABLED, 0) != 1) {
            missingSettings.add(SettingsType.ADBperUSB);
        }

        // ADBperNetwork settings
        Thread t = new Thread(new Runnable() { // Use own thread because of ThreadPolicy
            @Override
            public void run() {
                // Test a connection
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
                    missingSettings.add(SettingsType.ADBperNetwork);
                }
            }
        });

        t.start();

        try {
            t.join();
        } catch (InterruptedException e) {
            if (missingSettings.contains(SettingsType.ADBperNetwork) == false)
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

    /**
     * Different types of settings
     */
    public enum SettingsType {
        /**
         * Developer settings
         */
        Developer,
        /**
         * ADB/USB debugging
         */
        ADBperUSB,
        /**
         * ADB per network debugging
         */
        ADBperNetwork
    }


}

