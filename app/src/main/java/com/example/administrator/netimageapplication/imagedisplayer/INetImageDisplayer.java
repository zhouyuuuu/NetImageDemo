package com.example.administrator.netimageapplication.imagedisplayer;

import android.graphics.Bitmap;
import android.widget.ImageView;

import com.example.administrator.netimageapplication.Bean.ImageInfo;

import java.util.ArrayList;

/**
 * Edited by Administrator on 2018/3/14.
 */

public interface INetImageDisplayer {
    void updateOriginalImageProgress(int percent);
    void setImageViewBitmap(ImageView iv, Bitmap bm);
    void netImageInfoLoaded(ArrayList<ArrayList<ImageInfo>> infos);
    void changeOriginalImageProgressBarVisibility(int visibility);
    void changeThumbnailListProgressBarVisibility(int visibility);
    boolean isReadyToUpdate();
}
