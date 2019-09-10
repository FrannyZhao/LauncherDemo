package com.franny.mylauncher;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.pm.PackageManager;
import android.content.pm.ResolveInfo;
import android.util.Log;

import java.util.List;

public class AppStateReceiver extends BroadcastReceiver {
    private static final String TAG = AppStateReceiver.class.getSimpleName();
    private static volatile AppStateReceiver instance = null;
    private AppStateListener appStateListener;

    public void setAppStateListener(AppStateListener appStateListener) {
        this.appStateListener = appStateListener;
    }

    private void notifyAppState() {
        if (appStateListener != null) {
            appStateListener.onAppsChanged();
        }
    }

    private AppStateReceiver(Context context) {
        register(context);
    }

    public static AppStateReceiver getInstance(Context context) {
        if (instance == null) {
            synchronized (AppStateReceiver.class) {
                if (instance == null) {
                    instance = new AppStateReceiver(context);
                }
            }
        }
        return instance;
    }

    private void register(Context context) {
        IntentFilter filter = new IntentFilter(Intent.ACTION_PACKAGE_ADDED);
        filter.addAction(Intent.ACTION_PACKAGE_REMOVED);
        filter.addAction(Intent.ACTION_PACKAGE_CHANGED);
        filter.addDataScheme("package");
        context.registerReceiver(this, filter);
    }

    public List<ResolveInfo> getAllApps(Context context) {
        PackageManager pm = context.getPackageManager();
        Intent launchable = new Intent(Intent.ACTION_MAIN);
        launchable.addCategory(Intent.CATEGORY_LAUNCHER);
        final List<ResolveInfo> allApps = pm.queryIntentActivities(launchable, 0);
        Log.i(TAG, "allApps size " + allApps.size());
//        for (ResolveInfo app : allApps) {
//            Log.i(TAG, "app " + allApps.activityInfo.name);
//        }
        return allApps;
    }

    @Override
    public void onReceive(Context context, Intent intent) {
        String action = intent.getAction();
        Log.i(TAG, "receive action " + action);
        String packageName = intent.getData().getSchemeSpecificPart();
        boolean replacing = intent.getBooleanExtra(Intent.EXTRA_REPLACING, false);
        if (packageName == null || packageName.length() == 0) {
            return;
        }
        int op = PackageUpdatedTask.OP_NONE;
        if (Intent.ACTION_PACKAGE_CHANGED.equals(action)) {
            op = PackageUpdatedTask.OP_UPDATE;
        } else if (Intent.ACTION_PACKAGE_REMOVED.equals(action)) {
            if (!replacing) {
                op = PackageUpdatedTask.OP_REMOVE;
            }
            // else, we are replacing the package, so a PACKAGE_ADDED will be sent
            // later, we will update the package at this time
        } else if (Intent.ACTION_PACKAGE_ADDED.equals(action)) {
            if (!replacing) {
                op = PackageUpdatedTask.OP_ADD;
            } else {
                op = PackageUpdatedTask.OP_UPDATE;
            }
        }
        Log.i(TAG, "op " + op);
        if (op != PackageUpdatedTask.OP_NONE) {
            notifyAppState();
        }
    }

    private class PackageUpdatedTask {
        int mOp;
        public static final int OP_NONE = 0;
        public static final int OP_ADD = 1;
        public static final int OP_UPDATE = 2;
        public static final int OP_REMOVE = 3; // uninstlled
        public static final int OP_UNAVAILABLE = 4; // external media unmounted
    }
}
