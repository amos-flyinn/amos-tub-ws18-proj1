package com.amos.flyinn.nearbyservice;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.robolectric.RobolectricTestRunner;

/**
 * See inside NearbyService for tests including NearbyServer
 */
@RunWith(RobolectricTestRunner.class)
public class NearbyServerTest {

    @Test(expected = NullPointerException.class)
    public void init_null() {
        NearbyServer server = new NearbyServer(null);
    }
}
