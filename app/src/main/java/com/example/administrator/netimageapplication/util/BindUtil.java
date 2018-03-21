package com.example.administrator.netimageapplication.util;

import android.view.View;

import com.example.administrator.netimageapplication.R;

/**
 * Edited by Administrator on 2018/3/21.
 */

public class BindUtil {
    // 把url存进View的Tag中
    public static void bindUrlAndView(View v, String url) {
        v.setTag(R.id.url_view, url);
    }

    // 检查是否View的Tag和url相同，若相同则是绑定了
    public static boolean isBound(View v, String url) {
        return url.equals(v.getTag(R.id.url_view));
    }
}
