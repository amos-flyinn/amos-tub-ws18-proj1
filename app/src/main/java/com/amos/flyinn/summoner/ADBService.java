package com.amos.flyinn.summoner;

import android.app.IntentService;
import android.content.Intent;
import android.os.StrictMode;
import android.util.Log;

import com.tananaev.adblib.AdbBase64;
import com.tananaev.adblib.AdbConnection;
import com.tananaev.adblib.AdbCrypto;
import com.tananaev.adblib.AdbStream;

import java.net.Socket;

/**
 * Background service controlling the start of fakeinputlib via an adb shell.
 */
public class ADBService extends IntentService {
    /**
     * Creates an IntentService.  Invoked by your subclass's constructor.
     *
     * @param name Used to name the worker thread, important only for debugging.
     */
    public ADBService(String name) {
        super(name);
    }

    public ADBService() {
        super("ADBService");
    }

    /**
     * Start the adb shell process.
     * @param workIntent
     */
    @Override
    protected void onHandleIntent(Intent workIntent) {
        try {
            // TODO(lbb): remfactor in new Thread.
            StrictMode.ThreadPolicy policy = new StrictMode.ThreadPolicy.Builder().permitAll().build();
            StrictMode.setThreadPolicy(policy);

            Log.d("AppDaemon", "Spawning the app");

            Socket socket = new Socket("127.0.0.1", 5555);

            AdbCrypto crypto = AdbCrypto.generateAdbKeyPair(new AdbBase64() {
                @Override
                public String encodeToString(byte[] data) {
                    return android.util.Base64.encodeToString(data, 16);
                }
            });
            AdbConnection connection = AdbConnection.create(socket, crypto);
            connection.connect();
            AdbStream stream = connection.open(workIntent.getStringExtra("cmd"));
            while (true)
                stream.read();
        } catch (Exception e) {}
    }
}
