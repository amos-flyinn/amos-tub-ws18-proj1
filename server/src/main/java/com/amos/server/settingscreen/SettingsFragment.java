package com.amos.server.settingscreen;

import android.os.Bundle;
import android.support.v7.preference.Preference;
import android.support.v7.preference.PreferenceFragmentCompat;

import com.amos.server.R;

public class SettingsFragment extends PreferenceFragmentCompat {
    @Override
    public void onCreatePreferences(Bundle bundle, String s) {
        setPreferencesFromResource(R.xml.settings, s);

    }

    /*
    here you can set listeners for the preferences, just add
    a key to the preference in settings.xml file.
     */
    @Override
    public boolean onPreferenceTreeClick(Preference preference) {
        String key = preference.getKey();

        return super.onPreferenceTreeClick(preference);

    }
}
