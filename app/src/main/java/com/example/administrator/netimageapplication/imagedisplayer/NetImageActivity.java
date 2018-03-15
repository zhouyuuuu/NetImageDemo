package com.example.administrator.netimageapplication.imagedisplayer;

import android.graphics.Bitmap;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.widget.ImageView;
import android.widget.ProgressBar;

import com.example.administrator.netimageapplication.Bean.ImageCache;
import com.example.administrator.netimageapplication.Bean.ImageInfo;
import com.example.administrator.netimageapplication.R;
import com.example.administrator.netimageapplication.imagepresenter.NetImagePresenter;
import com.example.administrator.netimageapplication.util.LogUtil;
import com.example.administrator.netimageapplication.view.PercentProgressBar;

import java.util.ArrayList;

public class NetImageActivity extends AppCompatActivity implements INetImageDisplayer, NetImageAdapter.ItemClickListener {
    private static final int ANIMATOR_INTERVAL_DEFAULT = 200;//默认的动画时间
    private static final int ANIMATOR_REMOVE_DELAY_DEFAULT = (int) (1.5f * ANIMATOR_INTERVAL_DEFAULT);//默认的删除动画滞后时间
    private ImageView mIvOriginalImage;
    private PercentProgressBar mPbLoadOriginalImage;
    private ProgressBar mPbLoadThumbnailList;
    private RecyclerView mRvThumbnailList;
    private LinearLayoutManager mLayoutManager;
    private NetImageAdapter mNetImageAdapter;
    private ImageCache mImageCache;
    private ArrayList<ArrayList<ImageInfo>> mImageInfos;
    private ArrayList<ImageInfo> mDisplayingImageInfos;
    private NetImagePresenter mNetImagePresenter;
    private ArrayList<Integer> mSubItemCountList;
    private TelescopicItemAnimator mTelescopicItemAnimator;
    private boolean mRecyclerViewExecutingAnimation = false;//recyclerView是否正在执行动画

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_netimage);
        initView();
        initData();
        loadThumbnailList();
    }

    private void initView() {
        mPbLoadOriginalImage = findViewById(R.id.pb_original_image);
        mPbLoadThumbnailList = findViewById(R.id.pb_image_info);
        mRvThumbnailList = findViewById(R.id.rv_thumbnail);
        mIvOriginalImage = findViewById(R.id.iv_original_image);
    }

    private void initData() {
        mImageCache = new ImageCache();
        mImageInfos = new ArrayList<>();
        mDisplayingImageInfos = new ArrayList<>();
        mSubItemCountList = new ArrayList<>();
        mTelescopicItemAnimator = new TelescopicItemAnimator();
        mTelescopicItemAnimator.setAddDuration(ANIMATOR_INTERVAL_DEFAULT);
        mTelescopicItemAnimator.setMoveDuration(ANIMATOR_INTERVAL_DEFAULT);
        mTelescopicItemAnimator.setChangeDuration(ANIMATOR_INTERVAL_DEFAULT);
        mTelescopicItemAnimator.setRemoveDuration(ANIMATOR_INTERVAL_DEFAULT);
        mTelescopicItemAnimator.setScreenWidth(this.getWindowManager().getDefaultDisplay().getWidth());
        mNetImagePresenter = new NetImagePresenter(this);
        mLayoutManager = new LinearLayoutManager(this, LinearLayoutManager.HORIZONTAL, false);
        mNetImageAdapter = new NetImageAdapter(mDisplayingImageInfos, mImageCache, this);
        mNetImageAdapter.setItemClickListener(this);
        mRvThumbnailList.setAdapter(mNetImageAdapter);
        mRvThumbnailList.setItemAnimator(mTelescopicItemAnimator);
        mRvThumbnailList.setLayoutManager(mLayoutManager);
        mRvThumbnailList.addOnScrollListener(new RecyclerView.OnScrollListener() {
            @Override
            public void onScrollStateChanged(RecyclerView recyclerView, int newState) {
                if (newState == RecyclerView.SCROLL_STATE_IDLE) {
                    mNetImagePresenter.notifyIsReadyToUpdate();
                }
            }
        });
    }

    @Override
    public void updateOriginalImageProgress(final int percent) {
        LogUtil.e("updateOriginalImageProgress", String.valueOf(percent));
        runOnUiThread(new Runnable() {
            @Override
            public void run() {
                mPbLoadOriginalImage.setPercent(percent);
            }
        });
    }

    private void loadThumbnailList() {
        mNetImagePresenter.loadNetImageInfo();
    }

    public void loadImage(ImageView imageView, ImageInfo imageInfo, ImageCache imageCache, boolean thumbnail) {
        mNetImagePresenter.loadNetImage(imageInfo, imageView, imageCache, thumbnail);
    }

    @Override
    public boolean isReadyToUpdate() {
        return mRvThumbnailList.getScrollState() == RecyclerView.SCROLL_STATE_IDLE;
    }

    @Override
    public void setImageViewBitmap(final ImageView iv, final Bitmap bm) {
        runOnUiThread(new Runnable() {
            @Override
            public void run() {
                iv.setImageBitmap(bm);
            }
        });
    }

    @Override
    public void netImageInfoLoaded(ArrayList<ArrayList<ImageInfo>> infos) {
        mImageInfos = infos;
        for (ArrayList<ImageInfo> group : mImageInfos) {
            mDisplayingImageInfos.add(group.get(0));
            mSubItemCountList.add(0);
        }
        runOnUiThread(new Runnable() {
            @Override
            public void run() {
                mNetImageAdapter.notifyDataSetChanged();
            }
        });
    }

    @Override
    public void changeOriginalImageProgressBarVisibility(final int visibility) {
        runOnUiThread(new Runnable() {
            @Override
            public void run() {
                mPbLoadOriginalImage.setVisibility(visibility);
            }
        });
    }

    @Override
    public void changeThumbnailListProgressBarVisibility(final int visibility) {
        runOnUiThread(new Runnable() {
            @Override
            public void run() {
                mPbLoadThumbnailList.setVisibility(visibility);
            }
        });
    }

    private void animatorStart() {
        mRecyclerViewExecutingAnimation = true;
    }

    private void animatorEnd() {
        mRvThumbnailList.postDelayed(new Runnable() {
            @Override
            public void run() {
                mRecyclerViewExecutingAnimation = false;
            }
        }, ANIMATOR_INTERVAL_DEFAULT);
    }

    @Override
    public void OnItemClick(int position, NetImageAdapter.ImageViewHolder holder) {
        //如果RecyclerView正在执行动画，不执行点击事件以防止数据混乱造成的数组越界
        if (mRecyclerViewExecutingAnimation) return;
        //点击项为子项时加载图片
        if (mDisplayingImageInfos.get(position).getViewType() == ImageInfo.ITEM_TYPE_SUB_ITEM) {
            Bitmap bitmap = mImageCache.getBitmap(mDisplayingImageInfos.get(position).getOriginalImageUrl());
            if (bitmap != null){
                mIvOriginalImage.setImageBitmap(bitmap);
            }else {
                loadImage(mIvOriginalImage, mDisplayingImageInfos.get(position), mImageCache, false);
            }
            return;
        }
        //动画开始
        animatorStart();
        //将被点击的View及其位置传递给ItemAnimator
        mTelescopicItemAnimator.setClickedView(holder.itemView);
        //传入View的中点坐标
        mTelescopicItemAnimator.setClickedX((holder.itemView.getLeft() + holder.itemView.getRight()) / 2);
        //判断该位置在mMarkList中的值，如果是0，则该item没有被展开，如果大于0，该值为该item的子项数目
        if (mSubItemCountList.get(position) == 0) {
            //寻找点击的Item在mData中的位置
            int index = -1;
            for (int i = 0; i <= position; i++) {
                if (mDisplayingImageInfos.get(i).getViewType() == ImageInfo.ITEM_TYPE_ITEM) {
                    index++;
                }
            }
            if (index < 0 || index > mImageInfos.size() - 1) return;
            //将点击Item所属mData的项从第一张图片开始导入到mDataToShow中
            ArrayList<ImageInfo> newData = mImageInfos.get(index);
            for (int i = newData.size() - 1; i > 0; i--) {
                ImageInfo newImageInfo = newData.get(i);
                mDisplayingImageInfos.add(position + 1, newImageInfo);
                //将展开信息同步到mMarkList
                mSubItemCountList.add(position + 1, 0);
            }
            //将position标记为被展开
            mSubItemCountList.set(position, newData.size() - 1);
            //执行该函数来触发Add动画
            mNetImageAdapter.notifyItemRangeInserted(position + 1, newData.size() - 1);
            //该操作用于更新RecyclerView的position，因为Add和Remove后RecyclerView中item的position没有自动更新，引起错乱
            mNetImageAdapter.notifyItemRangeChanged(position + newData.size(), mSubItemCountList.size() - 1 - (position + newData.size() - 1), 0);
            //被点击项滑动至最左边
            mLayoutManager.scrollToPositionWithOffset(position, 0);
            //检查是否有其他的项被展开，有则记录下被展开的子项数目以及该展开项的position
            boolean existExtendedItem = false;
            int lastExtendedPosition = -1;
            int lastExtendedSubItemCount = 0;
            for (int i = 0; i < mSubItemCountList.size(); i++) {
                if (i != position && mSubItemCountList.get(i) != 0) {
                    existExtendedItem = true;
                    lastExtendedPosition = i;
                    lastExtendedSubItemCount = mSubItemCountList.get(i);
                }
            }
            //如果存在被展开的其他项，则在添加动画完成之后，收起子项并执行删除动画
            if (existExtendedItem) {
                //设置存在被展开的其他项
                mTelescopicItemAnimator.setExistExtendedItem(true);
                final int finalSubItemCount = lastExtendedSubItemCount;//要收起的子项数目
                final int finalExtendedItemPosition = lastExtendedPosition;//要收起的项的位置
                holder.itemView.postDelayed(new Runnable() {
                    @Override
                    public void run() {
                        mSubItemCountList.set(finalExtendedItemPosition, 0);
                        for (int i = 0; i < finalSubItemCount; i++) {
                            //删除数据
                            mDisplayingImageInfos.remove(finalExtendedItemPosition + 1);
                            //列表同步
                            mSubItemCountList.remove(finalExtendedItemPosition + 1);
                        }
                        //执行该函数来触发Remove动画
                        mNetImageAdapter.notifyItemRangeRemoved(finalExtendedItemPosition + 1, finalSubItemCount);
                        mNetImageAdapter.notifyItemRangeChanged(finalExtendedItemPosition + finalSubItemCount + 1, mDisplayingImageInfos.size() - finalExtendedItemPosition - 1, 0);
                        //动画结束
                        animatorEnd();
                    }
                }, ANIMATOR_REMOVE_DELAY_DEFAULT);
            } else {
                //动画结束
                animatorEnd();
            }

        } else {
            //设置不存在被展开的其他项
            mTelescopicItemAnimator.setExistExtendedItem(false);
            //拿到position对应的子项数目
            int size = mSubItemCountList.get(position);
            //重新设置为没有被展开
            mSubItemCountList.set(position, 0);
            //删除子项，并同步到mMarkList
            for (int i = position + size; i > position; i--) {
                mDisplayingImageInfos.remove(i);
                mSubItemCountList.remove(i);
            }
            //执行该函数来触发Remove动画
            mNetImageAdapter.notifyItemRangeRemoved(position + 1, size);
            //该操作用于更新RecyclerView的position，因为Add和Remove后RecyclerView中item的position没有自动更新，引起错乱
            mNetImageAdapter.notifyItemRangeChanged(position + 1, mDisplayingImageInfos.size() - position - 1, 0);
            //被点击项滑动至最左边
            mLayoutManager.scrollToPositionWithOffset(position, 0);
            //动画结束
            animatorEnd();
        }
    }
}
