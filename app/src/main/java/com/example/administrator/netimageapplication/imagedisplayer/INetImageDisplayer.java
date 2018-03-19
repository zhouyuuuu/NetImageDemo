package com.example.administrator.netimageapplication.imagedisplayer;

import android.graphics.Bitmap;
import android.widget.ImageView;

import com.example.administrator.netimageapplication.bean.ImageInfo;
import com.example.administrator.netimageapplication.view.PercentProgressBar;

import java.util.ArrayList;

/**
 * Edited by Administrator on 2018/3/14.
 */

public interface INetImageDisplayer {
    void updateImageLoadingProgress(int percent, PercentProgressBar percentProgressBar);

    void setImageViewBitmap(ImageView iv, Bitmap bm);

    void netImageInfoLoaded(ArrayList<ArrayList<ImageInfo>> infos);

    void changeImageProgressBarVisibility(PercentProgressBar percentProgressBar, int visibility);

    void changeImageInfoProgressBarVisibility(int visibility);

    boolean isReadyToUpdate();

    void setRetryButtonVisibility(int visibility);

    void ToastImageLoadFailedInfo();
}
