package com.example.administrator.netimageapplication.bean;

/**
 * Edited by Administrator on 2018/3/14.
 */

public class ImageInfo {
    // 封面
    public static final int ITEM_TYPE_ITEM = 101;
    // 子项
    public static final int ITEM_TYPE_SUB_ITEM = 102;
    // 在RecyclerView中用于区分封面和子项
    private int mViewType;
    // 缩略图URL
    private String mThumbnailUrl;
    // 原图URL
    private String mOriginalImageUrl;

    public ImageInfo(int mViewType, String mThumbnailUrl, String mOriginalImageUrl) {
        this.mViewType = mViewType;
        this.mThumbnailUrl = mThumbnailUrl;
        this.mOriginalImageUrl = mOriginalImageUrl;
    }

    public int getViewType() {
        return mViewType;
    }

    public String getThumbnailUrl() {
        return mThumbnailUrl;
    }

    public String getOriginalImageUrl() {
        return mOriginalImageUrl;
    }

}
