package com.amos.flyinn.summoner;

import android.content.Context;
import android.graphics.Point;


import androidx.test.core.app.ApplicationProvider;

import org.junit.Test;
import org.junit.Assert;
import org.junit.rules.ExpectedException;
import org.junit.runner.RunWith;
import org.mockito.internal.matchers.Null;
import org.robolectric.RobolectricTestRunner;

import java.io.IOException;


@RunWith(RobolectricTestRunner.class)
public class DaemonTest {

    private Context context = ApplicationProvider.getApplicationContext();

    /**
     * Create a new daemon daemon with a provided correct mock context.
     */
    @Test
    public void spawn_adbHappypathTest() {
        Daemon d = new Daemon(context, "127.0.0.1", new Point(0, 0));
        d.spawn_adb();
    }

    /**
     * Create a daemon without a properly provided mock context.
     */
    @Test(expected = NullPointerException.class)
    public void spawn_adbNoContextTest() {
        Daemon d = new Daemon(null, "127.0.0.1", new Point(0, 0));
        d.spawn_adb();
    }

    /**
     * Try to write out the binary to filesystem. This will not work in the local setting.
     * @throws IOException
     */
    @Test(expected = IOException.class)
    public void writeFakeInputToFilesystem() throws IOException {
        Daemon d = new Daemon(context, "127.0.0.1", new Point(0, 0));
        d.writeFakeInputToFilesystem();
    }
}