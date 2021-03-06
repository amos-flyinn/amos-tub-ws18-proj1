package com.amos.flyinn;

import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.widget.TextView;
import com.amos.flyinn.buildinfo.BuildInfo;

public class BuildInfoActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_build_info);
        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        ((TextView)findViewById(R.id.buildinfo_git_tag_name)).setText(BuildInfo.GIT_TAG_NAME);
        ((TextView)findViewById(R.id.buildinfo_git_commit_date)).setText(BuildInfo.GIT_COMMIT_DATE);
        ((TextView)findViewById(R.id.buildinfo_build_date)).setText(BuildInfo.GIT_BUILD_DATE);
    }

}
