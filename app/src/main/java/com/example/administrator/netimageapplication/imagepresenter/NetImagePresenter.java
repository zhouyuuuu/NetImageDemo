package com.example.administrator.netimageapplication.imagepresenter;

import android.graphics.Bitmap;
import android.view.View;
import android.widget.ImageView;

import com.example.administrator.netimageapplication.bean.ImageInfo;
import com.example.administrator.netimageapplication.imagedisplayer.INetImageDisplayer;
import com.example.administrator.netimageapplication.imageloader.IImageLoader;
import com.example.administrator.netimageapplication.imageloader.ImageLoader;
import com.example.administrator.netimageapplication.view.PercentProgressBar;

import java.lang.ref.WeakReference;
import java.util.ArrayList;

/**
 * 图片加载业务类
 * Edited by Administrator on 2018/3/14.
 */

public class NetImagePresenter {
    // NetImageActivity
    private WeakReference<INetImageDisplayer> mNetImageDisplayerRef;
    // ImageLoader
    private IImageLoader mImageLoader;

    public NetImagePresenter(INetImageDisplayer iNetImageDisplayer) {
        this.mNetImageDisplayerRef = new WeakReference<>(iNetImageDisplayer);
        this.mImageLoader = new ImageLoader(this);
    }

    /**
     * 加载ImageInfo图片数据集合
     */
    public void loadNetImageInfo() {
        INetImageDisplayer iNetImageDisplayer = mNetImageDisplayerRef.get();
        if (iNetImageDisplayer == null) return;
        // 加载前先显示一下进度条
        iNetImageDisplayer.changeImageInfoProgressBarVisibility(View.VISIBLE);
        // 开始加载数据
        mImageLoader.loadNetImageInfo();
    }

    /**
     * 加载图片(缩略图或原图)
     *
     * @param imageView  哪个imageView发起的加载请求，在加载完成后就设置在这个imageView上
     * @param imageInfo  图片数据
     * @param thumbnail  是否是缩略图，否则是原图
     */
    public void loadNetImage(ImageInfo imageInfo, PercentProgressBar percentProgressBar, ImageView imageView, boolean thumbnail) {
        // 开始加载图片
        mImageLoader.loadNetImage(imageInfo, imageView, percentProgressBar, thumbnail);
    }

    /**
     * 显示加载进度条
     * @param url 路径
     * @param percentProgressBar 进度条
     */
    public void showProgressBar(String url, PercentProgressBar percentProgressBar) {
        INetImageDisplayer iNetImageDisplayer = mNetImageDisplayerRef.get();
        if (iNetImageDisplayer == null) return;
        if (percentProgressBar != null) {
            // 每次显示都将其进度设置为0，因为这总是代表着一个加载操作的开始
            iNetImageDisplayer.updateImageLoadingProgress(0, percentProgressBar, url);
            iNetImageDisplayer.changeImageProgressBarVisibility(percentProgressBar, View.VISIBLE, url);
        }
    }

    /**
     * 隐藏加载进度条
     * @param url 路径
     * @param percentProgressBar 进度条
     */
    public void hideProgressBar(String url, PercentProgressBar percentProgressBar) {
        INetImageDisplayer iNetImageDisplayer = mNetImageDisplayerRef.get();
        if (iNetImageDisplayer == null) return;
        if (percentProgressBar != null) {
            iNetImageDisplayer.changeImageProgressBarVisibility(percentProgressBar, View.GONE, url);
        }
    }

    /**
     * 图片数据加载完毕时回调该方法
     *
     * @param infos 图片数据
     */
    public void netImageInfoLoaded(ArrayList<ArrayList<ImageInfo>> infos) {
        INetImageDisplayer iNetImageDisplayer = mNetImageDisplayerRef.get();
        if (iNetImageDisplayer == null) return;
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
    public void netImageLoaded(Bitmap bitmap, ImageView imageView, String url) {
        INetImageDisplayer iNetImageDisplayer = mNetImageDisplayerRef.get();
        if (iNetImageDisplayer == null) return;
        // 图片加载完毕，回调让activity把图片设置给imageView，注意url要与image有绑定关系，如果没绑定则不设置
        if (bitmap != null && imageView != null) {
            iNetImageDisplayer.setImageViewBitmap(imageView, bitmap, url);
        }
    }

    /**
     * 图片加载重启
     */
    public void restartLoading() {
        mImageLoader.restartLoading();
    }

    /**
     * 图片加载暂停
     */
    public void pauseLoading() {
        mImageLoader.pauseLoading();
    }

    /**
     * 图片下载进度变化时回调
     *
     * @param percent 进度百分比
     */
    public void loadingProgressUpdate(int percent, PercentProgressBar percentProgressBar, String url) {
        INetImageDisplayer iNetImageDisplayer = mNetImageDisplayerRef.get();
        if (iNetImageDisplayer == null) return;
        // 更新Activity中进度条的进度
        iNetImageDisplayer.updateImageLoadingProgress(percent, percentProgressBar, url);
    }

    /**
     * 停止加载，停止了之后就没法再重启了，用在Activity销毁时调用
     */
    public void stopLoading() {
        mImageLoader.shutdownAllTask();
    }

    /**
     * 图片加载失败的回调
     */
    public void loadImageFailed() {
        INetImageDisplayer iNetImageDisplayer = mNetImageDisplayerRef.get();
        if (iNetImageDisplayer == null) return;
        // 弹出加载失败的信息
        iNetImageDisplayer.toastImageLoadFailedInfo();
    }

    /**
     * 图片数据加载失败的回调
     */
    public void loadImageInfoFailed() {
        INetImageDisplayer iNetImageDisplayer = mNetImageDisplayerRef.get();
        if (iNetImageDisplayer == null) return;
        // 隐藏进度条
        iNetImageDisplayer.changeImageInfoProgressBarVisibility(View.GONE);
        // 显示重试按钮
        iNetImageDisplayer.setRetryButtonVisibility(View.VISIBLE);
    }
}
