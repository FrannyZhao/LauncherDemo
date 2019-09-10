package com.franny.mylauncher;

import android.content.Context;
import android.content.pm.LauncherActivityInfo;
import android.content.pm.PackageManager;
import android.content.pm.ResolveInfo;
import android.os.Build;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import java.util.List;

public class AllAppsAdaper extends RecyclerView.Adapter<AllAppsAdaper.AllAppsViewHolder> {
    private List<ResolveInfo> mAppsLowVersion;
    private List<LauncherActivityInfo> mAppsHighVersion;
    private Context mContext;
    private RecyclerView mRecyclerView;
    private PackageManager mPackageManager;

    public AllAppsAdaper(Context context, RecyclerView recyclerView) {
        mContext = context;
        mRecyclerView = recyclerView;
        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.LOLLIPOP) {
            mPackageManager = context.getPackageManager();
        }
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
    public void onBindViewHolder(@NonNull AllAppsAdaper.AllAppsViewHolder holder, int position) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            LauncherActivityInfo launcherActivityInfo = mAppsHighVersion.get(position);
            holder.appName.setText(launcherActivityInfo.getLabel().toString());
            holder.appIcon.setImageDrawable(launcherActivityInfo.getIcon(160));
        } else {
            ResolveInfo resolveInfo = mAppsLowVersion.get(position);
            holder.appName.setText(resolveInfo.activityInfo.applicationInfo.loadLabel(mPackageManager));
            holder.appIcon.setImageDrawable(resolveInfo.activityInfo.applicationInfo.loadIcon(mPackageManager));
        }
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