package com.example.administrator.netimageapplication.imageloader;

import android.widget.ImageView;

import com.example.administrator.netimageapplication.bean.ImageInfo;
import com.example.administrator.netimageapplication.view.PercentProgressBar;

/**
 * 图片加载器
 * Edited by Administrator on 2018/3/14.
 */

public interface IImageLoader {
    /**
     * 加载图片数据
     */
    void loadNetImageInfo();

    /**
     * 加载图片
     * @param imageInfo 图片数据
     * @param imageView ImageView
     * @param percentProgressBar 进度条
     * @param thumbnail 是否缩略图
     */
    void loadNetImage(ImageInfo imageInfo, ImageView imageView, PercentProgressBar percentProgressBar, boolean thumbnail);

    /**
     * 终止加载任务
     */
    void shutdownAllTask();

    /**
     * 重新加载
     */
    void restartLoading();

    /**
     * 暂停加载
     */
    void pauseLoading();
}
