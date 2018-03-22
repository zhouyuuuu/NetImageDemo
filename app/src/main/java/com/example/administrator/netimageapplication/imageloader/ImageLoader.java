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

public class ImageLoader implements IImageLoader {
    // 需要被重新执行的加载请求
    private final ArrayList<LoadNetImageRunnable> mRestartRequests;
    // 即将要执行的加载请求
    private final ArrayList<LoadNetImageRunnable> mPendingRequests;
    // 即将要执行的加载请求
    private final ArrayList<LoadNetImageRunnable> mRunningRequests;
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

    public ImageLoader(NetImagePresenter mNetImagePresenter) {
        this.mNetImagePresenterRef = new WeakReference<>(mNetImagePresenter);
        mThreadPoolExecutor = new ThreadPoolExecutor(3, 5, 10, TimeUnit.SECONDS, new PriorityBlockingQueue<Runnable>());
        mNotifyThreadPoolExecutor = new ThreadPoolExecutor(1, 1, 10, TimeUnit.SECONDS, new PriorityBlockingQueue<Runnable>());
        mRestartRequests = new ArrayList<>();
        mPendingRequests = new ArrayList<>();
        mRunningRequests = new ArrayList<>();
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
        mThreadPoolExecutor.execute(new LoadNetImageInfoRunnable(1));
    }

    /**
     * 提交一条加载图片的线程到线程池中
     */
    @Override
    public void loadNetImage(ImageInfo imageInfo, ImageView imageView, PercentProgressBar percentProgressBar, ImageCache imageCache, boolean thumbnail) {
        LoadNetImageRunnable runnable = new LoadNetImageRunnable(1, imageInfo, percentProgressBar, imageView, imageCache, thumbnail);
        loadNetImage(runnable);
    }

    private void loadNetImage(LoadNetImageRunnable runnable) {
        // 加载之前要先看看是否有相同的任务正准备执行或者已经在执行了，如果有的话就直接return取消了
        synchronized (mRunningRequests) {
            for (LoadNetImageRunnable runningRunnable : mRunningRequests) {
                // 如果thumbnail和imageInfo对象是相同的，我们就认为两个任务相同
                if (runningRunnable.thumbnail == runnable.thumbnail && runningRunnable.imageInfo == runnable.imageInfo) {
                    return;
                }
            }
        }
        // 这里同上
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

    @Override
    public void restartLoading() {
        mNotifyThreadPoolExecutor.execute(new RestartRunnable(1));
    }

    @Override
    public void pauseLoading() {
        mNotifyThreadPoolExecutor.execute(new PauseRunnable(1));
    }


    //通过三级缓存策略获取图片
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
        void call() {
            synchronized (mRunningRequests) {
                mRunningRequests.add(this);
            }
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
                synchronized (mRunningRequests) {
                    mRunningRequests.remove(this);
                }
                return;
            }
            if (mIsCancelled) {
                synchronized (mRestartRequests) {
                    synchronized (mRunningRequests) {
                        mRunningRequests.remove(this);
                        mRestartRequests.add(this);
                    }
                }
                return;
            }
            if (imageView == null || percentProgressBar == null) {
                netImagePresenter.netImageLoaded(null, null, url);
                // 结束前将此任务从mRunningRequests中移除
                synchronized (mRunningRequests) {
                    mRunningRequests.remove(this);
                }
                return;
            }
            // 显示开始加载图片的进度条
            netImagePresenter.showProgressBar(url, percentProgressBar);
            // 获取图片
            bitmap = getBitmap(url, imageView, percentProgressBar, imageCacheRef.get());
            if (mIsCancelled) {
                // 取消了要隐藏进度条
                netImagePresenter.hideProgressBar(url, percentProgressBar);
                synchronized (mRestartRequests) {
                    synchronized (mRunningRequests) {
                        mRunningRequests.remove(this);
                        mRestartRequests.add(this);
                    }
                }
                return;
            }
            if (bitmap == null) {
                netImagePresenter.loadImageFailed();
                // 失败了要隐藏进度条
                netImagePresenter.hideProgressBar(url, percentProgressBar);
                synchronized (mRunningRequests) {
                    mRunningRequests.remove(this);
                }
                return;
            }
            // 通知presenter图片加载完成
            netImagePresenter.netImageLoaded(bitmap, imageView, url);
            // 完成了要隐藏进度条
            netImagePresenter.hideProgressBar(url, percentProgressBar);
            // 结束前将此任务从mRunningRequests中移除
            synchronized (mRunningRequests) {
                mRunningRequests.remove(this);
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
