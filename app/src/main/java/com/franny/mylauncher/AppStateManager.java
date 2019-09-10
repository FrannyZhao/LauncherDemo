package com.franny.mylauncher;

import android.content.Context;
import android.content.pm.LauncherActivityInfo;
import android.content.pm.LauncherApps;
import android.os.Build;
import android.os.Handler;
import android.os.HandlerThread;
import android.os.UserHandle;
import android.os.UserManager;
import android.util.Log;

import androidx.annotation.RequiresApi;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class AppStateManager {
    private static final String TAG = AppStateManager.class.getSimpleName();
    private static volatile AppStateManager instance = null;

    private LauncherApps mLauncherApps;
    private AppStateCallback mCallback;
    private Object mCallbackLock = new Object();
    private final HandlerThread mCallbackThread = new HandlerThread("callback");

    private AppStateListener appStateListener;

    public void setAppStateListener(AppStateListener appStateListener) {
        this.appStateListener = appStateListener;
    }

    private void notifyAppState() {
        if (appStateListener != null) {
            appStateListener.onAppsChanged();
        }
    }

    @RequiresApi(api = Build.VERSION_CODES.LOLLIPOP)
    private AppStateManager(Context context) {
        mCallbackThread.start();
        mCallback = new AppStateCallback();
        mLauncherApps = (LauncherApps) context.getSystemService(Context.LAUNCHER_APPS_SERVICE);
        synchronized (mCallbackLock) {
            if (mCallback != null) {
                mLauncherApps.unregisterCallback(mCallback);
            }
        }
        mLauncherApps.registerCallback(mCallback, new Handler(mCallbackThread.getLooper()));
    }

    @RequiresApi(api = Build.VERSION_CODES.LOLLIPOP)
    public List<LauncherActivityInfo> getAllApps(Context context) {
        List<LauncherActivityInfo> allApps = new ArrayList<>();
        UserManager mUserManager = (UserManager) context.getSystemService(Context.USER_SERVICE);
        for (UserHandle user : mUserManager.getUserProfiles()) {
            List<LauncherActivityInfo> lais = mLauncherApps.getActivityList(null, user);
            allApps.addAll(lais);
//            Log.i(TAG, "========================== user " + user + ", lais size " + lais.size());
//            for (LauncherActivityInfo lai : lais) {
//                Log.i(TAG, "lai " + lai.getName());
//            }
        }
        return allApps;
    }

    @RequiresApi(api = Build.VERSION_CODES.LOLLIPOP)
    public void unregister() {
        synchronized (mCallbackLock) {
            if (mCallback != null) {
                mLauncherApps.unregisterCallback(mCallback);
                mCallback = null;
            }
        }
    }

    @RequiresApi(api = Build.VERSION_CODES.LOLLIPOP)
    private class AppStateCallback extends LauncherApps.Callback {
        private final String TAG = AppStateCallback.class.getSimpleName();
        @Override
        public void onPackageRemoved(String packageName, UserHandle user) {
            Log.i(TAG, "onPackageRemoved packageName " + packageName + ", user " + user);
            notifyAppState();
        }

        @Override
        public void onPackageAdded(String packageName, UserHandle user) {
            Log.i(TAG, "onPackageAdded packageName " + packageName + ", user " + user);
            notifyAppState();
        }

        @Override
        public void onPackageChanged(String packageName, UserHandle user) {
            Log.i(TAG, "onPackageChanged packageName " + packageName + ", user " + user);
            notifyAppState();
        }

        @Override
        public void onPackagesAvailable(String[] packageNames, UserHandle user, boolean replacing) {
            Log.i(TAG, "onPackagesAvailable packageNames " + Arrays.toString(packageNames) + ", user " + user + ", replacing " + replacing);
        }

        @Override
        public void onPackagesUnavailable(String[] packageNames, UserHandle user, boolean replacing) {
            Log.i(TAG, "onPackagesUnavailable packageNames " + Arrays.toString(packageNames) + ", user " + user + ", replacing " + replacing);
        }
    }

    @RequiresApi(api = Build.VERSION_CODES.LOLLIPOP)
    public static AppStateManager getInstance(Context context) {
        if (instance == null) {
            synchronized (AppStateManager.class) {
                if (instance == null) {
                    instance = new AppStateManager(context);
                }
            }
        }
        return instance;
    }


}
