package com.example.administrator.netimageapplication.imageloader;

import android.graphics.Bitmap;
import android.support.annotation.NonNull;
import android.widget.ImageView;

import com.example.administrator.netimageapplication.Bean.ImageCache;
import com.example.administrator.netimageapplication.Bean.ImageInfo;
import com.example.administrator.netimageapplication.imagepresenter.NetImagePresenter;
import com.example.administrator.netimageapplication.util.BitmapUtil;
import com.example.administrator.netimageapplication.util.DiskUtil;
import com.example.administrator.netimageapplication.util.LogUtil;
import com.example.administrator.netimageapplication.util.NetUtil;

import java.util.concurrent.PriorityBlockingQueue;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;

/**
 * Edited by Administrator on 2018/3/14.
 */

public class ImageLoader implements IImageLoader,NetUtil.ProgressListener {
    private NetImagePresenter mNetImagePresenter;
    private final ImageLoader mImageLoader;
    private ThreadPoolExecutor mThreadPoolExecutor;

    public ImageLoader(NetImagePresenter mNetImagePresenter) {
        this.mNetImagePresenter = mNetImagePresenter;
        mImageLoader = this;
        mThreadPoolExecutor = new ThreadPoolExecutor(3,5,10, TimeUnit.SECONDS,new PriorityBlockingQueue<Runnable>());
    }

    @Override
    public void loadNetImageInfo() {
        mThreadPoolExecutor.execute(new LoadNetImageInfoRunnable(1));
    }

    @Override
    public void loadNetImage(ImageInfo ii, ImageView iv, ImageCache ic, boolean thumbnail) {
        mThreadPoolExecutor.execute(new LoadNetImageRunnable(1,ii,iv,ic,thumbnail));
    }

    @Override
    public void notifyAllThread() {
        new Thread(new Runnable() {
            @Override
            public void run() {
                synchronized (mImageLoader) {
                    mImageLoader.notifyAll();
                }
            }
        }).start();
    }

    @Override
    public void onProgressUpdate(int percent) {
        mNetImagePresenter.loadingProgressUpdate(percent);
    }

    private static abstract class BaseLoadRunnable implements Runnable, Comparable<BaseLoadRunnable>{

        private int priority;

        private int getPriority() {
            return priority;
        }

        BaseLoadRunnable(int priority) {
            this.priority = priority;
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

    private class LoadNetImageInfoRunnable extends BaseLoadRunnable{

        LoadNetImageInfoRunnable(int priority) {
            super(priority);
        }

        @Override
        protected void call() {
            mNetImagePresenter.netImageInfoLoaded(NetUtil.loadImageInfo());
        }
    }

    private class LoadNetImageRunnable extends BaseLoadRunnable{

        private ImageInfo imageInfo;
        private ImageView imageView;
        private ImageCache imageCache;
        private boolean thumbnail;

        LoadNetImageRunnable(int priority, ImageInfo imageInfo, ImageView imageView, ImageCache imageCache, boolean thumbnail) {
            super(priority);
            this.imageInfo = imageInfo;
            this.imageView = imageView;
            this.imageCache = imageCache;
            this.thumbnail = thumbnail;
        }

        @Override
        protected void call() {
            Bitmap bitmap;
            java.lang.String url;
            if (thumbnail){
                url = imageInfo.getThumbnailUrl();
                bitmap = DiskUtil.loadBitmap(url);
                if (bitmap == null){
                    bitmap = NetUtil.loadBitmap(url,mImageLoader);
                    if (bitmap != null){
                        bitmap = BitmapUtil.resizeBitmap(bitmap,imageView);
                        DiskUtil.saveBitmap(bitmap,url);
                    }
                }
            }else {
                url = imageInfo.getOriginalImageUrl();
                bitmap = DiskUtil.loadBitmap(url);
                if (bitmap == null){
                    LogUtil.e("what","获取不到硬盘数据");
                    bitmap = NetUtil.loadBitmap(url,mImageLoader);
                    if (bitmap != null){
                        bitmap = BitmapUtil.resizeBitmap(bitmap,imageView);
                        DiskUtil.saveBitmap(bitmap,url);
                    }
                }
            }
            imageCache.putBitmap(url,bitmap);
            synchronized (mImageLoader) {
                while (!mNetImagePresenter.ifDisplayerIsReadyToUpdate()) {
                    try {
                        mImageLoader.wait();
                    } catch (InterruptedException e) {
                        e.printStackTrace();
                    }
                }
                mNetImagePresenter.netImageLoaded(bitmap, imageView);
            }
        }
    }
}
