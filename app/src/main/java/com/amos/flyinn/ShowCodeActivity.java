package com.amos.flyinn;

import android.Manifest;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Point;
import android.os.Build;
import android.os.Bundle;
import android.support.annotation.CallSuper;
import android.support.annotation.NonNull;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.widget.TextView;
import android.widget.Toast;

import com.amos.flyinn.configuration.ConfigurationActivity;
import com.amos.flyinn.nearbyservice.NearbyService;
import com.amos.flyinn.summoner.Daemon;

import java.util.concurrent.ThreadLocalRandom;

/**
 * Initial activity showing code used for connection from remote display.
 */
public class ShowCodeActivity extends AppCompatActivity {
    private String nameNum = "";
    private TextView display;
    private Toast mToast;

    private static final String TAG = "showCode";

    private static final int REQUEST_CODE_REQUIRED_PERMISSIONS = 1;

    /**
     * Set state and information in android service.
     */
    private void setService() {
        Intent intent = NearbyService.createNearbyIntent(NearbyService.ACTION_START, this);
        intent.putExtra("code", nameNum);
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            startForegroundService(intent);
        } else {
            startService(intent);
        }
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_show_code);
        Thread.setDefaultUncaughtExceptionHandler(new ExceptionHandler(this));


        display = findViewById(R.id.textView2);
        if (!hasPermissions(NearbyService.getRequiredPermissions()) && Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            requestPermissions(NearbyService.getRequiredPermissions(), REQUEST_CODE_REQUIRED_PERMISSIONS);
        } else {
            Log.w(TAG, "Could not check permissions due to version");
        }

        String[] perms = new String[]{Manifest.permission.WRITE_EXTERNAL_STORAGE, Manifest.permission.READ_EXTERNAL_STORAGE};
        if (!hasPermissions(perms) && Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            requestPermissions(perms, REQUEST_CODE_REQUIRED_PERMISSIONS);
        }

        try {
            createADBService();
        } catch (Exception e) {
            Log.d("ShowCodeActivity", "Failed to start ADB service");
            e.printStackTrace();
        }
        nameNum = String.valueOf(ThreadLocalRandom.current().nextInt(1000, 9998 + 1));
        display.setText(nameNum);
        setService();
    }

    protected Daemon createADBService() throws Exception {
        Point p = new Point();
        getWindowManager().getDefaultDisplay().getRealSize(p);
        Daemon d = new Daemon(getApplicationContext(), p);
        d.writeFakeInputToFilesystem();
        Log.d("ShowCodeActivity", "Wrote to FS");
        Log.d("ShowCodeActivity", "Going to spawn ADB service");
        d.spawn_adb();
        Log.d("ShowCodeActivity", "Spawned ADB service");
        return d;
    }

    @Override
    public void onResume() {
        super.onResume();
    }


    @Override
    protected void onDestroy() {
        Intent intent = NearbyService.createNearbyIntent("", this);
        stopService(intent);
        super.onDestroy();
    }

    /**
     * Handles user acceptance (or denial) of our permission request.
     *
     * @param requestCode  The request code passed in requestPermissions()
     * @param permissions  Permissions that must be granted to run nearby connections
     * @param grantResults Results of granting permissions
     */
    @CallSuper
    @Override
    public void onRequestPermissionsResult(
            int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);

        if (requestCode != REQUEST_CODE_REQUIRED_PERMISSIONS) {
            return;
        }

        for (int grantResult : grantResults) {
            if (grantResult == PackageManager.PERMISSION_DENIED) {
                Log.w("flyinn.ShowCode", "Permissions necessary for connections were not granted.");
                mToast.setText(R.string.nearby_missing_permissions);
                mToast.show();
                finish();
            }
        }
        recreate();
    }

    /**
     * Determines whether the FlyInn server app has the necessary permissions to run nearby.
     *
     * @return True if the app was granted all the permissions, false otherwise
     */
    public boolean hasPermissions(String[] permissions) {
        for (String permission : permissions) {
            if (ContextCompat.checkSelfPermission(this, permission)
                    != PackageManager.PERMISSION_GRANTED) {
                return false;
            }
        }
        return true;
    }


    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        MenuInflater inflater = getMenuInflater();
        inflater.inflate(R.menu.options_menu, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case R.id.btn_settings:
                startActivity(new Intent(this, ConfigurationActivity.class));
                break;

            default:
                Log.e(TAG, "unimplemented option selected");
                return false;
        }

        return true;
    }
}
