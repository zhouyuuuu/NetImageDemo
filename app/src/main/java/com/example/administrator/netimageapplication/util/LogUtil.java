package com.example.administrator.netimageapplication.util;

import android.util.Log;
import com.example.administrator.netimageapplication.BuildConfig;


/**
 * Edited by Administrator on 2018/3/4.
 */

public class LogUtil {
    public static void e(String TAG,String description){
        if (BuildConfig.LOG) {
            Log.e(TAG, description);
        }
    }
}
