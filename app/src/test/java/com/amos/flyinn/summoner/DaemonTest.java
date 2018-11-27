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


@RunWith(RobolectricTestRunner.class)
public class DaemonTest {

    private Context context = ApplicationProvider.getApplicationContext();

    @Test
    public void spawn_adbHappypathTest() {
        Daemon d = new Daemon(context, "127.0.0.1", new Point(0, 0));
        d.spawn_adb();
    }

    @Test(expected = NullPointerException.class)
    public void spawn_adbNoContextTest() {
        Daemon d = new Daemon(null, "127.0.0.1", new Point(0, 0));
        d.spawn_adb();
    }

    @Test
    public void writeFakeInputToFilesystem() {
        Daemon d = new Daemon(context, "127.0.0.1", new Point(0, 0));
    }
}