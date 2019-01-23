package com.amos.flyinn.errorScreen;

import android.content.Intent;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;

import com.amos.flyinn.R;

public class ErrorActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_error);
        //print the error if existed.
        try {
            Log.i("error", "onCreate: " + getIntent().getExtras().getString("error_message"));
        } catch (Exception e) {

        }
    }

    /**
     * on button click close app
     * @param view
     */
    public void closeApp(View view) {
        this.finishAffinity();
    }

    /**
     * restart the app on button click
     * @param view
     */
    public void restartApp(View view) {
        Intent i = getBaseContext().getPackageManager()
                .getLaunchIntentForPackage( getBaseContext().getPackageName() );
        i.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
        startActivity(i);
    }
}
