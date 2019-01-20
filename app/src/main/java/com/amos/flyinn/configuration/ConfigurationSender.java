package com.amos.flyinn.configuration;

import android.content.Context;
import android.content.SharedPreferences;
import android.util.Log;
import android.widget.Toast;

import com.google.android.gms.nearby.Nearby;
import com.google.android.gms.nearby.connection.Payload;

import org.json.JSONException;
import org.json.JSONObject;

/**
 * Sends the configuration loaded from SharedPrefs to the server
 */
public class ConfigurationSender {
    private static final String TAG="ConfigurationSender";
    /**
     * Send config String through nearby to server
     */
    public ConfigurationSender(String endpoint, Context context) {
        String jsonPrefStr=GenerateConfigurationStr(context);

        // Send
        Payload bytesPayload = Payload.fromBytes(jsonPrefStr.getBytes());
        try {
            Nearby.getConnectionsClient(context).sendPayload(endpoint, bytesPayload);
        } catch(Exception ex) {
            Log.e(TAG, "Failed to transmit settings", ex);
            Toast.makeText(context, "Sending settings to server failed", Toast.LENGTH_SHORT).show();
        }
    }


    /**
     * Generates a JSON object of the preferences to use for communication with server
     * @return String from JSON object of preferences
     */
    private String GenerateConfigurationStr(Context context) {
        SharedPreferences prefs = context.getSharedPreferences("com.amos.flyinn", Context.MODE_PRIVATE);

        JSONObject prefJson = new JSONObject();
        String screenRatioKey = "com.amos.flyinn.screenratio";
        String proximityKey = "com.amos.flyinn.proximitysensor";

        try {
            prefJson.put("screenratio", prefs.getString(screenRatioKey, "Pad"));
            prefJson.put("proximitysensor", prefs.getBoolean(proximityKey, true));
        } catch (JSONException e) {
            e.printStackTrace();
        }
        return prefJson.toString();
    }

}
