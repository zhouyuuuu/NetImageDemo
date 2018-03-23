package com.example.administrator.netimageapplication.imageloader;

import android.graphics.Bitmap;
import android.support.annotation.NonNull;
import android.widget.ImageView;

import com.example.administrator.netimageapplication.cache.ImageCache;
import com.example.administrator.netimageapplication.bean.ImageInfo;
import com.example.administrator.netimageapplication.imagepresenter.NetImagePresenter;
import com.example.administrator.netimageapplication.util.BindUtil;
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
 * 图片加载类，实现三级缓存、可暂停重启请求。（概念：图片加载指下载bitmap，图片数据加载指请求图片的url集合）
 * Edited by Administrator on 2018/3/14.
 */
public class ImageLoader implements IImageLoader {
    // 需要被重新执行的加载请求
    private final ArrayList<LoadNetImageRunnable> mRestartRequests;
    // 即将要执行的加载请求
    private final ArrayList<LoadNetImageRunnable> mPendingRequests;
    // presenter的弱引用，当Activity销毁后，Presenter就没用了，所以这里持弱引用
    private WeakReference<NetImagePresenter> mNetImagePresenterRef;
    // 加载图片线程池(核心3，最大5)
    private ThreadPoolExecutor mThreadPoolExecutor;
    // 通知唤醒线程池(核心1，最大1)
    private ThreadPoolExecutor mNotifyThreadPoolExecutor;
    // 取消加载标志位
    private volatile Boolean mIsCancelled = false;
    // 进度监听器
    private ProgressListener mProgressListener;
    // 是否在请求图片数据了
    private volatile Boolean mIsLoadingData = false;
    // 图片内存缓存
    private ImageCache mImageCache;

    public ImageLoader(NetImagePresenter mNetImagePresenter) {
        this.mNetImagePresenterRef = new WeakReference<>(mNetImagePresenter);
        int NUMBER_OF_CORES = Runtime.getRuntime().availableProcessors();
        int NUMBER_OF_MAX = 2*NUMBER_OF_CORES;
        int KEEP_ALIVE_TIME = 1;
        TimeUnit KEEP_ALIVE_TIME_UNIT = TimeUnit.SECONDS;
//        BlockingQueue<Runnable> taskQueue = new LinkedBlockingQueue<>();
        mThreadPoolExecutor = new ThreadPoolExecutor(NUMBER_OF_CORES, NUMBER_OF_MAX, KEEP_ALIVE_TIME, KEEP_ALIVE_TIME_UNIT, new PriorityBlockingQueue<Runnable>());
        mNotifyThreadPoolExecutor = new ThreadPoolExecutor(1, 1, KEEP_ALIVE_TIME, KEEP_ALIVE_TIME_UNIT, new PriorityBlockingQueue<Runnable>());
        mRestartRequests = new ArrayList<>();
        mPendingRequests = new ArrayList<>();
        mImageCache = new ImageCache();
        mProgressListener = new ProgressListener() {
            @Override
            public void onProgressUpdate(int percent, PercentProgressBar percentProgressBar, String url) {
                NetImagePresenter netImagePresenter = mNetImagePresenterRef.get();
                if (netImagePresenter != null) {
                    netImagePresenter.loadingProgressUpdate(percent, percentProgressBar, url);
                }
            }
        };
    }

    /**
     * 提交一条加载图片数据的线程到线程池中
     */
    @Override
    public void loadNetImageInfo() {
        if (!mIsLoadingData) {
            mIsLoadingData = true;
        }
        mThreadPoolExecutor.execute(new LoadNetImageInfoRunnable(1));
    }

    /**
     * 先检查内存中是否有图片，有则直接回调，否则提交一条加载图片的线程到线程池中
     */
    @Override
    public void loadNetImage(ImageInfo imageInfo, ImageView imageView, PercentProgressBar percentProgressBar, boolean thumbnail) {
        String url;
        if (thumbnail){
            url = imageInfo.getThumbnailUrl();
        }else {
            url = imageInfo.getOriginalImageUrl();
        }
        Bitmap bitmap = mImageCache.getBitmap(url);
        if (bitmap != null){
            NetImagePresenter netImagePresenter = mNetImagePresenterRef.get();
            if (netImagePresenter != null){
                netImagePresenter.netImageLoaded(bitmap,imageView,url);
            }
            return;
        }
        LoadNetImageRunnable runnable = new LoadNetImageRunnable(1, imageInfo, percentProgressBar, imageView, thumbnail);
        loadNetImage(runnable);
    }

    /**
     * 提交图片加载的线程到线程池中
     * @param runnable 任务
     */
    private void loadNetImage(LoadNetImageRunnable runnable) {
        // 加载之前要先看看是否有相同的任务正准备执行，如果有的话就直接return取消了
        synchronized (mPendingRequests) {
            for (LoadNetImageRunnable pendingRunnable : mPendingRequests) {
                if (pendingRunnable.thumbnail == runnable.thumbnail && pendingRunnable.imageInfo == runnable.imageInfo) {
                    return;
                }
            }
            mPendingRequests.add(runnable);
        }
        // 提交到线程池执行
        mThreadPoolExecutor.execute(runnable);
    }

    /**
     * 关闭线程池
     */
    @Override
    public void shutdownAllTask() {
        mThreadPoolExecutor.shutdownNow();
        mNotifyThreadPoolExecutor.shutdownNow();
    }

    /**
     * 重新启动任务
     */
    @Override
    public void restartLoading() {
        mNotifyThreadPoolExecutor.execute(new RestartRunnable(1));
    }

    /**
     * 暂停任务
     */
    @Override
    public void pauseLoading() {
        mNotifyThreadPoolExecutor.execute(new PauseRunnable(1));
    }


    /**
     * 通过三级缓存策略获取图片
     */
    private Bitmap getBitmap(String url, ImageView imageView, PercentProgressBar percentProgressBar, ImageCache imageCache) {
        // 从内存缓存中获取图片
        Bitmap bitmap = imageCache == null ? null : imageCache.getBitmap(url);
        if (bitmap != null) {
            return bitmap;
        }

        // 硬盘中获取图片
        bitmap = DiskUtil.loadBitmap(url, mProgressListener, percentProgressBar);
        if (bitmap != null) {
            // 缓存一份到内存
            if (imageCache != null) {
                imageCache.putBitmap(url, bitmap);
            }
            return bitmap;
        }

        // 网络下载图片
        bitmap = NetUtil.loadBitmap(url, mProgressListener, percentProgressBar, mIsCancelled);
        if (bitmap != null) {
            // 下载完成的图片进行压缩
            bitmap = BitmapUtil.resizeBitmap(bitmap, imageView);
            // 缓存一份到硬盘
            DiskUtil.saveBitmap(bitmap, url);
            // 缓存一份到内存
            if (imageCache != null) {
                imageCache.putBitmap(url, bitmap);
            }
        }
        return bitmap;
    }




    // 通知重新开始请求的Runnable
    private class RestartRunnable extends BaseLoadRunnable {

        RestartRunnable(int priority) {
            super(priority);
        }

        @Override
        void call() {
            synchronized (mRestartRequests) {
                for (LoadNetImageRunnable runnable : mRestartRequests) {
                    loadNetImage(runnable);
                }
                mRestartRequests.clear();
            }
            mIsCancelled = false;
        }
    }


    // 通知请求暂停的Runnable
    private class PauseRunnable extends BaseLoadRunnable {

        PauseRunnable(int priority) {
            super(priority);
        }

        @Override
        void call() {
            synchronized (mRestartRequests) {
                synchronized (mPendingRequests) {
                    for (LoadNetImageRunnable runnable : mPendingRequests) {
                        mRestartRequests.add(runnable);
                        mThreadPoolExecutor.remove(runnable);
                    }
                    mPendingRequests.clear();
                }
            }
            mIsCancelled = true;
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
        void call() {

            ArrayList<ArrayList<ImageInfo>> infos = null;
            try {
                infos = NetUtil.loadImageInfo();
            } catch (IOException e) {
                e.printStackTrace();
            } catch (JSONException e) {
                e.printStackTrace();
            }
            NetImagePresenter netImagePresenter = mNetImagePresenterRef.get();
            if (netImagePresenter == null) return;
            if (infos != null) {
                netImagePresenter.netImageInfoLoaded(infos);
            } else {
                netImagePresenter.loadImageInfoFailed();
            }
            mIsLoadingData = false;
        }
    }

    /**
     * 加载图片的Runnable
     */
    private class LoadNetImageRunnable extends BaseLoadRunnable {

        private ImageInfo imageInfo;
        private WeakReference<ImageView> imageViewRef;
        private WeakReference<PercentProgressBar> percentProgressBarRef;
        private boolean thumbnail;

        LoadNetImageRunnable(int priority, ImageInfo imageInfo, PercentProgressBar percentProgressBar,ImageView imageView, boolean thumbnail) {
            super(priority);
            this.imageInfo = imageInfo;
            this.percentProgressBarRef = new WeakReference<>(percentProgressBar);
            this.thumbnail = thumbnail;
            this.imageViewRef = new WeakReference<>(imageView);
        }

        @Override
        void call() {
            synchronized (mPendingRequests) {
                mPendingRequests.remove(this);
            }
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
            NetImagePresenter netImagePresenter = mNetImagePresenterRef.get();
            ImageView imageView = imageViewRef.get();
            PercentProgressBar percentProgressBar = percentProgressBarRef.get();
            if (netImagePresenter == null) {
                return;
            }
            if (mIsCancelled) {
                synchronized (mRestartRequests) {
                        mRestartRequests.add(this);
                }
                return;
            }
            if (imageView == null || percentProgressBar == null) {
                netImagePresenter.netImageLoaded(null, null, url);
                // 结束前将此任务从mRunningRequests中移除
                return;
            }
            if (BindUtil.isBound(percentProgressBar, url)) {
                // 显示开始加载图片的进度条
                netImagePresenter.showProgressBar(url, percentProgressBar);
            }
            // 获取图片
            bitmap = getBitmap(url, imageView, percentProgressBar, mImageCache);
            netImagePresenter = mNetImagePresenterRef.get();
            if (mIsCancelled) {
                // 取消了要隐藏进度条
                netImagePresenter.hideProgressBar(url, percentProgressBar);
                synchronized (mRestartRequests) {
                        mRestartRequests.add(this);
                }
                return;
            }
            if (bitmap == null) {
                if (BindUtil.isBound(percentProgressBar, url)) {
                    netImagePresenter.loadImageFailed();
                    // 失败了要隐藏进度条
                    netImagePresenter.hideProgressBar(url, percentProgressBar);
                }
                return;
            }
            if (BindUtil.isBound(percentProgressBar, url)) {
                // 通知presenter图片加载完成
                netImagePresenter.netImageLoaded(bitmap, imageView, url);
                // 完成了要隐藏进度条
                netImagePresenter.hideProgressBar(url, percentProgressBar);
            }
        }
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

        abstract void call();
    }

    /**
     * 进度监听器
     */
    public interface ProgressListener {
        void onProgressUpdate(int percent, PercentProgressBar percentProgressBar, String url);
    }
}
