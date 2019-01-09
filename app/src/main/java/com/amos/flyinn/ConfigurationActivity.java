package com.amos.flyinn;

import android.annotation.TargetApi;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.res.Configuration;
import android.media.Ringtone;
import android.media.RingtoneManager;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.preference.ListPreference;
import android.preference.Preference;
import android.preference.PreferenceActivity;
import android.app.ActionBar;
import android.preference.PreferenceFragment;
import android.preference.PreferenceManager;
import android.preference.RingtonePreference;
import android.support.v7.app.AppCompatActivity;
import android.text.TextUtils;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.Spinner;
import android.widget.Switch;

import org.json.JSONException;
import org.json.JSONObject;

import java.util.List;

/**
 * Interface for configuration
 */
public class ConfigurationActivity extends AppCompatActivity {
    SharedPreferences prefs;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_configuration);

        prefs = this.getSharedPreferences("com.amos.flyinn", Context.MODE_PRIVATE);

        // Init ui
        Spinner dropdown = findViewById(R.id.spinner1);
        String[] items = new String[]{"Pad", "Crop", "Stretch"};
        ArrayAdapter<String> adapter = new ArrayAdapter<>(this, android.R.layout.simple_spinner_dropdown_item, items);
        dropdown.setAdapter(adapter);
        dropdown.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parentView, View selectedItemView, int position, long id) {
                setSelectedScreenRatio(dropdown.getSelectedItem().toString());
            }

            @Override
            public void onNothingSelected(AdapterView<?> parentView) {
            }
        });

        CheckBox proxCheckbox=findViewById(R.id.checkBox);
        proxCheckbox.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                setUseProximitySensor(isChecked);
            }
        });


        // Set status from prefs
        String srPrefs=getSelectedScreenRatio();
        if (srPrefs != null) {
            int spinnerPosition = adapter.getPosition(srPrefs);
            dropdown.setSelection(spinnerPosition);
        }

        boolean puPrefs=getUseProximitySensor();
        proxCheckbox.setChecked(puPrefs);


        SubmitConfiguration();
    }


    private void setSelectedScreenRatio(String screenRatio) {
        String screenRatioKey = "com.amos.flyinn.screenratio";
        prefs.edit().putString(screenRatioKey, screenRatio).apply();
        SubmitConfiguration();
    }


    private void setUseProximitySensor(boolean useProximitySensor) {
        String proximityKey = "com.amos.flyinn.proximitysensor";
        prefs.edit().putBoolean(proximityKey, useProximitySensor).apply();
        SubmitConfiguration();
    }

    /**
     * Loads screenratio settings from prefs or return 'Pad'
     * @return screenratio setting
     */
    public String getSelectedScreenRatio() {
        String screenRatioKey = "com.amos.flyinn.screenratio";
        return prefs.getString(screenRatioKey, "Pad");
    }

    /**
     * Loads proximitysensor settings from prefs or return false
     * @return proximitysensor setting
     */
    public boolean getUseProximitySensor() {
        String proximityKey = "com.amos.flyinn.proximitysensor";
        return prefs.getBoolean(proximityKey, true);
    }


    /**
     * Generates a JSON object of the preferences to use for communication with server
     * @return String from JSON object of preferences
     */
    private String GenerateConfigurationStr() {
        JSONObject prefJson = new JSONObject();
        try {
            prefJson.put("screenratio", getSelectedScreenRatio());
            prefJson.put("proximitysensor", getUseProximitySensor());
        } catch (JSONException e) {
            e.printStackTrace();
        }
        return prefJson.toString();
    }

    public void SubmitConfiguration() {
        String jsonPrefStr=GenerateConfigurationStr();
        /*
        Todo: Transfer String to server
         */
    }
}
