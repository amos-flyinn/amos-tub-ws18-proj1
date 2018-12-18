package com.amos.server;

import org.junit.Rule;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.JUnit4;

import androidx.test.rule.ActivityTestRule;

import static org.junit.Assert.assertTrue;

/**
 * Tests for connection authentication
 */
@RunWith(JUnit4.class)
public class ServerConnAuthTest {

    @Rule
    public ActivityTestRule<ServerConnectionConnAuth> testRule =
            new ActivityTestRule(ServerConnectionConnAuth.class, false, true);

    /**
     * Tests whether ServerConnectionConnAuth activity finishes after being started by
     * ActivityTestRule (it should since nearby cannot start advertising)
     *
     * @throws InterruptedException
     */
    @Test
    public void finishAfterNearbyImpossible() throws InterruptedException {
        Thread.sleep(2000);
        assertTrue(testRule.getActivity().isFinishing());
    }
}
