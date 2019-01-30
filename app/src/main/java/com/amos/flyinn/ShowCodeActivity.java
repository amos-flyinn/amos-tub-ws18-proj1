package com.amos.flyinn;

import android.Manifest;
import android.app.NotificationManager;
import android.content.BroadcastReceiver;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.ServiceConnection;
import android.content.pm.PackageManager;
import android.graphics.Point;
import android.os.Build;
import android.os.Bundle;
import android.os.IBinder;
import android.support.annotation.CallSuper;
import android.support.annotation.NonNull;
import android.support.v4.content.ContextCompat;
import android.support.v4.content.LocalBroadcastManager;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.TextView;
import android.widget.Toast;

import com.amos.flyinn.nearbyservice.NearbyService;
import com.amos.flyinn.summoner.Daemon;

import java.util.ArrayList;
import java.util.Arrays;
import java.security.SecureRandom;
import java.util.Locale;

/**
 * Initial activity showing code used for connection from remote display.
 */
public class ShowCodeActivity extends AppCompatActivity {

    private String nameNum = "";

    private static final String TAG = "showCode";
    private static final int REQUEST_CODE_REQUIRED_PERMISSIONS = 1;


    public static final String[] STORAGE_PERMISSIONS = new String[]{
            Manifest.permission.WRITE_EXTERNAL_STORAGE,
            Manifest.permission.READ_EXTERNAL_STORAGE};

    private BroadcastReceiver msgReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            if (intent.getExtras() != null) {
                if (intent.getBooleanExtra("com.amos.flyinn.exit", false)) {
                    Log.i(TAG, "Received exit message via BroadcastReceiver.");
                    closeApp();
                } else if (intent.getBooleanExtra("com.amos.flyinn.restart", false)) {
                    Log.i(TAG, "Received restart message via BroadcastReceiver.");
                    restartApp();
                }
            }
        }
    };

    private ServiceConnection myConnection = new ServiceConnection() {
        public void onServiceConnected (ComponentName className, IBinder binder) {
            ((KillNotificationService.KillBinder) binder).service.startService(
                    new Intent(ShowCodeActivity.this, KillNotificationService.class));
        }
        public void onServiceDisconnected(ComponentName className) {
            // empty method
        }
    };

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

        LocalBroadcastManager.getInstance(this).registerReceiver(msgReceiver,
                new IntentFilter("msg-flyinn"));
        bindService(new Intent(ShowCodeActivity.this,
                KillNotificationService.class), myConnection, Context.BIND_AUTO_CREATE);

        setContentView(R.layout.activity_show_code);
        Thread.setDefaultUncaughtExceptionHandler(new ExceptionHandler(this));
        TextView display = findViewById(R.id.textView2);


        // create app code between 0 and 9999; secureRandom is non-deterministic
        nameNum = String.format(Locale.ROOT, "%04d", new SecureRandom().nextInt(9999 + 1));

        Log.i(TAG, "App code is set to " + nameNum);
        display.setText(nameNum);

        validatePermissions();
    }

    /**
     * Checks if all permissions are given, requests them and start service if yes
     */
    protected void validatePermissions() {
        // Create permission list
        ArrayList<String> allPermissions=new ArrayList<String>();
        allPermissions.addAll(Arrays.asList(NearbyService.getRequiredPermissions()));
        allPermissions.add(Manifest.permission.WRITE_EXTERNAL_STORAGE);
        allPermissions.add(Manifest.permission.READ_EXTERNAL_STORAGE);

        String[] allPermissionsArr = new String[allPermissions.size()];
        allPermissionsArr = allPermissions.toArray(allPermissionsArr);

        // Request missing permissions
        if(hasPermissions(allPermissionsArr)) {
            startServices();
        }
        else if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            requestPermissions(allPermissionsArr, REQUEST_CODE_REQUIRED_PERMISSIONS);
        } else {
            CloseNoPermissions();
        }

    }

    /**
     * Starts adb and set nearby service
     */
    protected void startServices() {
        try {
            createADBService();
        } catch (Exception e) {
            Log.d("ShowCodeActivity", "Failed to start ADB service");
            e.printStackTrace();
        }

        setService();
    }

    /**
     * Close app because of no permissions
     * @throws Exception
     */
    protected void CloseNoPermissions() {
        Log.w(TAG, "Permissions necessary for connections were not granted.");
        Toast.makeText(this, R.string.nearby_missing_permissions, Toast.LENGTH_LONG).show();
        closeApp();
    }

    protected void createADBService() throws Exception {
        Point p = new Point();
        getWindowManager().getDefaultDisplay().getRealSize(p);
        Daemon d = new Daemon(getApplicationContext(), p);
        d.writeFakeInputToFilesystem();
        Log.d("ShowCodeActivity", "Wrote to FS");
        Log.d("ShowCodeActivity", "Going to spawn ADB service");
        d.spawn_adb();
        Log.d("ShowCodeActivity", "Spawned ADB service");
    }

    @Override
    public void onResume() {
        super.onResume();
    }


    @Override
    protected void onDestroy() {
        Intent intent = NearbyService.createNearbyIntent("", this);
        stopService(intent);

        try {
            LocalBroadcastManager.getInstance(this).unregisterReceiver(msgReceiver);
        } catch (Exception e) {
            Log.d(TAG, e.toString());
            Log.i(TAG, "Receiver was already unregistered.");
        }

        try {
            unbindService(myConnection);
        } catch (Exception e) {
            Log.d(TAG, e.toString());
            Log.i(TAG, "KillNotificationService was already unbound.");
        }

        ((NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE)).cancelAll();
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
                CloseNoPermissions();

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
        getMenuInflater();
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
//            case R.id.btn_settings:
//                startActivity(new Intent(this, ConfigurationActivity.class));
//                break;

            default:
                Log.e(TAG, "unimplemented option selected");
                return false;
        }

//        return true;
    }

    /**
     * Closes the app (kills all activities)
     */
    public void closeApp() {
        Log.d(TAG, "Closing app via closeApp function.");
        ((NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE)).cancelAll();

        this.finishAffinity();
        finishAndRemoveTask();
    }

    /**
     * Finishes all activities and then restarts the app
     */
    public void restartApp() {
        Log.d(TAG, "Restarting app via restartApp function.");
        Intent i = getBaseContext().getPackageManager()
                .getLaunchIntentForPackage( getBaseContext().getPackageName() );
        if (i != null) {
            i.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
        }
        startActivity(i);
    }
}
