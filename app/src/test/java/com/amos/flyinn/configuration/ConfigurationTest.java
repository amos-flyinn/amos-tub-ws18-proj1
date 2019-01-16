package com.amos.flyinn.configuration;

import android.content.Context;
import android.content.SharedPreferences;
import android.widget.CheckBox;
import android.widget.Spinner;
import android.widget.TextView;

import com.amos.flyinn.R;
import com.amos.flyinn.ShowCodeConnAuth;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.robolectric.Robolectric;
import org.robolectric.RobolectricTestRunner;

import static org.junit.Assert.assertTrue;
/**
 * Tests the configuration interface
 */
@RunWith(RobolectricTestRunner.class)
public class ConfigurationTest {
    /**
     * Checks the loading and setting function of the ui
     */
    @Test
    public void interfaceIsSaved(){

        ConfigurationActivity activity = Robolectric.buildActivity(ConfigurationActivity.class).create().start().get();

        SharedPreferences prefs=activity.getSharedPreferences("com.amos.flyinn", Context.MODE_PRIVATE);

        ((CheckBox)activity.findViewById(R.id.checkBox)).setChecked(false);
        assertTrue(activity.getUseProximitySensor()==false);
        ((CheckBox)activity.findViewById(R.id.checkBox)).setChecked(true);
        assertTrue(activity.getUseProximitySensor()==true);

        ((Spinner)activity.findViewById(R.id.spinner1)).setSelection(0);
        assertTrue(activity.getSelectedScreenRatio()=="Pad");

        ((Spinner)activity.findViewById(R.id.spinner1)).setSelection(1);
        assertTrue(activity.getSelectedScreenRatio()=="Crop");

    }
}
