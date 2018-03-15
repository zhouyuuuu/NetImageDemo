package com.example.administrator.netimageapplication.imagedisplayer;

import android.graphics.Bitmap;
import android.support.annotation.NonNull;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

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
    private LayoutInflater mLayoutInflater;
    private final ArrayList<ImageInfo> mDisplayingImageInfos;
    private ItemClickListener mItemClickListener;
    private ImageCache mImageCache;
    private NetImageActivity mNetImageActivity;

    NetImageAdapter(ArrayList<ImageInfo> data, ImageCache images, NetImageActivity netImageActivity) {
        this.mLayoutInflater = LayoutInflater.from(NetImageApplication.getApplication());
        this.mDisplayingImageInfos = data;
        this.mNetImageActivity = netImageActivity;
        this.mImageCache = images;
    }


    @NonNull
    @Override
    public ImageViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View v;
        if (viewType == ImageInfo.ITEM_TYPE_ITEM){
            v = mLayoutInflater.inflate(R.layout.view_imagelist_item, parent, false);
            return new ImageViewHolder(v);
        }else {
            v = mLayoutInflater.inflate(R.layout.view_imagelist_subitem, parent, false);
            v.setTag(TelescopicItemAnimator.ITEM_TYPE_SUB_ITEM);
            return new ImageViewHolder(v);
        }
    }

    @Override
    public void onBindViewHolder(@NonNull final ImageViewHolder holder, int position) {
        ImageInfo imageInfo = mDisplayingImageInfos.get(position);
        Bitmap image = mImageCache.getBitmap(imageInfo.getThumbnailUrl());
        if (image != null) {
            holder.iv.setImageBitmap(image);
        } else {
            holder.iv.setImageResource(R.drawable.bg_gray_round);
            mNetImageActivity.loadImage(holder.iv,imageInfo,mImageCache,true);
        }
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


    public interface ItemClickListener {
        void OnItemClick(int position, ImageViewHolder holder);
    }

    static class ImageViewHolder extends RecyclerView.ViewHolder {
        TextView tv;
        ImageView iv;

        ImageViewHolder(View itemView) {
            super(itemView);
            iv = itemView.findViewById(R.id.iv_image);
            tv = itemView.findViewById(R.id.tv_image);
        }
    }
}
