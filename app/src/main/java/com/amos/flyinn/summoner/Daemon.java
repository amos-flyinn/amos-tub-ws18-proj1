package com.amos.flyinn.summoner;

import android.content.Context;
import android.os.Environment;
import android.os.StrictMode;

import com.amos.flyinn.R;
import com.tananaev.adblib.AdbBase64;
import com.tananaev.adblib.AdbConnection;
import com.tananaev.adblib.AdbCrypto;
import com.tananaev.adblib.AdbStream;

import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.Socket;

public class Daemon {
    private final String FAKE_INPUT_SERVER_PATH = Environment.getExternalStorageDirectory() + "/Android/data/flyinn_fakeinputlib.jar";
    // NOTE: cant use the FAKE_INPUT_SERVER_PATH as this will lead to permission issues, use "/sdcard/", which is a symlink.
    private String SHELL_FAKE_INPUT_NOHUP_COMMAND = "shell:CLASSPATH=/sdcard/Android/data/flyinn_fakeinputlib.jar nohup app_process / com.amos.fakeinputlib.Main ";

    private Context context;

    public Daemon(Context context, String addr) {
        SHELL_FAKE_INPUT_NOHUP_COMMAND += addr;
        System.out.println(FAKE_INPUT_SERVER_PATH);
        System.out.println(SHELL_FAKE_INPUT_NOHUP_COMMAND);
        this.context = context;
    }

    public void writeFakeInputToFilesystem() throws IOException {
            InputStream in = this.context.getResources().openRawResource(R.raw.flyinn_fakeinputlib);
            FileOutputStream out = new FileOutputStream(FAKE_INPUT_SERVER_PATH);

            byte[] buff = new byte[2048];
            try {
                for (int read;(read = in.read(buff)) > 0;) {
                    out.write(buff, 0, read);
                }
            } finally {
                in.close();
                out.close();
            }
    }

    public void spawn_adb() throws Exception {
            // TODO(lbb): remfactor in new Thread.
            StrictMode.ThreadPolicy policy = new StrictMode.ThreadPolicy.Builder().permitAll().build();
            StrictMode.setThreadPolicy(policy);

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
                AdbStream stream = connection.open(SHELL_FAKE_INPUT_NOHUP_COMMAND);
                byte[] bytes = {};

                // Hack to flush the connection and wait until execution, read will always throw.
                stream.write(bytes, true);
                stream.read();
            } catch (IOException e) { }
            finally {
                connection.close();
            }
    }

}
