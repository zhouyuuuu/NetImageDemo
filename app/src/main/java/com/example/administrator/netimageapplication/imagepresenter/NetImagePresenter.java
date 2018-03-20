package com.example.administrator.netimageapplication.imagepresenter;

import android.graphics.Bitmap;
import android.view.View;
import android.widget.ImageView;

import com.example.administrator.netimageapplication.bean.ImageCache;
import com.example.administrator.netimageapplication.bean.ImageInfo;
import com.example.administrator.netimageapplication.imagedisplayer.INetImageDisplayer;
import com.example.administrator.netimageapplication.imageloader.IImageLoader;
import com.example.administrator.netimageapplication.imageloader.ImageLoader;
import com.example.administrator.netimageapplication.view.PercentProgressBar;

import java.util.ArrayList;

/**
 * Edited by Administrator on 2018/3/14.
 */

public class NetImagePresenter {
    // NetImageActivity
    private INetImageDisplayer iNetImageDisplayer;
    // ImageLoader
    private IImageLoader iImageLoader;

    public NetImagePresenter(INetImageDisplayer iNetImageDisplayer) {
        this.iNetImageDisplayer = iNetImageDisplayer;
        this.iImageLoader = new ImageLoader(this);
    }

    /**
     * 加载ImageInfo图片数据集合
     */
    public void loadNetImageInfo() {
        // 加载前先显示一下进度条
        iNetImageDisplayer.changeImageInfoProgressBarVisibility(View.VISIBLE);
        // 开始加载数据
        iImageLoader.loadNetImageInfo();
    }

    /**
     * 加载图片(缩略图或原图)
     *
     * @param imageView  哪个imageView发起的加载请求，在加载完成后就设置在这个imageView上
     * @param imageInfo  图片数据
     * @param imageCache 图片内存缓存，加载完成后将图片缓存到这里
     * @param thumbnail  是否是缩略图，否则是原图
     */
    public void loadNetImage(ImageInfo imageInfo, PercentProgressBar percentProgressBar, ImageView imageView, ImageCache imageCache, boolean thumbnail) {
        // 判断这个加载请求是缩略图加载还是原图加载，原图的话要显示原图的进度条并更新进度
        iNetImageDisplayer.updateImageLoadingProgress(0, percentProgressBar);
        iNetImageDisplayer.changeImageProgressBarVisibility(percentProgressBar, View.VISIBLE);
        // 开始加载图片
        iImageLoader.loadNetImage(imageInfo, imageView, percentProgressBar, imageCache, thumbnail);
    }

    /**
     * 图片数据加载完毕时回调该方法
     *
     * @param infos 图片数据
     */
    public void netImageInfoLoaded(ArrayList<ArrayList<ImageInfo>> infos) {
        // 隐藏进度条
        iNetImageDisplayer.changeImageInfoProgressBarVisibility(View.GONE);
        // 图片数据加载完毕，回调数据交给Activity处理
        iNetImageDisplayer.netImageInfoLoaded(infos);
    }

    /**
     * 图片数据加载完毕时回调该方法
     *
     * @param bitmap    图片
     * @param imageView View
     */
    public void netImageLoaded(Bitmap bitmap, ImageView imageView, PercentProgressBar percentProgressBar) {
        // 图片加载完毕，回调让activity把图片设置给imageView
        if (bitmap != null && imageView != null) {
            iNetImageDisplayer.setImageViewBitmap(imageView, bitmap);
        }
        // 隐藏进度条
        if (percentProgressBar != null) {
            iNetImageDisplayer.changeImageProgressBarVisibility(percentProgressBar, View.GONE);
        }
    }

    public void restartLoading(){
        iImageLoader.restartLoading();
    }

    public void pauseLoading(){
        iImageLoader.pauseLoading();
    }

    /**
     * 图片下载进度变化时回调
     *
     * @param percent 进度百分比
     */
    public void loadingProgressUpdate(int percent, PercentProgressBar percentProgressBar) {
        // 更新Activity中进度条的进度
        iNetImageDisplayer.updateImageLoadingProgress(percent, percentProgressBar);
    }

    public void stopLoading() {
        iImageLoader.shutdownAllTask();
    }

    public void loadImageFailed(PercentProgressBar percentProgressBar) {
        iNetImageDisplayer.changeImageProgressBarVisibility(percentProgressBar, View.GONE);
        iNetImageDisplayer.ToastImageLoadFailedInfo();
    }

    public void loadImageInfoFailed() {
        // 隐藏进度条
        iNetImageDisplayer.changeImageInfoProgressBarVisibility(View.GONE);
        iNetImageDisplayer.setRetryButtonVisibility(View.VISIBLE);
    }
}
