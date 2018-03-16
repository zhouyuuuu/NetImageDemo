package com.example.administrator.netimageapplication.util;

import android.graphics.Bitmap;
import android.widget.ImageView;

import com.example.administrator.netimageapplication.application.NetImageApplication;

/**
 * Edited by Administrator on 2018/3/8.
 */

public class BitmapUtil {
    // 默认的压缩宽高，在imageView获取不到宽高时则使用此常量
    private static final int WIDTH_THUMBNAIL_DEFAULT = (int) dip2px(70);
    private static final int HEIGHT_THUMBNAIL_DEFAULT = (int) dip2px(70);

    /**
     * 将图片压缩到适合imageView的大小
     */
    public static Bitmap resizeBitmap(Bitmap bitmap, ImageView imageView) {
        // 目标宽高
        int toWidth;
        int toHeight;
        // 当ImageView可以测量到宽高时使用imageView的宽高，否则使用默认宽高
        if (imageView.getWidth() != 0 && imageView.getHeight() != 0) {
            toWidth = imageView.getWidth();
            toHeight = imageView.getHeight();
        } else {
            toWidth = WIDTH_THUMBNAIL_DEFAULT;
            toHeight = HEIGHT_THUMBNAIL_DEFAULT;
        }
        // 计算目标比例
        float toScale = toWidth * 1.0f / toHeight;
        // bitmap的原始宽高
        int bitmapWidth = bitmap.getWidth();
        int bitmapHeight = bitmap.getHeight();
        // 如果bitmap已经比imageView小了，则没必要压缩
        if (bitmapWidth < toWidth && bitmapHeight < toHeight) return bitmap;
        // 计算bitmap比例
        float bitmapScale = bitmapWidth * 1.0f / bitmapHeight;
        int width;
        int height;
        // 如果bitmap比例大于目标比例，则说明bitmap比目标更“扁”，因此将toWidth作为目标宽度，然后通过toWidth和bitmap原有比例计算得到toHeight，反之同理
        if (bitmapScale > toScale) {
            width = toWidth;
            height = (int) (toWidth / bitmapScale);
        } else {
            height = toHeight;
            width = (int) (toHeight * bitmapScale);
        }
        return Bitmap.createScaledBitmap(bitmap, width, height, false);
    }

    /**
     * dp转为px像素
     *
     * @param dpValue dp值
     * @return px值
     */
    public static float dip2px(float dpValue) {
        float scale = NetImageApplication.getApplication().getResources().getDisplayMetrics().density;
        // 注意四舍五入
        return dpValue * scale + 0.5f;
    }
}
