package com.amos.flyinn.configuration;

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

import com.amos.flyinn.R;
import com.google.android.gms.nearby.Nearby;
import com.google.android.gms.nearby.connection.ConnectionsClient;
import com.google.android.gms.nearby.connection.Payload;

import org.json.JSONException;
import org.json.JSONObject;

import java.util.List;

/**
 * Interface for configuration
 */
public class ConfigurationActivity extends AppCompatActivity {
    private SharedPreferences prefs;
    private String endPoint;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_configuration);

        prefs = this.getSharedPreferences("com.amos.flyinn", Context.MODE_PRIVATE);

        // Set endpoint ID
        if(getIntent()!=null && getIntent().getExtras()!=null) {
            endPoint = getIntent().getExtras().getString("endPoint");
        }

        Init();
    }

    /**
     * Setup ui and load/apply settings from configuration
     */
    private void Init() {
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


        if(endPoint!=null) new ConfigurationSender(endPoint, this);
    }

    private void setSelectedScreenRatio(String screenRatio) {
        String screenRatioKey = "com.amos.flyinn.screenratio";
        prefs.edit().putString(screenRatioKey, screenRatio).apply();
    }


    private void setUseProximitySensor(boolean useProximitySensor) {
        String proximityKey = "com.amos.flyinn.proximitysensor";
        prefs.edit().putBoolean(proximityKey, useProximitySensor).apply();
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


    @Override
    protected void onPause() {
        super.onPause();
        if(endPoint!=null) {
            new ConfigurationSender(endPoint, this);
        }
    }
}
