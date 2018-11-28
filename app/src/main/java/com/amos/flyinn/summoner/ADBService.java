package com.amos.flyinn.summoner;

import android.app.IntentService;
import android.content.Intent;
import android.util.Log;

import com.tananaev.adblib.AdbBase64;
import com.tananaev.adblib.AdbConnection;
import com.tananaev.adblib.AdbCrypto;
import com.tananaev.adblib.AdbStream;

import java.io.IOException;
import java.net.Socket;
import java.security.NoSuchAlgorithmException;

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
     * Create a socket connection to use adb over network with the local phone.
     * @return
     * @throws IOException
     *          All errors resulting in us not being able to connect to ADB over network.
     */
    protected AdbConnection connectNetworkADB() throws IOException {
        AdbConnection connection;
        try {
            Socket socket = new Socket("127.0.0.1", 5555);
            AdbCrypto crypto = AdbCrypto.generateAdbKeyPair(new AdbBase64() {
                @Override
                public String encodeToString(byte[] data) {
                    return android.util.Base64.encodeToString(data, 16);
                }
            });
            connection = AdbConnection.create(socket, crypto);
            connection.connect();
        } catch (Exception err) {
            throw new IOException("Could not start fakeinputlib");
        }
        return connection;
    }

    /**
     * Run a command using adb shell.
     * @param connection
     *          ADB connection used to spawn command on.
     * @param command
     *          Custom command to run
     * @throws IOException
     *          Issues in running the command correctly.
     */
    protected void spawnApp(AdbConnection connection, String command) throws IOException {
        if (connection == null) {
            throw new IOException("Connection is null");
        }
        Log.d("AppDaemon", "Spawning the app");
        try {
            AdbStream stream = connection.open(command);
            while (true)
                stream.read();
        } catch (InterruptedException err) {
            // Do nothing on interrupts for now
        }
    }

    /**
     * Start the adb shell process.
     * @param workIntent
     */
    @Override
    protected void onHandleIntent(Intent workIntent) {
        try {
            AdbConnection connection = connectNetworkADB();
            spawnApp(connection, workIntent.getStringExtra("cmd"));
        } catch (Exception e) {}
    }
}
