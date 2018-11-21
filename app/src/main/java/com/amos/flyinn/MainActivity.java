package com.amos.flyinn;
 
import android.content.Intent;
import android.os.StrictMode;
import android.provider.Settings;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.widget.TextView;
import android.widget.Toast;
 
import org.json.JSONException;
 
import com.amos.flyinn.screenRecording.RecordingActivity;
 
public class MainActivity extends AppCompatActivity {
 
    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        MenuInflater inflater = getMenuInflater();
        inflater.inflate(R.menu.main_menu, menu);
 
        return true;
    }
 
    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        Intent intent;
        switch (item.getItemId()) {
            case R.id.wifip2p_activity:
                intent = new Intent(this,WifiP2PActivity.class);
                break;
            case R.id.adb_activity:
                intent = new Intent(this,ADBActivity.class);
                break;
            case R.id.webrtc_activity:
                intent = new Intent(this, WebRTCActivity.class);
                break;
            default:
                return super.onOptionsItemSelected(item);
        }
        startActivity(intent);
        return super.onOptionsItemSelected(item);
    }
 
    // Used to load the 'native-lib' library on application startup.
    static {
        System.loadLibrary("native-lib");
    }
 
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
 
        // Example of a call to a native method
        TextView tv = (TextView) findViewById(R.id.sample_text);
        tv.setText(stringFromJNI());
    }
 
 
 
    /**
     * A native method that is implemented by the 'native-lib' native library,
     * which is packaged with this application.
     */
    public native String stringFromJNI();
 
    public void toScreenActivityOnClick(View view) {
        Intent intent = new Intent(this, RecordingActivity.class);
        startActivity(intent);
    }
 
    public void toRequirementsActivityOnClick(View view) {
        Intent intent = new Intent(this, RequirementsCheckAvtivity.class);
        startActivity(intent);
    }
 
    @Override
    public void onResume()
    {
        super.onResume();
        checkForDebuggingMode();
    }
 
     private void checkForDebuggingMode() {
        if(Settings.Secure.getInt(this.getContentResolver(), Settings.Global.ADB_ENABLED, 0) != 1) {
            try {
                startActivity(new Intent(android.provider.Settings.ACTION_APPLICATION_DEVELOPMENT_SETTINGS));
                Toast.makeText(this, "To use FlyInn please enable USB debugging.", Toast.LENGTH_SHORT).show();
            } catch(Exception ex) {
                Toast.makeText(this, "To use FlyInn please enable first the debugging mode and then the USB debugging.\n" +
                        "Mostly it works with tapping multiple times the 'Software Build number' label.", Toast.LENGTH_SHORT).show();
                startActivity(new Intent(Settings.ACTION_DEVICE_INFO_SETTINGS));
            }
        }
    }
}
