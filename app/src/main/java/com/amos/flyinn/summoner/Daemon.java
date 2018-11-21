package com.amos.flyinn.summoner;

import android.content.Context;
import android.graphics.Point;
import android.os.Environment;
import android.os.StrictMode;
import android.util.Log;

import com.amos.flyinn.R;
import com.tananaev.adblib.AdbBase64;
import com.tananaev.adblib.AdbConnection;
import com.tananaev.adblib.AdbCrypto;
import com.tananaev.adblib.AdbStream;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.Socket;
import java.util.Locale;

public class Daemon {
    private final String FAKE_INPUT_SERVER_PATH = Environment.getExternalStorageDirectory() + "/Android/data/flyinn_fakeinputlib.jar";
    // NOTE: cant use the FAKE_INPUT_SERVER_PATH as this will lead to permission issues, use "/sdcard/", which is a symlink.
    private final String SHELL_FAKE_INPUT_NOHUP_COMMAND = "shell:CLASSPATH=/sdcard/Android/data/flyinn_fakeinputlib.jar nohup app_process / com.amos.fakeinputlib.Main %s %d %d";
    //private final String CMD = "shell:CLASSPATH=%%s nohup app_process / com.amos.fakeinputlib.Main %s %d %d";
    private final String execCMD;
    private Context context;

    private String binaryPath;

    public Daemon(Context context, String addr, Point p) {
        execCMD = String.format(Locale.ENGLISH, SHELL_FAKE_INPUT_NOHUP_COMMAND, addr, p.x, p.y);
        Log.d("AdbDaemon", this.execCMD);
        this.context = context;
    }

    public void writeFakeInputToFilesystem() throws IOException {
        InputStream in = this.context.getResources().openRawResource(R.raw.flyinn_fakeinputlib);


        File fpath = new File(FAKE_INPUT_SERVER_PATH);
        FileOutputStream out = new FileOutputStream(fpath.toString());

        byte[] buff = new byte[2048];
        try {
            for (int read; (read = in.read(buff)) > 0; ) {
                out.write(buff, 0, read);
            }
            binaryPath = fpath.toString();
        } finally {
            in.close();
            out.close();
        }
    }

    public void spawn_adb() throws Exception {
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
        try {
            AdbStream stream = connection.open(execCMD);
            byte[] bytes = {};

            // Hack to flush the connection and wait until execution, read will always throw.
            stream.write(bytes, true);
            stream.read();
        } catch (IOException e) {
        } finally {
            connection.close();
        }
    }

}
