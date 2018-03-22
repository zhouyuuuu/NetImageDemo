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
    void updateImageLoadingProgress(int percent, PercentProgressBar percentProgressBar, String url);

    void setImageViewBitmap(ImageView iv, Bitmap bm, String url);

    void netImageInfoLoaded(ArrayList<ArrayList<ImageInfo>> infos);

    void changeImageProgressBarVisibility(PercentProgressBar percentProgressBar, int visibility, String url);

    void changeImageInfoProgressBarVisibility(int visibility);

    void setRetryButtonVisibility(int visibility);

    void ToastImageLoadFailedInfo();
}
