package com.example.administrator.netimageapplication.Bean;

/**
 * Edited by Administrator on 2018/3/14.
 */

public class ImageInfo {
    public static final int ITEM_TYPE_ITEM = 101;
    public static final int ITEM_TYPE_SUB_ITEM = 102;
    private int mViewType;
    private java.lang.String mThumbnailUrl;
    private java.lang.String mOriginalImageUrl;

    public ImageInfo(int mViewType, java.lang.String mThumbnailUrl, java.lang.String mOriginalImageUrl) {
        this.mViewType = mViewType;
        this.mThumbnailUrl = mThumbnailUrl;
        this.mOriginalImageUrl = mOriginalImageUrl;
    }

    public int getViewType() {
        return mViewType;
    }

    public java.lang.String getThumbnailUrl() {
        return mThumbnailUrl;
    }

    public java.lang.String getOriginalImageUrl() {
        return mOriginalImageUrl;
    }

}
