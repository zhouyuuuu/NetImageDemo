package com.example.administrator.netimageapplication.imagedisplayer;

import android.graphics.Bitmap;
import android.support.annotation.NonNull;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;

import com.example.administrator.netimageapplication.Bean.ImageCache;
import com.example.administrator.netimageapplication.Bean.ImageInfo;
import com.example.administrator.netimageapplication.R;
import com.example.administrator.netimageapplication.application.NetImageApplication;

import java.util.ArrayList;
import java.util.List;

/**
 * Edited by Administrator on 2018/3/14.
 */

public class NetImageAdapter extends RecyclerView.Adapter<NetImageAdapter.ImageViewHolder> {
    // 展示的图片数据
    private final ArrayList<ImageInfo> mDisplayingImageInfos;
    // 布局加载器，作为全局变量就不用每次都去调用from方法，降低ViewHolder创建效率
    private LayoutInflater mLayoutInflater;
    // Item点击监听器
    private ItemClickListener mItemClickListener;
    // 图片内存缓存
    private ImageCache mImageCache;
    // 所属Activity，需要调该Activity的loadImage方法加载图片
    private NetImageActivity mNetImageActivity;

    NetImageAdapter(ArrayList<ImageInfo> data, ImageCache images, NetImageActivity netImageActivity) {
        // 成员变量初始化
        this.mLayoutInflater = LayoutInflater.from(NetImageApplication.getApplication());
        this.mDisplayingImageInfos = data;
        this.mNetImageActivity = netImageActivity;
        this.mImageCache = images;
    }


    @NonNull
    @Override
    public ImageViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View v;
        // 根据viewType加载不同布局
        if (viewType == ImageInfo.ITEM_TYPE_ITEM) {
            v = mLayoutInflater.inflate(R.layout.view_imagelist_item, parent, false);
            return new ImageViewHolder(v);
        } else {
            v = mLayoutInflater.inflate(R.layout.view_imagelist_subitem, parent, false);
            // 这里需要给view打个标记，用于在TelescopicItemAnimator的动画中区分封面和子项
            v.setTag(TelescopicItemAnimator.ITEM_TYPE_SUB_ITEM);
            return new ImageViewHolder(v);
        }
    }

    @Override
    public void onBindViewHolder(@NonNull final ImageViewHolder holder, int position) {
        ImageInfo imageInfo = mDisplayingImageInfos.get(position);
        // 先从内存缓存中获取
        Bitmap image = mImageCache.getBitmap(imageInfo.getThumbnailUrl());
        // 获取到图片就直接设置就可以，如果没有获取到则先设置一个默认图片，然后调用activity中的loadImage加载图片
        if (image != null) {
            holder.iv.setImageBitmap(image);
        } else {
            holder.iv.setImageResource(R.drawable.bg_gray_round);
            mNetImageActivity.loadImage(holder.iv, imageInfo, mImageCache, true);
        }
        // Item点击事件的监听器在这里触发
        holder.iv.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (mItemClickListener != null) {
                    mItemClickListener.OnItemClick(holder.getAdapterPosition(), holder);
                }
            }
        });
    }

    /**
     * 重写了这个方法，如果payloads不为空，就不重新绑定View了避免Change动画执行覆盖掉其他动画
     */
    @Override
    public void onBindViewHolder(@NonNull ImageViewHolder holder, int position, @NonNull List<Object> payloads) {
        if (payloads.isEmpty()) {
            onBindViewHolder(holder, position);
        }
    }

    @Override
    public int getItemViewType(int position) {
        return mDisplayingImageInfos.get(position).getViewType();
    }

    @Override
    public int getItemCount() {
        return mDisplayingImageInfos == null ? 0 : mDisplayingImageInfos.size();
    }

    void setItemClickListener(ItemClickListener itemClickListener) {
        mItemClickListener = itemClickListener;
    }

    /**
     * Item点击监听器
     */
    public interface ItemClickListener {
        void OnItemClick(int position, ImageViewHolder holder);
    }

    /**
     * ViewHolder，子项和封面都是由一个imageView因此用一类Holder就可以
     */
    static class ImageViewHolder extends RecyclerView.ViewHolder {
        ImageView iv;

        ImageViewHolder(View itemView) {
            super(itemView);
            iv = itemView.findViewById(R.id.iv_image);
        }
    }
}
