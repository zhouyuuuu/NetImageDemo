package com.example.administrator.netimageapplication.application;

import android.annotation.SuppressLint;
import android.app.Application;
import android.content.Context;

/**
 * Edited by Administrator on 2018/3/14.
 */

public class NetImageApplication extends Application {
    @SuppressLint("StaticFieldLeak")
    private static Context mContext;

    public static Context getApplication() {
        return mContext;
    }

    @Override
    public void onCreate() {
        super.onCreate();
        mContext = this;
    }
}
