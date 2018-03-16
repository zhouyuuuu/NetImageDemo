package com.example.administrator.netimageapplication.imageloader;

import android.widget.ImageView;

import com.example.administrator.netimageapplication.Bean.ImageCache;
import com.example.administrator.netimageapplication.Bean.ImageInfo;

/**
 * Edited by Administrator on 2018/3/14.
 */

public interface IImageLoader {
    void loadNetImageInfo();

    void loadNetImage(ImageInfo ii, ImageView iv, ImageCache ic, boolean thumbnail);

    void notifyAllThread();

    void shutdownAllTask();
}
