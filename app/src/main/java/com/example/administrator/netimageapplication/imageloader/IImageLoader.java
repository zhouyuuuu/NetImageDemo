package com.example.administrator.netimageapplication.imageloader;

import android.widget.ImageView;

import com.example.administrator.netimageapplication.bean.ImageCache;
import com.example.administrator.netimageapplication.bean.ImageInfo;
import com.example.administrator.netimageapplication.view.PercentProgressBar;

/**
 * Edited by Administrator on 2018/3/14.
 */

public interface IImageLoader {
    void loadNetImageInfo();

    void loadNetImage(ImageInfo ii, ImageView iv, PercentProgressBar ppb, ImageCache ic, boolean thumbnail);

    void shutdownAllTask();

    void restartLoading();

    void pauseLoading();
}
