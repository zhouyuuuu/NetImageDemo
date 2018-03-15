package com.example.administrator.netimageapplication.util;

import android.graphics.Bitmap;
import android.widget.ImageView;

import com.example.administrator.netimageapplication.application.NetImageApplication;

/**
 * Edited by Administrator on 2018/3/8.
 */

public class BitmapUtil {
    private static final int WIDTH_THUMBNAIL_DEFAULT = (int) dip2px(70);
    private static final int HEIGHT_THUMBNAIL_DEFAULT = (int) dip2px(70);

    public static Bitmap resizeBitmap(Bitmap bitmap,ImageView imageView) {
        int toWidth;
        int toHeight;
        if (imageView.getWidth() != 0&&imageView.getHeight() != 0) {
            toWidth = imageView.getWidth();
            toHeight = imageView.getHeight();
        }else {
            toWidth = WIDTH_THUMBNAIL_DEFAULT;
            toHeight = HEIGHT_THUMBNAIL_DEFAULT;
        }
        float toScale = toWidth * 1.0f / toHeight;
        int bitmapWidth = bitmap.getWidth();
        int bitmapHeight = bitmap.getHeight();
        if (bitmapWidth< toWidth &&bitmapHeight< toHeight) return bitmap;
        float bitmapScale = bitmapWidth * 1.0f / bitmapHeight;
        int width;
        int height;
        if (bitmapScale > toScale) {
            width = toWidth;
            height = (int) (toWidth / bitmapScale);
        } else {
            height = toHeight;
            width = (int) (toHeight * bitmapScale);
        }
        return Bitmap.createScaledBitmap(bitmap,width,height,false);
    }

    public static float dip2px(float dpValue) {
        float scale = NetImageApplication.getApplication().getResources().getDisplayMetrics().density;
        return dpValue * scale + 0.5f;
    }
}
