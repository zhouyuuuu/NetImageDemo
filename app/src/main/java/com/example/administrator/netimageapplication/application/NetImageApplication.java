package com.example.administrator.netimageapplication.application;

import android.annotation.SuppressLint;
import android.app.Application;

/**
 * Edited by Administrator on 2018/3/14.
 */

public class NetImageApplication extends Application {
    @SuppressLint("StaticFieldLeak")
    private static Application netImageApplication = null;

    public static Application getApplication() {
        return netImageApplication;
    }

    @Override
    public void onCreate() {
        super.onCreate();
        netImageApplication = this;
    }
}
