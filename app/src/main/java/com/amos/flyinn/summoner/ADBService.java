package com.amos.flyinn.summoner;

import android.app.IntentService;
import android.content.Intent;
import android.util.Base64;
import android.util.Log;

import com.google.android.gms.common.util.IOUtils;
import com.tananaev.adblib.AdbConnection;
import com.tananaev.adblib.AdbCrypto;
import com.tananaev.adblib.AdbStream;

import java.io.IOException;
import java.io.InputStream;
import java.net.Socket;
import java.util.Objects;

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

    @Override
    public void onCreate() {
        super.onCreate();
        Log.d("ADBService", "Only called once in a lifetime!");
    }

    /**
     * Create a socket connection to use adb over network with the local phone.
     *
     * @return
     * @throws IOException All errors resulting in us not being able to connect to ADB over network.
     */
    protected AdbConnection connectNetworkADB() throws IOException {
        AdbConnection connection;
        try {
            Socket socket = new Socket("127.0.0.1", 5555);
            AdbCrypto crypto = AdbCrypto.generateAdbKeyPair(data -> {
                String s = Base64.encodeToString(data, 16).replace("\n", "");
                Log.d("ADBService", "New key is" + s);
                return s;
            });
            Log.d("ADBService", "Acquiring connection to port :5555");
            connection = AdbConnection.create(socket, crypto);
            Log.d("ADBService", "Trying to connect to ADB session");
            connection.connect();
        } catch (Exception err) {
            Log.d("ADBService", "Failed to connect to ADB");
            Log.wtf("ADBService", err);
            throw new IOException("Could not start fakeinputlib");
        }
        Log.d("ADBService", "Got the ADB connection");
        return connection;
    }

    /**
     * Run a command using adb shell.
     *
     * @param connection ADB connection used to spawn command on.
     * @param command    Custom command to run
     * @throws IOException Issues in running the command correctly.
     */
    protected void spawnApp(AdbConnection connection, String command) throws Exception {
        if (connection == null) {
            throw new IOException("Connection is null");
        }
        Log.d("AppDaemon", "Spawning the app");
        try {
            AdbStream stream = connection.open(command);
            new Thread(() -> {
                try {
                    for (; ; ) {
                        Log.d("AppDaemon", "Read loop");
                        stream.read();
                    }
                } catch (Exception e) {
                    Log.d("AppDaemon", "Failed the read loop");
                    e.printStackTrace();
                }
                Log.d("AppDaemon", "Stopping the App");
            }).start();
        } catch (Exception e) {
            Log.d("AppDaemon", "Failed to satart listener", e);
            throw e;
        }
    }

    /**
     * Start the adb shell process.
     *
     * @param workIntent
     */
    @Override
    protected void onHandleIntent(Intent workIntent) {
        Log.d("ADBService", "Got intent");
        if (workIntent.getStringExtra("cmd") != null && !"".equals(workIntent.getStringExtra("cmd"))) {
            try {
                Log.d("ADBService", "Got create intent");
                AdbConnection connection = connectNetworkADB();
                spawnApp(connection, workIntent.getStringExtra("cmd"));
                Log.d("ADBService", "Launched adb connection");
            } catch (Exception e) {
                Log.d("ADBService", "Failed to start the adb service", e);
            }
            return;
        }

        Log.d("ADBService", "Get action");
        try {
            Log.d("ADBService", workIntent.getAction());
            switch (Objects.requireNonNull(workIntent.getAction())) {
                case "stream":
                    Log.d("ADBService", "Start proxy...");
                    Socket s = new Socket("127.0.0.1", 1337);
                    Log.d("ADBService", "Proxy connected");
                    InputStream ss = ConnectionSigleton.getInstance().inputStream;
                    new Thread(() -> {
                        try {
                            Log.d("ADBService", "Proxy piping...");
                            IOUtils.copyStream(ss, s.getOutputStream());
                            Log.d("ADBService", "Proxy done piping");
                        } catch (Exception e) {
                            Log.d("ADBService", "Failed to pipe", e);
                        }
                    }).start();
            }
        } catch (Exception e) {
            Log.d("ADBService", "Failed to connect to ADB", e);
        }
    }
}
