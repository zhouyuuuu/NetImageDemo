package com.example.administrator.netimageapplication.imagedisplayer;

import android.graphics.Bitmap;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.View;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import com.example.administrator.netimageapplication.R;
import com.example.administrator.netimageapplication.application.NetImageApplication;
import com.example.administrator.netimageapplication.bean.ImageCache;
import com.example.administrator.netimageapplication.bean.ImageInfo;
import com.example.administrator.netimageapplication.imagepresenter.NetImagePresenter;
import com.example.administrator.netimageapplication.view.PercentProgressBar;

import java.util.ArrayList;

public class NetImageActivity extends AppCompatActivity implements INetImageDisplayer, NetImageAdapter.ItemClickListener, View.OnClickListener {
    // 图片加载失败
    private static final String INFO_LOAD_FAILED = "有张图片加载失败了~~";
    // 默认的动画时间
    private static final int ANIMATOR_INTERVAL_DEFAULT = 200;
    // 默认的删除动画滞后时间
    private static final int ANIMATOR_REMOVE_DELAY_DEFAULT = (int) (1.5f * ANIMATOR_INTERVAL_DEFAULT);
    // 数据加载失败按钮
    private TextView mTvRetry;
    // 原图大图的ImageView
    private ImageView mIvOriginalImage;
    // 加载原图的进度条
    private PercentProgressBar mPpbLoadOriginalImage;
    // 加载所有图片数据(原图Url和缩略图Url)的进度条
    private ProgressBar mPbLoadImageInfo;
    // 缩略图列表RecyclerView
    private RecyclerView mRvThumbnailList;
    // RecyclerView的布局管理者，必须为水平的线性布局
    private LinearLayoutManager mLayoutManager;
    // RecyclerView的适配器
    private NetImageAdapter mNetImageAdapter;
    // 图片内存缓存管理类
    private ImageCache mImageCache;
    // 所有图片的数据(分组存放，一个ArrayList一组)
    private ArrayList<ArrayList<ImageInfo>> mImageInfos;
    // 在RecyclerView中显示的图片集
    private ArrayList<ImageInfo> mDisplayingImageInfos;
    // Presenter层
    private NetImagePresenter mNetImagePresenter;
    // 对应mDisplayingImageInfos的每一项，其已被展开的子项的数目，方便收缩该项的子项时计算子项数量
    private ArrayList<Integer> mSubItemCountList;
    // RecyclerView伸缩动画
    private TelescopicItemAnimator mTelescopicItemAnimator;
    // recyclerView执行动画的状态，执行中会无视用户的点击事件(添加或删除RecyclerView数据元素)，因为动画过程中增删元素会造成元素position和动画错乱，甚至导致崩溃
    private boolean mRecyclerViewExecutingAnimation = false;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_netimage);
        // 初始化View
        initView();
        // 初始化成员变量
        initData();
        // 开始加载图片数据
        loadImageInfos();
    }

    /**
     * 初始化View
     */
    private void initView() {
        mPpbLoadOriginalImage = findViewById(R.id.pb_original_image);
        mPbLoadImageInfo = findViewById(R.id.pb_image_info);
        mRvThumbnailList = findViewById(R.id.rv_thumbnail);
        mIvOriginalImage = findViewById(R.id.iv_original_image);
        mTvRetry = findViewById(R.id.tv_retry);
    }

    /**
     * 初始化成员变量
     */
    private void initData() {
        mImageCache = new ImageCache();
        mImageInfos = new ArrayList<>();
        mDisplayingImageInfos = new ArrayList<>();
        mSubItemCountList = new ArrayList<>();
        // 伸缩动画需要将各个动作的动画设为相同，否则效果凌乱
        mTelescopicItemAnimator = new TelescopicItemAnimator();
        mTelescopicItemAnimator.setAddDuration(ANIMATOR_INTERVAL_DEFAULT);
        mTelescopicItemAnimator.setMoveDuration(ANIMATOR_INTERVAL_DEFAULT);
        mTelescopicItemAnimator.setChangeDuration(ANIMATOR_INTERVAL_DEFAULT);
        mTelescopicItemAnimator.setRemoveDuration(ANIMATOR_INTERVAL_DEFAULT);
        // 设置屏幕宽度用于删除动画的translation计算
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
                // RecyclerView停止滚动时通知被加载的图片可以设置到ImageView上了，停止时再设置可以保证滑动流畅性
                if (newState == RecyclerView.SCROLL_STATE_IDLE) {
                    mNetImagePresenter.notifyIsReadyToUpdate();
                }
            }
        });
        mTvRetry.setOnClickListener(this);
    }

    /**
     * 图片下载过程中每下载1kb回调该方法，用于更新对应的progressBar的进度
     *
     * @param percent 百分比
     */
    @Override
    public void updateImageLoadingProgress(final int percent, final PercentProgressBar percentProgressBar) {
        runOnUiThread(new Runnable() {
            @Override
            public void run() {
                percentProgressBar.setPercent(percent);
            }
        });
    }

    /**
     * 加载图片数据
     */
    private void loadImageInfos() {
        mNetImagePresenter.loadNetImageInfo();
    }

    /**
     * 加载图片(缩略图或原图)
     *
     * @param imageView  哪个imageView发起的加载请求，在加载完成后就设置在这个imageView上
     * @param imageInfo  图片数据
     * @param imageCache 图片内存缓存，加载完成后将图片缓存到这里
     * @param thumbnail  是否是缩略图，否则是原图
     */
    public void loadImage(ImageView imageView, PercentProgressBar percentProgressBar, ImageInfo imageInfo, ImageCache imageCache, boolean thumbnail) {
        mNetImagePresenter.loadNetImage(imageInfo, percentProgressBar, imageView, imageCache, thumbnail);
    }

    /**
     * 当RecyclerView处于停止滑动的状态时，即可进行图片更新
     *
     * @return 是否可更新
     */
    @Override
    public boolean isReadyToUpdate() {
        return mRvThumbnailList.getScrollState() == RecyclerView.SCROLL_STATE_IDLE;
    }

    /**
     * 设置重试按钮是否可见
     */
    @Override
    public void setRetryButtonVisibility(final int visibility) {
        runOnUiThread(new Runnable() {
            @Override
            public void run() {
                mTvRetry.setVisibility(visibility);
            }
        });
    }

    /**
     * 加载图片失败弹出消息
     */
    @Override
    public void ToastImageLoadFailedInfo() {
        runOnUiThread(new Runnable() {
            @Override
            public void run() {
                Toast.makeText(NetImageApplication.getApplication(), INFO_LOAD_FAILED, Toast.LENGTH_SHORT).show();
            }
        });
    }

    /**
     * 设置ImageView图片，异步回调
     */
    @Override
    public void setImageViewBitmap(final ImageView iv, final Bitmap bm) {
        runOnUiThread(new Runnable() {
            @Override
            public void run() {
                iv.setImageBitmap(bm);
            }
        });
    }

    /**
     * 图片数据加载完成，通知RecyclerView更新
     *
     * @param infos 加载完成的图片数据
     */
    @Override
    public void netImageInfoLoaded(ArrayList<ArrayList<ImageInfo>> infos) {
        mImageInfos = infos;
        for (ArrayList<ImageInfo> group : mImageInfos) {
            // 每一组的第一张图片是封面，一开始只展示封面
            mDisplayingImageInfos.add(group.get(0));
            // 同步到子项数量标记列表
            mSubItemCountList.add(0);
        }
        // 数据获取完成后通知RecyclerView更新
        runOnUiThread(new Runnable() {
            @Override
            public void run() {
                mNetImageAdapter.notifyDataSetChanged();
            }
        });
    }

    /**
     * 设置图片加载进度条是否可见
     *
     * @param visibility 可见度
     */
    @Override
    public void changeImageProgressBarVisibility(final PercentProgressBar percentProgressBar, final int visibility) {
        runOnUiThread(new Runnable() {
            @Override
            public void run() {
                percentProgressBar.setVisibility(visibility);
            }
        });
    }

    /**
     * 设置图片数据加载进度条是否可见
     *
     * @param visibility 可见度
     */
    @Override
    public void changeImageInfoProgressBarVisibility(final int visibility) {
        runOnUiThread(new Runnable() {
            @Override
            public void run() {
                mPbLoadImageInfo.setVisibility(visibility);
            }
        });
    }

    /**
     * RecyclerView动画开始
     */
    private void animatorStart() {
        mRecyclerViewExecutingAnimation = true;
    }

    /**
     * RecyclerView动画结束
     */
    private void animatorEnd() {
        mRvThumbnailList.postDelayed(new Runnable() {
            @Override
            public void run() {
                mRecyclerViewExecutingAnimation = false;
            }
        }, ANIMATOR_INTERVAL_DEFAULT);
    }

    /**
     * RecyclerView的Item点击事件函数
     *
     * @param position Item的下标
     * @param holder   Item对应的holder
     */
    @Override
    public void OnItemClick(int position, NetImageAdapter.ItemHolder holder) {
        // 如果RecyclerView正在执行动画，不执行点击事件以防止数据混乱造成的数组越界
        if (mRecyclerViewExecutingAnimation) return;
        // 点击项为子项时加载原图
        if (mDisplayingImageInfos.get(position).getViewType() == ImageInfo.ITEM_TYPE_SUB_ITEM) {
            // 先在内存缓存中找图片，找不到则去加载图片
            Bitmap bitmap = mImageCache.getBitmap(mDisplayingImageInfos.get(position).getOriginalImageUrl());
            if (bitmap != null) {
                mIvOriginalImage.setImageBitmap(bitmap);
            } else {
                loadImage(mIvOriginalImage, mPpbLoadOriginalImage, mDisplayingImageInfos.get(position), mImageCache, false);
            }
            return;
        }
        // 动画开始
        animatorStart();
        // 将被点击的View及其位置传递给ItemAnimator
        mTelescopicItemAnimator.setClickedView(holder.itemView);
        // 传入View的中点坐标
        mTelescopicItemAnimator.setClickedX((holder.itemView.getLeft() + holder.itemView.getRight()) / 2);
        // 判断该位置在mMarkList中的值，如果是0，则该item没有展开的子项，如果大于0，该值为该item的子项数目，该项需要被收缩
        if (mSubItemCountList.get(position) == 0) {
            // 寻找点击的Item在mData中的位置，在mDisplayingImageInfos中顺序计数，只要计数时忽略掉子项即是所求
            int index = -1;
            for (int i = 0; i <= position; i++) {
                if (mDisplayingImageInfos.get(i).getViewType() == ImageInfo.ITEM_TYPE_ITEM) {
                    index++;
                }
            }
            // 将点击Item所属mData的项从第一张图片开始导入到mDataToShow中
            ArrayList<ImageInfo> newData = mImageInfos.get(index);
            for (int i = newData.size() - 1; i > 0; i--) {
                mDisplayingImageInfos.add(position + 1, newData.get(i));
                // mSubItemCountList记录展开信息
                mSubItemCountList.add(position + 1, 0);
            }
            // 标记已展开子项数目
            mSubItemCountList.set(position, newData.size() - 1);
            // 执行该函数来触发Add动画
            mNetImageAdapter.notifyItemRangeInserted(position + 1, newData.size() - 1);
            // 该操作用于更新RecyclerView的position，因为Add和Remove后RecyclerView中item的position没有自动更新，引起错乱
            mNetImageAdapter.notifyItemRangeChanged(position + newData.size(), mSubItemCountList.size() - 1 - (position + newData.size() - 1), 0);
            // 被点击项滑动至最左边
            mLayoutManager.scrollToPositionWithOffset(position, 0);
            // 检查是否有其他的项被展开，有则记录下被展开的子项数目以及该展开项的position
            boolean existExtendedItem = false;
            int lastExtendedPosition = -1;
            int lastExtendedSubItemCount = 0;
            // 在mSubItemCountList中找到position不是所点击的position但值不为0，则说明该项的子项被展开，此项需要收缩
            for (int i = 0; i < mSubItemCountList.size(); i++) {
                if (i != position && mSubItemCountList.get(i) != 0) {
                    existExtendedItem = true;
                    lastExtendedPosition = i;
                    lastExtendedSubItemCount = mSubItemCountList.get(i);
                }
            }
            // 如果存在被展开的其他项，则在添加动画完成之后，移除子项并执行删除动画
            if (existExtendedItem) {
                // TelescopicItemAnimator需要设置存在被展开的其他项，收起点击项和收起非点击项的动画不一样，由此标志区分
                mTelescopicItemAnimator.setExistExtendedItem(true);
                // 要收起的子项数目
                final int finalSubItemCount = lastExtendedSubItemCount;
                // 要收起的项的位置
                final int finalExtendedItemPosition = lastExtendedPosition;
                // 在Delay时间后执行删除操作避免与添加操作重叠引起动画错乱
                holder.itemView.postDelayed(new Runnable() {
                    @Override
                    public void run() {
                        mSubItemCountList.set(finalExtendedItemPosition, 0);
                        for (int i = 0; i < finalSubItemCount; i++) {
                            // 删除数据
                            mDisplayingImageInfos.remove(finalExtendedItemPosition + 1);
                            // 列表同步
                            mSubItemCountList.remove(finalExtendedItemPosition + 1);
                        }
                        // 执行该函数来触发Remove动画
                        mNetImageAdapter.notifyItemRangeRemoved(finalExtendedItemPosition + 1, finalSubItemCount);
                        mNetImageAdapter.notifyItemRangeChanged(finalExtendedItemPosition + finalSubItemCount + 1, mDisplayingImageInfos.size() - finalExtendedItemPosition - 1, 0);
                        // 动画结束
                        animatorEnd();
                    }
                }, ANIMATOR_REMOVE_DELAY_DEFAULT);
            } else {
                // 动画结束
                animatorEnd();
            }

        } else {
            // 设置不存在被展开的其他项，收起点击项和收起非点击项的动画不一样，由此标志区分
            mTelescopicItemAnimator.setExistExtendedItem(false);
            // 被展开子项数目
            int size = mSubItemCountList.get(position);
            // 重新设置被展开子项为0
            mSubItemCountList.set(position, 0);
            // 删除子项，并同步到mSubItemCountList
            for (int i = position + size; i > position; i--) {
                mDisplayingImageInfos.remove(i);
                mSubItemCountList.remove(i);
            }
            // 执行该函数来触发Remove动画
            mNetImageAdapter.notifyItemRangeRemoved(position + 1, size);
            // 该操作用于更新RecyclerView的position，因为Add和Remove后RecyclerView中item的position没有自动更新，引起错乱
            mNetImageAdapter.notifyItemRangeChanged(position + 1, mDisplayingImageInfos.size() - position - 1, 0);
            // 被点击项滑动至最左边
            mLayoutManager.scrollToPositionWithOffset(position, 0);
            // 动画结束
            animatorEnd();
        }
    }

    @Override
    protected void onDestroy() {
        mNetImagePresenter.stopLoading();
        super.onDestroy();
    }

    /**
     * 点击重试按钮重新获取图片数据
     */
    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.tv_retry:
                mTvRetry.setVisibility(View.GONE);
                loadImageInfos();
                break;
            default:
                break;
        }
    }
}
