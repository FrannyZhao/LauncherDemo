package com.franny.mylauncher;

import android.annotation.SuppressLint;
import android.app.DialogFragment;
import android.content.Context;
import android.content.DialogInterface;
import android.content.pm.PackageManager;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.graphics.drawable.Drawable;
import android.os.Bundle;
import android.os.PowerManager;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.view.WindowManager;
import android.widget.ImageView;
import android.widget.TextView;

public class ConfirmDialog extends DialogFragment implements View.OnClickListener {
    private static volatile ConfirmDialog instance = null;
    private int mType = -1;
    public static final int TYPE_UNINSTALL = 1;
    public static final int TYPE_STOP = 2;

    private View mRootView;
    private String mAppName, mPackageName, mHint, mRotation;
    private Drawable mAppIcon;
    private PackageManager mPackageManager;

    public static ConfirmDialog getInstance() {
        if (instance == null) {
            synchronized (ConfirmDialog.class) {
                if (instance == null) {
                    instance = new ConfirmDialog();
                }
            }
        }
        return instance;
    }

    @SuppressLint("ValidFragment")
    private ConfirmDialog() {

    }

    public void setType(int type) {
        this.mType = type;
    }

    public void setRotation(String rotation) {
        this.mRotation = rotation;
    }

    public void setAppName(String name) {
        mAppName = name;
    }

    public void setHint(String hint) {
        mHint = hint;
    }

    public void setAppIcon(Drawable icon) {
        mAppIcon = icon;
    }

    public void setPackageName(String packageName) {
        mPackageName = packageName;
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
//        getDialog().getWindow().setType(WindowManager.LayoutParams.TYPE_SYSTEM_ALERT);
        mRootView = inflater.inflate(R.layout.confirm_dialog, container);
        mPackageManager = getActivity().getPackageManager();
        TextView hint = mRootView.findViewById(R.id.confirm_hint);
        hint.setText(mHint);
        TextView appName = mRootView.findViewById(R.id.app_name);
        if (mType == TYPE_UNINSTALL) {
            appName.setVisibility(View.VISIBLE);
            appName.setText(mAppName);
        } else {
            appName.setVisibility(View.GONE);
        }
        ImageView appIcon = mRootView.findViewById(R.id.app_icon);
        if (mType == TYPE_UNINSTALL) {
            appIcon.setVisibility(View.VISIBLE);
            appIcon.setImageDrawable(mAppIcon);
        } else {
            appIcon.setVisibility(View.GONE);
        }
        TextView confirmCancel = mRootView.findViewById(R.id.cancel);
        confirmCancel.setOnClickListener(this);
        TextView confirmOK = mRootView.findViewById(R.id.ok);
        confirmOK.setOnClickListener(this);
        return mRootView;
    }

    @Override
    public void onSaveInstanceState(Bundle outState) {
        outState.putString("WORKAROUND_FOR_BUG_19917_KEY", "WORKAROUND_FOR_BUG_19917_VALUE");
        super.onSaveInstanceState(outState);
    }

    @Override
    public void onStart() {
        super.onStart();
        Window window = getDialog().getWindow();
        WindowManager.LayoutParams windowParams = window.getAttributes();
        windowParams.dimAmount = 0;
        windowParams.flags |= WindowManager.LayoutParams.FLAG_DIM_BEHIND;
        window.setAttributes(windowParams);
        window.setBackgroundDrawable(new ColorDrawable(Color.TRANSPARENT));
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
    }

    @Override
    public void onDismiss(DialogInterface dialog) {
        super.onDismiss(dialog);
    }

    @Override
    public void onClick(View v) {
        int id = v.getId();
        switch (id) {
            case R.id.cancel:
                dismiss();
                break;
            case R.id.ok:
                dismiss();
                if (mType == TYPE_UNINSTALL) {
                    AppManager.getInstance().uninstallPackage(getActivity(), mPackageManager, mPackageName);
                } else if (mType == TYPE_STOP) {
                    AppManager.getInstance().stopApplication(mPackageName);
                }
                break;
        }
    }
}
