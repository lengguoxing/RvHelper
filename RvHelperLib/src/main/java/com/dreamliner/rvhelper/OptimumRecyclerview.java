package com.dreamliner.rvhelper;

import android.content.Context;
import android.content.res.TypedArray;
import android.support.v7.widget.RecyclerView;
import android.util.AttributeSet;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewStub;
import android.widget.FrameLayout;

import com.dreamliner.loadmore.LoadMoreRecycleViewContainer;
import com.dreamliner.ptrlib.PtrClassicFrameLayout;
import com.dreamliner.ptrlib.PtrDefaultHandler;
import com.dreamliner.ptrlib.PtrFrameLayout;
import com.dreamliner.ptrlib.PtrHandler;
import com.dreamliner.rvhelper.interfaces.OnMoreListener;
import com.dreamliner.rvhelper.interfaces.OnRefreshListener;
import com.dreamliner.rvhelper.util.FloatUtil;

public class OptimumRecyclerview extends FrameLayout {

    protected RecyclerView mRecyclerView;

    protected ViewStub mEmptyViewStub;
    protected View mEmptyView;

    protected ViewStub mLoadingViewStub;
    protected View mLoadingView;

    protected boolean mClipToPadding;
    protected int mPadding;
    protected int mPaddingTop;
    protected int mPaddingBottom;
    protected int mPaddingLeft;
    protected int mPaddingRight;
    protected int mScrollbarStyle;
    protected int mEmptyId;
    protected int mMoreProgressId;

    protected OnRefreshListener mOnRefreshListener;
    protected OnMoreListener mOnMoreListener;
    protected PtrClassicFrameLayout mPtrLayout;
    protected LoadMoreRecycleViewContainer mLoadMoreRecycleViewContainer;

    protected int mSuperRecyclerViewMainLayout;

    //下拉刷新的头部相关信息
    private int mPtrBgColor;
    private int mDurationToClose;
    private int mDurationToCloseHeader;
    private boolean mKeepHeaderWhenREfresh;
    private boolean mPullToFresh;
    private float mRatioOfHedaerHeightToRefresh;
    private float mResistance;

    public PtrClassicFrameLayout getPtrLayout() {
        return mPtrLayout;
    }

    public RecyclerView getRecyclerView() {
        return mRecyclerView;
    }

    public OptimumRecyclerview(Context context) {
        super(context);
        initView();
    }

    public OptimumRecyclerview(Context context, AttributeSet attrs) {
        super(context, attrs);
        initAttrs(attrs);
        initView();
    }

    public OptimumRecyclerview(Context context, AttributeSet attrs, int defStyle) {
        super(context, attrs, defStyle);
        initAttrs(attrs);
        initView();
    }

    protected void initAttrs(AttributeSet attrs) {
        TypedArray optimumRvArr = getContext().obtainStyledAttributes(attrs, R.styleable.superrecyclerview);
        TypedArray ptrArr = getContext().obtainStyledAttributes(attrs, R.styleable.PtrFrameLayout);

        try {
            //初始化rv相关
            mSuperRecyclerViewMainLayout = R.layout.layout_rvhelper_verticalscroll;

            mClipToPadding = optimumRvArr.getBoolean(R.styleable.superrecyclerview_recyclerClipToPadding, false);
            mPadding = (int) optimumRvArr.getDimension(R.styleable.superrecyclerview_recyclerPadding, -1.0f);
            mPaddingTop = (int) optimumRvArr.getDimension(R.styleable.superrecyclerview_recyclerPaddingTop, 0.0f);
            mPaddingBottom = (int) optimumRvArr.getDimension(R.styleable.superrecyclerview_recyclerPaddingBottom, 0.0f);
            mPaddingLeft = (int) optimumRvArr.getDimension(R.styleable.superrecyclerview_recyclerPaddingLeft, 0.0f);
            mPaddingRight = (int) optimumRvArr.getDimension(R.styleable.superrecyclerview_recyclerPaddingRight, 0.0f);
            mScrollbarStyle = optimumRvArr.getInt(R.styleable.superrecyclerview_scrollbarStyle, -1);
            mEmptyId = optimumRvArr.getResourceId(R.styleable.superrecyclerview_layout_empty, 0);
            mMoreProgressId = optimumRvArr.getResourceId(R.styleable.superrecyclerview_layout_moreProgress,
                    R.layout.layout_more_progress);

            //初始化uptr相关
            mPtrBgColor = ptrArr.getInt(R.styleable.PtrFrameLayout_ptr_bg_color, 0xf1f1f1);
            mDurationToClose = ptrArr.getInt(R.styleable.PtrFrameLayout_ptr_duration_to_close, 200);
            mDurationToCloseHeader = ptrArr.getInt(R.styleable.PtrFrameLayout_ptr_duration_to_close_header, 1000);
            mKeepHeaderWhenREfresh = ptrArr.getBoolean(R.styleable.PtrFrameLayout_ptr_duration_to_close, true);
            mPullToFresh = ptrArr.getBoolean(R.styleable.PtrFrameLayout_ptr_duration_to_close, false);
            mRatioOfHedaerHeightToRefresh = ptrArr.getFloat(R.styleable.PtrFrameLayout_ptr_ratio_of_header_height_to_refresh, 1.2f);
            mResistance = ptrArr.getFloat(R.styleable.PtrFrameLayout_ptr_resistance, 1.7f);
        } finally {
            optimumRvArr.recycle();
            ptrArr.recycle();
        }
    }

    private void initView() {
        if (isInEditMode()) {
            return;
        }
        View v = LayoutInflater.from(getContext()).inflate(mSuperRecyclerViewMainLayout, this);

        mEmptyViewStub = (ViewStub) v.findViewById(R.id.empty_viewstub);
        mEmptyViewStub.setLayoutResource(mEmptyId);
        if (mEmptyId != 0)
            mEmptyView = mEmptyViewStub.inflate();
        mEmptyViewStub.setVisibility(View.GONE);

        inituptrView(v);
        initRecyclerView(v);
    }

    private void inituptrView(View v) {
        mPtrLayout = (PtrClassicFrameLayout) v.findViewById(R.id.ptr_layout);
        mPtrLayout.setEnabled(false);

        mPtrLayout.setBackgroundColor(mPtrBgColor);
        mPtrLayout.setDurationToClose(mDurationToClose);
        mPtrLayout.setDurationToCloseHeader(mDurationToCloseHeader);
        mPtrLayout.setKeepHeaderWhenRefresh(mKeepHeaderWhenREfresh);
        mPtrLayout.setPullToRefresh(mPullToFresh);
        mPtrLayout.setRatioOfHeaderHeightToRefresh(mRatioOfHedaerHeightToRefresh);
        mPtrLayout.setResistance(mResistance);

        mPtrLayout.setLastUpdateTimeRelateObject(this);
    }

    /**
     * Implement this method to customize the AbsListView
     */
    protected void initRecyclerView(View view) {
        View recyclerView = view.findViewById(android.R.id.list);

        if (recyclerView instanceof RecyclerView)
            mRecyclerView = (RecyclerView) recyclerView;
        else
            throw new IllegalArgumentException("SuperRecyclerView works with a RecyclerView!");


        mRecyclerView.setClipToPadding(mClipToPadding);

        if (!FloatUtil.compareFloats(mPadding, -1.0f)) {
            mRecyclerView.setPadding(mPadding, mPadding, mPadding, mPadding);
        } else {
            mRecyclerView.setPadding(mPaddingLeft, mPaddingTop, mPaddingRight, mPaddingBottom);
        }

        if (mScrollbarStyle != -1) {
            mRecyclerView.setScrollBarStyle(mScrollbarStyle);
        }
        mRecyclerView.setNestedScrollingEnabled(false);
    }

    /**
     * @param adapter                       The new adapter to set, or null to set no adapter
     * @param compatibleWithPrevious        Should be set to true if new adapter uses the same {@android.support.v7.widget
     *                                      .RecyclerView.ViewHolder}
     *                                      as previous one
     * @param removeAndRecycleExistingViews If set to true, RecyclerView will recycle all existing Views. If adapters
     *                                      have stable ids and/or you want to animate the disappearing views, you may
     *                                      prefer to set this to false
     */
    private void setAdapterInternal(RecyclerView.Adapter adapter, boolean compatibleWithPrevious,
                                    boolean removeAndRecycleExistingViews) {
        if (compatibleWithPrevious)
            mRecyclerView.swapAdapter(adapter, removeAndRecycleExistingViews);
        else
            mRecyclerView.setAdapter(adapter);

        //mProgress.setVisibility(View.GONE);
        mRecyclerView.setVisibility(View.VISIBLE);

        // TODO: 2016/6/12 默认关闭下拉刷新
        //mPtrLayout.setRefreshing(false);
        mPtrLayout.refreshComplete();
        if (null != adapter)
            adapter.registerAdapterDataObserver(new RecyclerView.AdapterDataObserver() {
                @Override
                public void onItemRangeChanged(int positionStart, int itemCount) {
                    super.onItemRangeChanged(positionStart, itemCount);
                    update();
                }

                @Override
                public void onItemRangeInserted(int positionStart, int itemCount) {
                    super.onItemRangeInserted(positionStart, itemCount);
                    update();
                }

                @Override
                public void onItemRangeRemoved(int positionStart, int itemCount) {
                    super.onItemRangeRemoved(positionStart, itemCount);
                    update();
                }

                @Override
                public void onItemRangeMoved(int fromPosition, int toPosition, int itemCount) {
                    super.onItemRangeMoved(fromPosition, toPosition, itemCount);
                    update();
                }

                @Override
                public void onChanged() {
                    super.onChanged();
                    update();
                }

                private void update() {
                    // TODO: 2016/6/12  数据更新之后.禁用一下刷新.
                    //mPtrLayout.setRefreshing(false);
                    mPtrLayout.refreshComplete();
                    if (mRecyclerView.getAdapter().getItemCount() == 0 && mEmptyId != 0) {
                        mEmptyViewStub.setVisibility(View.VISIBLE);
                    } else if (mEmptyId != 0) {
                        mEmptyViewStub.setVisibility(View.GONE);
                    }
                }
            });

        if (mEmptyId != 0) {
            mEmptyViewStub.setVisibility(null != adapter && adapter.getItemCount() > 0
                    ? View.GONE
                    : View.VISIBLE);
        }
    }

    /**
     * Set the layout manager to the recycler
     */
    public void setLayoutManager(RecyclerView.LayoutManager manager) {
        mRecyclerView.setLayoutManager(manager);
    }

    /**
     * Set the adapter to the recycler
     * Automatically hide the progressbar
     * Set the refresh to false
     * If adapter is empty, then the emptyview is shown
     */
    public void setAdapter(RecyclerView.Adapter adapter) {
        setAdapterInternal(adapter, false, true);
    }

    /**
     * @param adapter                       The new adapter to , or null to set no adapter.
     * @param removeAndRecycleExistingViews If set to true, RecyclerView will recycle all existing Views. If adapters
     *                                      have stable ids and/or you want to animate the disappearing views, you may
     *                                      prefer to set this to false.
     */
    public void swapAdapter(RecyclerView.Adapter adapter, boolean removeAndRecycleExistingViews) {
        setAdapterInternal(adapter, true, removeAndRecycleExistingViews);
    }

    /**
     * Remove the adapter from the recycler
     */
    public void clear() {
        mRecyclerView.setAdapter(null);
    }

    /**
     * Show the progressbar
     */
    public void showProgress() {
        hideRecycler();
        if (mEmptyId != 0) mEmptyViewStub.setVisibility(View.INVISIBLE);
    }

    /**
     * Hide the progressbar and show the recycler
     */
    public void showRecycler() {
        if (mRecyclerView.getAdapter().getItemCount() == 0 && mEmptyId != 0) {
            mEmptyViewStub.setVisibility(View.VISIBLE);
        } else if (mEmptyId != 0) {
            mEmptyViewStub.setVisibility(View.GONE);
        }
        mRecyclerView.setVisibility(View.VISIBLE);
    }

    /**
     * Hide the recycler
     */
    public void hideRecycler() {
        mRecyclerView.setVisibility(View.GONE);
    }

    /**
     * Add the onItemTouchListener for the recycler
     */
    public void addOnItemTouchListener(RecyclerView.OnItemTouchListener listener) {
        mRecyclerView.addOnItemTouchListener(listener);
    }

    /**
     * Remove the onItemTouchListener for the recycler
     */
    public void removeOnItemTouchListener(RecyclerView.OnItemTouchListener listener) {
        mRecyclerView.removeOnItemTouchListener(listener);
    }

    /**
     * @return the recycler adapter
     */
    public RecyclerView.Adapter getAdapter() {
        return mRecyclerView.getAdapter();
    }

    /**
     * Sets the onRefresh listener
     */
    public void setRefreshListener(OnRefreshListener onRefreshListener) {
        mPtrLayout.setEnabled(true);
        this.mOnRefreshListener = onRefreshListener;

        mPtrLayout.setPtrHandler(new PtrHandler() {
            @Override
            public void onRefreshBegin(PtrFrameLayout frame) {
                if (null != mOnRefreshListener) {
                    mOnRefreshListener.onRefresh(frame);
                }
            }

            @Override
            public boolean checkCanDoRefresh(PtrFrameLayout frame, View content, View header) {
                return PtrDefaultHandler.checkContentCanBePulledDown(frame, content, header);
            }
        });
    }

    /**
     * Sets the More listener
     *
     * @param max Number of items before loading more
     */
    public void setupMoreListener(OnMoreListener onMoreListener, int max) {
        mOnMoreListener = onMoreListener;
        mLoadMoreRecycleViewContainer.setItemLeftToLoadMore(max);
    }

    public void setOnMoreListener(OnMoreListener onMoreListener) {
        mOnMoreListener = onMoreListener;
    }

    public void setNumberBeforeMoreIsCalled(int max) {
        mLoadMoreRecycleViewContainer.setItemLeftToLoadMore(max);
    }

    public void setOnTouchListener(OnTouchListener listener) {
        mRecyclerView.setOnTouchListener(listener);
    }

    public void addItemDecoration(RecyclerView.ItemDecoration itemDecoration) {
        mRecyclerView.addItemDecoration(itemDecoration);
    }

    public void addItemDecoration(RecyclerView.ItemDecoration itemDecoration, int index) {
        mRecyclerView.addItemDecoration(itemDecoration, index);
    }

    public void removeItemDecoration(RecyclerView.ItemDecoration itemDecoration) {
        mRecyclerView.removeItemDecoration(itemDecoration);
    }

    /**
     * @return inflated progress view or null
     */
    public View getLoadingView() {
        return mLoadingView;
    }

    /**
     * @return inflated empty view or null
     */
    public View getEmptyView() {
        return mEmptyView;
    }

    /**
     * Animate a scroll by the given amount of pixels along either axis.
     *
     * @param dx Pixels to scroll horizontally
     * @param dy Pixels to scroll vertically
     */
    public void smoothScrollBy(int dx, int dy) {
        mRecyclerView.smoothScrollBy(dx, dy);
    }
}
