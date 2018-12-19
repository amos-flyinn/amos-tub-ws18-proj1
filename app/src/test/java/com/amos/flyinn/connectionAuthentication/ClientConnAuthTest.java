package com.amos.flyinn.connectionAuthentication;

import android.widget.TextView;

import com.amos.flyinn.R;
import com.amos.flyinn.ShowCodeConnAuth;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.robolectric.Robolectric;
import org.robolectric.RobolectricTestRunner;

import static org.junit.Assert.assertTrue;

/**
 * Tests for the client-side connection authentication
 */
@RunWith(RobolectricTestRunner.class)
public class ClientConnAuthTest {

    /**
     * Tests whether the code shown to the user on the client's display is numeric
     */
    @Test
    public void appCodeIsNumeric(){
        ShowCodeConnAuth activity = Robolectric.buildActivity(ShowCodeConnAuth.class).create().start().get();
        String code = (String) ((TextView)activity.findViewById(R.id.textView2)).getText();
        boolean codeIsNumber = true;
        try {
            Integer.parseInt(code);
        } catch (NumberFormatException e) {
            codeIsNumber = false;
        }
        assertTrue(codeIsNumber);
    }
}
