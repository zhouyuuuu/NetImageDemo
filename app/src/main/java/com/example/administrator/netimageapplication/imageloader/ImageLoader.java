package com.example.administrator.netimageapplication.imageloader;

import android.graphics.Bitmap;
import android.support.annotation.NonNull;
import android.widget.ImageView;

import com.example.administrator.netimageapplication.bean.ImageCache;
import com.example.administrator.netimageapplication.bean.ImageInfo;
import com.example.administrator.netimageapplication.imagepresenter.NetImagePresenter;
import com.example.administrator.netimageapplication.util.BitmapUtil;
import com.example.administrator.netimageapplication.util.DiskUtil;
import com.example.administrator.netimageapplication.util.NetUtil;
import com.example.administrator.netimageapplication.view.PercentProgressBar;

import org.json.JSONException;

import java.io.IOException;
import java.lang.ref.WeakReference;
import java.util.ArrayList;
import java.util.concurrent.PriorityBlockingQueue;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;

/**
 * Edited by Administrator on 2018/3/14.
 */

public class ImageLoader implements IImageLoader, NetUtil.ProgressListener {
    // 持有自己的引用，作为同步锁的对象
    private final ImageLoader mImageLoader;
    private WeakReference<NetImagePresenter> mNetImagePresenterRef;
    // 加载图片线程池(核心3，最大5)
    private ThreadPoolExecutor mThreadPoolExecutor;
    // 通知唤醒线程池(核心1，最大1)
    private ThreadPoolExecutor mNotifyThreadPoolExecutor;

    public ImageLoader(NetImagePresenter mNetImagePresenter) {
        this.mNetImagePresenterRef = new WeakReference<>(mNetImagePresenter);
        mImageLoader = this;
        mThreadPoolExecutor = new ThreadPoolExecutor(3, 5, 10, TimeUnit.SECONDS, new PriorityBlockingQueue<Runnable>());
        mNotifyThreadPoolExecutor = new ThreadPoolExecutor(1, 1, 10, TimeUnit.SECONDS, new PriorityBlockingQueue<Runnable>());
    }

    /**
     * 提交一条加载图片数据的线程到线程池中
     */
    @Override
    public void loadNetImageInfo() {
        mThreadPoolExecutor.execute(new LoadNetImageInfoRunnable(1));
    }

    /**
     * 提交一条加载图片的线程到线程池中
     */
    @Override
    public void loadNetImage(ImageInfo ii, ImageView iv, PercentProgressBar ppb, ImageCache ic, boolean thumbnail) {
        mThreadPoolExecutor.execute(new LoadNetImageRunnable(1, ii, ppb, iv, ic, thumbnail));
    }

    /**
     * 唤醒所有等待中的线程
     */
    @Override
    public void notifyAllThread() {
        mNotifyThreadPoolExecutor.execute(new NotifyRunnable(1));
    }

    /**
     * 关闭线程池
     */
    @Override
    public void shutdownAllTask() {
        mThreadPoolExecutor.shutdownNow();
    }

    /**
     * 进度更新时回调，通知presenter进度已经变化
     *
     * @param percent 进度百分比
     */
    @Override
    public void onProgressUpdate(int percent, PercentProgressBar percentProgressBar) {
        NetImagePresenter netImagePresenter = mNetImagePresenterRef.get();
        if (netImagePresenter != null) {
            netImagePresenter.loadingProgressUpdate(percent, percentProgressBar);
        }
    }

    /**
     * 通过三级缓存策略获取图片
     */
    private Bitmap getBitmap(String url, ImageView imageView, PercentProgressBar percentProgressBar) {
        // 硬盘中获取图片
        Bitmap bitmap = DiskUtil.loadBitmap(url, this, percentProgressBar);
        // 如果硬盘获取不到
        if (bitmap == null) {
            // 网络下载图片
            bitmap = NetUtil.loadBitmap(url, mImageLoader, percentProgressBar);
            if (bitmap != null) {
                // 下载完成的图片进行压缩
                bitmap = BitmapUtil.resizeBitmap(bitmap, imageView);
                // 缓存一份到硬盘
                DiskUtil.saveBitmap(bitmap, url);
            }
        }
        return bitmap;
    }

    /**
     * 线程池运行Runnable的基类
     */
    private static abstract class BaseLoadRunnable implements Runnable, Comparable<BaseLoadRunnable> {

        private int priority;

        BaseLoadRunnable(int priority) {
            this.priority = priority;
        }

        private int getPriority() {
            return priority;
        }

        @Override
        public int compareTo(@NonNull BaseLoadRunnable another) {
            int my = this.getPriority();
            int other = another.getPriority();
            return my < other ? 1 : my > other ? -1 : 0;
        }

        @Override
        public void run() {
            call();
        }

        protected abstract void call();
    }

    /**
     * 通知唤醒的Runnable
     */
    private class NotifyRunnable extends BaseLoadRunnable {

        NotifyRunnable(int priority) {
            super(priority);
        }

        @Override
        protected void call() {
            synchronized (mImageLoader) {
                mImageLoader.notifyAll();
            }
        }
    }

    /**
     * 加载图片数据的Runnable
     */
    private class LoadNetImageInfoRunnable extends BaseLoadRunnable {

        LoadNetImageInfoRunnable(int priority) {
            super(priority);
        }

        @Override
        protected void call() {
            ArrayList<ArrayList<ImageInfo>> infos = null;
            try {
                infos = NetUtil.loadImageInfo();
            } catch (IOException e) {
                e.printStackTrace();
            } catch (JSONException e) {
                e.printStackTrace();
            }
            NetImagePresenter netImagePresenter = mNetImagePresenterRef.get();
            if (netImagePresenter != null) {
                if (infos != null) {
                    netImagePresenter.netImageInfoLoaded(infos);
                } else {
                    netImagePresenter.loadImageInfoFailed();
                }
            }
        }
    }

    /**
     * 加载图片的Runnable
     */
    private class LoadNetImageRunnable extends BaseLoadRunnable {

        private ImageInfo imageInfo;
        private WeakReference<ImageView> imageViewRef;
        private WeakReference<PercentProgressBar> percentProgressBarRef;
        private WeakReference<ImageCache> imageCacheRef;
        private boolean thumbnail;

        LoadNetImageRunnable(int priority, ImageInfo imageInfo, PercentProgressBar percentProgressBar, ImageView imageView, ImageCache imageCache, boolean thumbnail) {
            super(priority);
            this.imageInfo = imageInfo;
            this.imageViewRef = new WeakReference<>(imageView);
            this.percentProgressBarRef = new WeakReference<>(percentProgressBar);
            this.imageCacheRef = new WeakReference<>(imageCache);
            this.thumbnail = thumbnail;
        }

        @Override
        protected void call() {
            Bitmap bitmap;
            String url;
            // 判断是否加载缩略图
            if (thumbnail) {
                // 拿到缩略图的URL
                url = imageInfo.getThumbnailUrl();
            } else {
                // 拿到原图的URL
                url = imageInfo.getOriginalImageUrl();
            }
            ImageView imageView = imageViewRef.get();
            PercentProgressBar percentProgressBar = percentProgressBarRef.get();
            NetImagePresenter netImagePresenter = mNetImagePresenterRef.get();
            if (netImagePresenter != null) {
                if (imageView != null) {
                    // 获取图片
                    bitmap = getBitmap(url, imageView, percentProgressBar);
                    if (bitmap == null) {
                        netImagePresenter.loadImageFailed(percentProgressBar);
                        return;
                    }
                    // 缓存一份到内存缓存中
                    ImageCache imageCache = imageCacheRef.get();
                    if (imageCache != null) {
                        imageCache.putBitmap(url, bitmap);
                    }
                    synchronized (mImageLoader) {
                        // 等待activity的view可以被更新
                        while (!netImagePresenter.ifDisplayerIsReadyToUpdate()) {
                            try {
                                // 等待
                                mImageLoader.wait();
                            } catch (InterruptedException e) {
                                e.printStackTrace();
                            }
                        }
                        // 通知presenter图片加载完成
                        netImagePresenter.netImageLoaded(bitmap, imageView, percentProgressBar);
                    }
                } else {
                    netImagePresenter.netImageLoaded(null, null, null);
                }
            }
        }
    }
}
