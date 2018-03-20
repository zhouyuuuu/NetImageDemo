package com.example.administrator.netimageapplication.imagedisplayer;

import android.graphics.Bitmap;
import android.support.annotation.NonNull;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;

import com.example.administrator.netimageapplication.R;
import com.example.administrator.netimageapplication.application.NetImageApplication;
import com.example.administrator.netimageapplication.bean.ImageCache;
import com.example.administrator.netimageapplication.bean.ImageInfo;
import com.example.administrator.netimageapplication.view.PercentProgressBar;

import java.util.ArrayList;
import java.util.List;

/**
 * Edited by Administrator on 2018/3/14.
 */

public class NetImageAdapter extends RecyclerView.Adapter<NetImageAdapter.ItemHolder> {
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
    public ItemHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View v;
        // 根据viewType加载不同布局
        if (viewType == ImageInfo.ITEM_TYPE_ITEM) {
            v = mLayoutInflater.inflate(R.layout.view_imagelist_item, parent, false);
            return new ItemHolder(v);
        } else {
            v = mLayoutInflater.inflate(R.layout.view_imagelist_subitem, parent, false);
            // 这里需要给view打个标记，用于在TelescopicItemAnimator的动画中区分封面和子项
            v.setTag(TelescopicItemAnimator.ITEM_TYPE_SUB_ITEM);
            return new ItemHolder(v);
        }
    }

    @Override
    public void onBindViewHolder(@NonNull final ItemHolder holder, int position) {
        ImageInfo imageInfo = mDisplayingImageInfos.get(position);
        // 先从内存缓存中获取
        Bitmap image = mImageCache.getBitmap(imageInfo.getThumbnailUrl());
        // 获取到图片就直接设置就可以，如果没有获取到则先设置一个默认图片，然后调用activity中的loadImage加载图片
        if (image != null) {
            holder.iv.setImageBitmap(image);
        } else {
            holder.iv.setImageResource(R.drawable.bg_gray_round);
            if (mNetImageActivity.readyToLoad()) {
                mNetImageActivity.loadImage(holder.iv, holder.ppb, imageInfo, mImageCache, true);
            }
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
    public void onBindViewHolder(@NonNull ItemHolder holder, int position, @NonNull List<Object> payloads) {
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
        void OnItemClick(int position, ItemHolder holder);
    }

    /**
     * 封面和子项ViewHolder
     */
    static class ItemHolder extends RecyclerView.ViewHolder {
        ImageView iv;
        PercentProgressBar ppb;

        ItemHolder(View itemView) {
            super(itemView);
            iv = itemView.findViewById(R.id.iv_image);
            ppb = itemView.findViewById(R.id.ppb_loading);
        }
    }

}
