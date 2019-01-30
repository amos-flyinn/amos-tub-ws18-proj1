package com.amos.flyinn.settingsCheck;

import android.content.Context;
import android.provider.Settings;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.robolectric.RobolectricTestRunner;

import java.util.HashSet;

import androidx.test.core.app.ApplicationProvider;

import static org.junit.Assert.assertTrue;

/**
 * Tests the settingsChecker
 */
@RunWith(RobolectricTestRunner.class)
public class SettingsCheckerTest {
    private Context context = ApplicationProvider.getApplicationContext();

    /**
     * Enable and disable developer settings and test the expected found missing settings
     */
    @Test
    public void getMissingSettingsDeveloper() {
        SettingsChecker settingsCheck=new SettingsChecker(context);
        HashSet<SettingsChecker.SettingsType> settings;

        Settings.Secure.putInt(context.getContentResolver(), "development_settings_enabled", 0);
        settings = settingsCheck.GetMissingSettings();
        assertTrue(settings.contains(SettingsChecker.SettingsType.Developer));

        Settings.Secure.putInt(context.getContentResolver(), "development_settings_enabled", 1);
        settings = settingsCheck.GetMissingSettings();
        assertTrue(settings.contains(SettingsChecker.SettingsType.Developer)==false);

    }

    /**
     * Enable and disable adb settings
     */
    @Test
    public void getMissingSettingsADB() {
        SettingsChecker settingsCheck=new SettingsChecker(context);
        HashSet<SettingsChecker.SettingsType> settings;

        Settings.Secure.putInt(context.getContentResolver(), "adb_enabled", 0);
        settings = settingsCheck.GetMissingSettings();
        assertTrue(settings.contains(SettingsChecker.SettingsType.ADBperUSB));

        Settings.Secure.putInt(context.getContentResolver(), "adb_enabled", 1);
        settings = settingsCheck.GetMissingSettings();
        assertTrue(settings.contains(SettingsChecker.SettingsType.ADBperUSB)==false);
    }
}