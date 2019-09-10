package com.franny.mylauncher;

import androidx.annotation.RequiresApi;
import androidx.constraintlayout.widget.ConstraintLayout;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.app.Activity;
import android.content.Context;
import android.content.pm.LauncherActivityInfo;
import android.content.pm.LauncherApps;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.os.HandlerThread;
import android.os.UserHandle;
import android.os.UserManager;
import android.util.Log;
import android.view.View;
import android.view.Window;
import android.widget.Button;
import android.widget.TextView;

import java.util.Arrays;
import java.util.List;

public class MainActivity extends Activity {
    private static final String TAG = MainActivity.class.getSimpleName();

    private TextView mAllAppsText;
    private Button mAllAppsBtn;
    private ConstraintLayout mWorkspace, mHome;
    private RecyclerView mAllAppsContainer;
    private AllAppsAdaper mAllAppsAdaper;

    private AppStateReceiver mAppStateReceiver;
    private AppStateManager mAppStateManager;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        requestWindowFeature(Window.FEATURE_NO_TITLE);
        setContentView(R.layout.activity_main);

        mAllAppsBtn = findViewById(R.id.all_apps_btn);
        mAllAppsBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                mHome.setVisibility(View.GONE);
                mWorkspace.setVisibility(View.VISIBLE);
            }
        });
        mAllAppsText = findViewById(R.id.launcher_txt);
        mWorkspace = findViewById(R.id.workspace);
        mWorkspace.setVisibility(View.GONE);
        mHome = findViewById(R.id.home);
        mHome.setVisibility(View.VISIBLE);
        mAllAppsContainer = findViewById(R.id.all_apps_container);
        mAllAppsContainer.setLayoutManager(new GridLayoutManager(this, 3));
        mAllAppsAdaper = new AllAppsAdaper(this, mAllAppsContainer);

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            mAppStateManager = AppStateManager.getInstance(this);
            mAllAppsAdaper.setData(mAppStateManager.getAllApps(MainActivity.this));
            mAppStateManager.setAppStateListener(new AppStateListener() {
                @Override
                public void onAppsChanged() {
                    mAllAppsAdaper.refresh(mAppStateManager.getAllApps(MainActivity.this));
                }
            });
        } else {
            mAppStateReceiver = AppStateReceiver.getInstance(this);
            mAllAppsAdaper.setData(mAppStateReceiver.getAllApps(MainActivity.this));
            mAppStateReceiver.setAppStateListener(new AppStateListener() {
                @Override
                public void onAppsChanged() {
                    mAllAppsAdaper.refresh(mAppStateReceiver.getAllApps(MainActivity.this));
                }
            });
        }
        mAllAppsContainer.setAdapter(mAllAppsAdaper);
    }

    @Override
    public void onBackPressed() {
        if (mHome.getVisibility() == View.GONE) {
            mHome.setVisibility(View.VISIBLE);
            mWorkspace.setVisibility(View.GONE);
        } else {
            super.onBackPressed();
        }
    }



    @Override
    protected void onDestroy() {
        super.onDestroy();
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            mAppStateManager.unregister();
        } else {
            unregisterReceiver(mAppStateReceiver);
        }
    }
}
