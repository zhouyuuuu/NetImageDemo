package com.example.administrator.netimageapplication.imagedisplayer;

import android.support.annotation.NonNull;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;

import com.example.administrator.netimageapplication.R;
import com.example.administrator.netimageapplication.application.NetImageApplication;
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
    // Item加载监听器
    private ItemLoadListener mItemLoadListener;

    NetImageAdapter(ArrayList<ImageInfo> data) {
        // 成员变量初始化
        this.mLayoutInflater = LayoutInflater.from(NetImageApplication.getApplication());
        this.mDisplayingImageInfos = data;
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
        // 每次都先将控件初始化，避免View复用携带了之前的数据
        holder.ppb.setVisibility(View.GONE);
        holder.iv.setImageResource(R.drawable.bg_gray_round);
        ImageInfo imageInfo = mDisplayingImageInfos.get(position);
        if (mItemLoadListener != null){
            mItemLoadListener.onLoadItem(holder.iv,holder.ppb,imageInfo);
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

    void setItemLoadListener(ItemLoadListener itemLoadListener){
        mItemLoadListener = itemLoadListener;
    }

    /**
     * Item点击监听器
     */
    public interface ItemClickListener {
        void OnItemClick(int position, ItemHolder holder);
    }

    /**
     * Item加载监听器
     */
    public interface ItemLoadListener {
        void onLoadItem(ImageView imageView, PercentProgressBar percentProgressBar, ImageInfo imageInfo);
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
