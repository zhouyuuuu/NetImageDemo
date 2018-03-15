package com.example.administrator.netimageapplication.imagepresenter;

import android.graphics.Bitmap;
import android.view.View;
import android.widget.ImageView;

import com.example.administrator.netimageapplication.Bean.ImageCache;
import com.example.administrator.netimageapplication.Bean.ImageInfo;
import com.example.administrator.netimageapplication.imagedisplayer.INetImageDisplayer;
import com.example.administrator.netimageapplication.imageloader.IImageLoader;
import com.example.administrator.netimageapplication.imageloader.ImageLoader;

import java.util.ArrayList;

/**
 * Edited by Administrator on 2018/3/14.
 */

public class NetImagePresenter {
    private INetImageDisplayer iNetImageDisplayer;
    private IImageLoader iImageLoader;

    public NetImagePresenter(INetImageDisplayer iNetImageDisplayer) {
        this.iNetImageDisplayer = iNetImageDisplayer;
        this.iImageLoader = new ImageLoader(this);
    }

    public void loadNetImageInfo(){
        iNetImageDisplayer.changeThumbnailListProgressBarVisibility(View.VISIBLE);
        iImageLoader.loadNetImageInfo();
    }
    public void loadNetImage(ImageInfo imageInfo, ImageView imageView, ImageCache imageCache, boolean thumbnail){
        if (!thumbnail){
            iNetImageDisplayer.updateOriginalImageProgress(0);
        }
        iNetImageDisplayer.changeOriginalImageProgressBarVisibility(View.VISIBLE);
        iImageLoader.loadNetImage(imageInfo,imageView,imageCache,thumbnail);
    }

    public void netImageInfoLoaded(ArrayList<ArrayList<ImageInfo>> infos){
        iNetImageDisplayer.netImageInfoLoaded(infos);
        iNetImageDisplayer.changeThumbnailListProgressBarVisibility(View.GONE);
    }

    public void netImageLoaded(Bitmap bitmap,ImageView imageView){
        iNetImageDisplayer.setImageViewBitmap(imageView,bitmap);
        iNetImageDisplayer.changeOriginalImageProgressBarVisibility(View.GONE);
    }

    public void notifyIsReadyToUpdate(){
        iImageLoader.notifyAllThread();
    }

    public boolean ifDisplayerIsReadyToUpdate(){
        return iNetImageDisplayer.isReadyToUpdate();
    }

    public void loadingProgressUpdate(int percent){
        iNetImageDisplayer.updateOriginalImageProgress(percent);
    }
}
