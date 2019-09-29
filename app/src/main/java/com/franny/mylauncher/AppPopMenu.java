package com.franny.mylauncher;

import android.annotation.SuppressLint;
import android.app.DialogFragment;
import android.app.FragmentManager;
import android.content.DialogInterface;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.graphics.drawable.Drawable;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.view.WindowManager;
import android.widget.TextView;


public class AppPopMenu extends DialogFragment implements View.OnClickListener {
    private static volatile AppPopMenu instance = null;
    private View mRootView;
    private TextView mTitleTv, mItemUninstall, mItemStop;
    private String mAppName, mPackageName;
    private Drawable mAppIcon;
    private FragmentManager mFragmentManager;
    private ConfirmDialog mConfirmDialog;

    public static AppPopMenu getInstance(FragmentManager fragmentManager) {
        if (instance == null) {
            synchronized (AppPopMenu.class) {
                if (instance == null) {
                    instance = new AppPopMenu(fragmentManager);
                }
            }
        }
        return instance;
    }

    @SuppressLint("ValidFragment")
    private AppPopMenu(FragmentManager fragmentManager) {
        mFragmentManager = fragmentManager;
    }

    public void setAppName(String name) {
        mAppName = name;
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
        mRootView = inflater.inflate(R.layout.app_pop_menu, container);

        mTitleTv = mRootView.findViewById(R.id.title);
        mTitleTv.setText(mAppName);
        mItemUninstall = mRootView.findViewById(R.id.menu_item_uninstall);
        mItemStop = mRootView.findViewById(R.id.menu_item_stop);
        mItemUninstall.setOnClickListener(this);
        mItemStop.setOnClickListener(this);
        mConfirmDialog = ConfirmDialog.getInstance();
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
        windowParams.dimAmount = 0f;
        windowParams.flags |= WindowManager.LayoutParams.FLAG_DIM_BEHIND;
        window.setAttributes(windowParams);
        window.setBackgroundDrawable(new ColorDrawable(Color.TRANSPARENT));
        getDialog().setCanceledOnTouchOutside(true);
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
    }

    @Override
    public void show(FragmentManager manager, String tag) {
        try {
            super.show(manager, tag);
        } catch (Exception e) {

        }
    }

    @Override
    public void onDismiss(DialogInterface dialog) {
        super.onDismiss(dialog);
    }

    @Override
    public void onClick(View v) {
        int id = v.getId();
        switch (id) {
            case R.id.menu_item_uninstall:
                dismiss();
                mConfirmDialog.setType(ConfirmDialog.TYPE_UNINSTALL);
                mConfirmDialog.setAppName(mAppName);
                mConfirmDialog.setAppIcon(mAppIcon);
                mConfirmDialog.setPackageName(mPackageName);
                mConfirmDialog.setHint(getString(R.string.confirm_uninstall_hint));
                mConfirmDialog.show(mFragmentManager, "");
                break;
            case R.id.menu_item_stop:
                dismiss();
                mConfirmDialog.setType(ConfirmDialog.TYPE_STOP);
                mConfirmDialog.setPackageName(mPackageName);
                mConfirmDialog.setHint(getString(R.string.confirm_stop_hint));
                mConfirmDialog.show(mFragmentManager, "");
                break;
        }
    }
}
