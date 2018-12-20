package com.amos.flyinn.summoner;

import android.content.Context;
import android.content.Intent;
import android.graphics.Point;
import android.os.Environment;
import android.util.Log;

import com.amos.flyinn.R;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.Locale;

/**
 * Handles deployment of the fakeinputlib package.
 * <p>
 * fakeinputlib is a separate java binary which is run with adb debugging permissions to
 * allow injection of input events into other applications. This daemon controls copying the
 * fakeinputlib binary to a specified location and its execution over adb over network.
 */
public class Daemon {
    private final String FAKE_INPUT_SERVER_PATH = Environment.getExternalStorageDirectory() + "/Android/data/flyinn_fakeinputlib.jar";
    // NOTE: cant use the FAKE_INPUT_SERVER_PATH as this will lead to permission issues, use "/sdcard/", which is a symlink.
    private final String SHELL_FAKE_INPUT_NOHUP_COMMAND = "shell:CLASSPATH=/sdcard/Android/data/flyinn_fakeinputlib.jar app_process / com.amos.fakeinputlib.Main %s %d %d";
    //private final String CMD = "shell:CLASSPATH=%%s nohup app_process / com.amos.fakeinputlib.Main %s %d %d";
    private final String execCMD;
    private Context context;

    private String binaryPath;

    /**
     * Create a new Daemon instance.
     *
     * @param context          Application context. Used to get application binary.
     * @param addr             Server address. This will be passed as a commandline argument to the fakeinputlib binary.
     * @param screenDimensions Screen size including status bar and navbar.
     */
    public Daemon(Context context, String addr, Point screenDimensions) {
        execCMD = String.format(Locale.ENGLISH, SHELL_FAKE_INPUT_NOHUP_COMMAND, addr, screenDimensions.x, screenDimensions.y);
        Log.d("AdbDaemon", this.execCMD);
        this.context = context;
    }

    /**
     * Copy fakeinputlib binary from APK to local directory.
     *
     * @throws IOException
     */
    public void writeFakeInputToFilesystem() throws IOException {
        InputStream in = this.context.getResources().openRawResource(R.raw.fakeinputlib);


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

    /**
     * Start ADBService, which will call fakeinputlib on commandline. This is necessary in order to
     * keep fakeinputlib alive even when the application itself is closed.
     *
     * @throws Exception
     */
    public void spawn_adb() {
        Intent i = new Intent(context, ADBService.class);
        i.putExtra("cmd", execCMD);
        context.startService(i);
    }
}
