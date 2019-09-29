package com.franny.mylauncher;

import android.content.Context;
import android.content.Intent;
import android.content.pm.LauncherActivityInfo;
import android.content.pm.PackageManager;
import android.content.pm.ResolveInfo;
import android.graphics.drawable.Drawable;
import android.os.Build;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.Animation;
import android.view.animation.RotateAnimation;
import android.widget.ImageView;
import android.widget.TextView;


import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import java.util.List;

public class AllAppsAdaper extends RecyclerView.Adapter<AllAppsAdaper.AllAppsViewHolder> {
    private List<ResolveInfo> mAppsLowVersion;
    private List<LauncherActivityInfo> mAppsHighVersion;
    private LauncherActivity mLauncher;
    private RecyclerView mRecyclerView;
    private PackageManager mPackageManager;
    private Animation mRotateAnim;
    private AppPopMenu mAppPopMenu;

    public AllAppsAdaper(LauncherActivity launcher, RecyclerView recyclerView) {
        mLauncher = launcher;
        mRecyclerView = recyclerView;
        mPackageManager = mLauncher.getPackageManager();
        mRotateAnim = new RotateAnimation(-5, 5, Animation.RELATIVE_TO_SELF, 0.5f, Animation.RELATIVE_TO_SELF, 0.5f);
        mRotateAnim.setDuration(50);
        mRotateAnim.setRepeatMode(Animation.REVERSE);
        mRotateAnim.setRepeatCount(5);
        mAppPopMenu = AppPopMenu.getInstance(mLauncher.getFragmentManager());
    }

    public void setData(List apps) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            mAppsHighVersion = apps;
        } else {
            mAppsLowVersion = apps;
        }
    }

    public void refresh(List newApps) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            mAppsHighVersion.clear();
            mAppsHighVersion.addAll(newApps);
        } else {
            mAppsLowVersion.clear();
            mAppsLowVersion.addAll(newApps);
        }
        mRecyclerView.post(new Runnable() {
            @Override
            public void run() {
                notifyDataSetChanged();
            }
        });
    }

    @NonNull
    @Override
    public AllAppsAdaper.AllAppsViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.app_grid_layout, null);
        return new AllAppsViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull final AllAppsAdaper.AllAppsViewHolder holder, int position) {
        final String currentAppName, packageName;
        final Drawable appIcon;
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            final LauncherActivityInfo launcherActivityInfo = mAppsHighVersion.get(position);
            currentAppName = launcherActivityInfo.getLabel().toString();
            packageName = launcherActivityInfo.getApplicationInfo().packageName;
            appIcon = launcherActivityInfo.getIcon(480);
            holder.appName.setText(currentAppName);
            holder.appIcon.setImageDrawable(appIcon);
            holder.rootView.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    mLauncher.startActivity(mPackageManager.getLaunchIntentForPackage(launcherActivityInfo.getApplicationInfo().packageName));
                }
            });
        } else {
            final ResolveInfo resolveInfo = mAppsLowVersion.get(position);
            currentAppName = resolveInfo.activityInfo.applicationInfo.loadLabel(mPackageManager).toString();
            packageName = resolveInfo.activityInfo.packageName;
            appIcon = resolveInfo.activityInfo.applicationInfo.loadIcon(mPackageManager);
            holder.appName.setText(currentAppName);
            holder.appIcon.setImageDrawable(appIcon);
            holder.rootView.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    mLauncher.startActivity(mPackageManager.getLaunchIntentForPackage(resolveInfo.activityInfo.packageName));
                }
            });
        }
        holder.rootView.setOnLongClickListener(new View.OnLongClickListener() {
            @Override
            public boolean onLongClick(View v) {
                holder.rootView.startAnimation(mRotateAnim);
                mAppPopMenu.setAppName(currentAppName);
                mAppPopMenu.setAppIcon(appIcon);
                mAppPopMenu.setPackageName(packageName);
                mAppPopMenu.show(mLauncher.getFragmentManager(), "");
                return true;
            }
        });
    }

    @Override
    public int getItemCount() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            if (mAppsHighVersion == null) {
                return 0;
            } else {
                return mAppsHighVersion.size();
            }
        } else {
            if (mAppsLowVersion == null) {
                return 0;
            } else {
                return mAppsLowVersion.size();
            }
        }
    }

    public class AllAppsViewHolder extends RecyclerView.ViewHolder {
        View rootView;
        ImageView appIcon;
        TextView appName;
        public AllAppsViewHolder(@NonNull View itemView) {
            super(itemView);
            rootView = itemView;
            appIcon = rootView.findViewById(R.id.app_icon);
            appName = rootView.findViewById(R.id.app_name);
        }
    }
}