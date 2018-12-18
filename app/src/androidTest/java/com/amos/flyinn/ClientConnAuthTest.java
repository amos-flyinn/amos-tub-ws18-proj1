package com.amos.flyinn;

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
public class ClientConnAuthTest {

    @Rule
    public ActivityTestRule<ShowCodeConnAuth> testRule =
            new ActivityTestRule(ShowCodeConnAuth.class, false, true);

    /**
     * Tests whether ShowCodeConnAuth activity finishes after being started by ActivityTestRule
     * (it should since nearby cannot start discovering)
     *
     * @throws InterruptedException
     */
    @Test
    public void finishAfterNearbyImpossible() throws InterruptedException {
        Thread.sleep(2000);
        assertTrue(testRule.getActivity().isFinishing());
    }
}
