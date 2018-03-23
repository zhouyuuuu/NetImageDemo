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

    private BitmapUtil(){
        super();
    }

    /**
     * 将图片压缩到适合imageView的大小
     */
    public static Bitmap resizeBitmap(Bitmap bitmap, ImageView imageView) {
        // 目标宽高
        int targetWidth;
        int targetHeight;
        // 当ImageView可以测量到宽高时使用imageView的宽高，否则使用默认宽高
        if (imageView.getWidth() != 0 && imageView.getHeight() != 0) {
            targetWidth = imageView.getWidth();
            targetHeight = imageView.getHeight();
        } else {
            targetWidth = WIDTH_THUMBNAIL_DEFAULT;
            targetHeight = HEIGHT_THUMBNAIL_DEFAULT;
        }
        // 计算目标比例
        float targetScale = targetWidth * 1.0f / targetHeight;
        // bitmap的原始宽高
        int bitmapWidth = bitmap.getWidth();
        int bitmapHeight = bitmap.getHeight();
        // 如果bitmap已经比imageView小了，则没必要压缩
        if (bitmapWidth < targetWidth && bitmapHeight < targetHeight) return bitmap;
        // 计算bitmap比例
        float bitmapScale = bitmapWidth * 1.0f / bitmapHeight;
        int resultWidth;
        int resultHeight;
        // 如果bitmap比例大于目标比例，则说明bitmap比目标更“扁”，因此将toWidth作为目标宽度，然后通过toWidth和bitmap原有比例计算得到toHeight，反之同理
        if (bitmapScale > targetScale) {
            resultWidth = targetWidth;
            resultHeight = (int) (targetWidth / bitmapScale);
        } else {
            resultHeight = targetHeight;
            resultWidth = (int) (targetHeight * bitmapScale);
        }
        return Bitmap.createScaledBitmap(bitmap, resultWidth, resultHeight, false);
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
