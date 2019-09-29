package com.franny.mylauncher;

import android.app.Activity;
import android.content.Context;
import android.content.pm.IPackageDeleteObserver;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.os.RemoteException;
import android.util.Log;
import android.widget.Toast;

import com.jaredrummler.android.shell.Shell;

import java.lang.reflect.Method;

public class AppManager {
    private static final String TAG = AppManager.class.getSimpleName();
    private static volatile AppManager instance = null;
    private static final int PackageManager_DELETE_SUCCEEDED = 1;

    private AppManager() {

    }

    public static AppManager getInstance() {
        if (instance == null) {
            synchronized (AppManager.class) {
                if (instance == null) {
                    instance = new AppManager();
                }
            }
        }
        return instance;
    }

    public void uninstallPackage(final Activity activity, PackageManager packageManager, final String packageName) {
        PackageInfo pkgInfo = null;
        try {
            pkgInfo = packageManager.getPackageInfo(packageName, PackageManager.GET_CONFIGURATIONS);
        } catch (PackageManager.NameNotFoundException e) {
            e.printStackTrace();
        }
        if (pkgInfo != null) {
            try {
                Method method = PackageManager.class.getDeclaredMethod("deletePackage",
                        String.class, IPackageDeleteObserver.class, int.class);
                method.setAccessible(true);
                method.invoke(instance, packageName, new IPackageDeleteObserver.Stub() {
                    @Override
                    public void packageDeleted(String s, int returnCode) {
                        if (returnCode == PackageManager_DELETE_SUCCEEDED) {
                            Log.i(TAG, "uninstall " + packageName + " success");
                            activity.runOnUiThread(new Runnable() {
                                @Override
                                public void run() {
                                    Toast.makeText(activity, R.string.app_uninstall_finish, Toast.LENGTH_SHORT).show();
                                }
                            });
                        } else {
                            Toast.makeText(activity, R.string.app_uninstall_fail, Toast.LENGTH_SHORT).show();
                            Log.i(TAG, "uninstall " + packageName + " fail");
                        }
                    }
                }, 0);
            } catch (Throwable e) {
                Toast.makeText(activity, R.string.app_uninstall_fail, Toast.LENGTH_SHORT).show();
                e.printStackTrace();
            }
        }
    }

    public void stopApplication(String packageName) {
        Shell.run(String.format("am force-stop %s", packageName));
    }
}
