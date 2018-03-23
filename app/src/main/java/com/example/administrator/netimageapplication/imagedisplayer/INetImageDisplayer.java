package com.example.administrator.netimageapplication.imagedisplayer;

import android.graphics.Bitmap;
import android.widget.ImageView;

import com.example.administrator.netimageapplication.bean.ImageInfo;
import com.example.administrator.netimageapplication.view.PercentProgressBar;

import java.util.ArrayList;

/**
 * 图片加载显示器接口
 * Edited by Administrator on 2018/3/14.
 */
public interface INetImageDisplayer {
    /**
     * 更新图片加载进度条
     * @param percent 进度
     * @param percentProgressBar 进度条
     * @param url 路径
     */
    void updateImageLoadingProgress(int percent, PercentProgressBar percentProgressBar, String url);

    /**
     * 设置ImageView的图片
     * @param iv imageView
     * @param bm 位图
     * @param url 路径
     */
    void setImageViewBitmap(ImageView iv, Bitmap bm, String url);

    /**
     * 网络图片数据加载完成回调
     * @param infos 图片数据集
     */
    void netImageInfoLoaded(ArrayList<ArrayList<ImageInfo>> infos);

    /**
     * 改变图片加载进度条是否可见
     * @param percentProgressBar 进度条
     * @param visibility 可见度
     * @param url 路径，用于检验加载进度条显示的是否是该url的加载进度，即是否有绑定关系
     */
    void changeImageProgressBarVisibility(PercentProgressBar percentProgressBar, int visibility, String url);

    /**
     * 改变图片数据加载的进度条是否可见
     * @param visibility 可见度
     */
    void changeImageInfoProgressBarVisibility(int visibility);

    /**
     * 改变图片数据加载的重试按钮可见度
     * @param visibility 可见度
     */
    void setRetryButtonVisibility(int visibility);

    /**
     * 弹出图片加载失败的消息
     */
    void toastImageLoadFailedInfo();
}
