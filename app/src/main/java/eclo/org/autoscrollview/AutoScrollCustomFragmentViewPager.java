package eclo.org.autoscrollview;

import android.app.Fragment;
import android.app.FragmentManager;
import android.content.Context;
import android.os.Handler;
import android.os.Message;
import android.support.v13.app.FragmentPagerAdapter;
import android.support.v4.view.ViewPager;
import android.util.AttributeSet;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;

import java.lang.ref.WeakReference;
import java.util.ArrayList;
import java.util.List;
import java.util.Timer;
import java.util.TimerTask;

/**
 *  Filename:    AutoScrollCustomFragmentViewPager.java  
 *  Description:  填充自定义fragment的自动左右循环滚动的viewpager
 *  @version:    1.0  
 *  Create at:   2013-8-9 下午4:13:14   
 */
public class AutoScrollCustomFragmentViewPager
        extends RelativeLayout
{

    private final static String TAG = "FragmentViewPager";
    private final static int VIEW_BANNER_CLICK_MAX_LENGTH = 40;
    // page页面总数
    private int mTotal = 0;
    private int mCurIndex;
    private int mLastIndex = 0;
    boolean mIsFirst;
    private Timer mTimer;
    private AutoScrollTimerTask myTimerTask;
    private Handler myHandler;
    private boolean mNeedAuto = true;
    private boolean mCanScroll = true;

    private LinearLayout mIndicatorLayout;
    private ViewPager mViewFlipper;
    /*指示器的单个item*/
    private List<ImageView> mIndicatorList;

    //浮标点背景
    private int mIndicatorBg;
    private int mIndicatorUnselectBg;
    private int mIndicatorLyBg;


    //TimerTask间隔定为500ms
    private static long TIMER_PERIOD = 500L;
    //flipper自动切换间隔为 8 * 500ms
    private static int TIMER_COUNTER = 12;

    private OnChangeListener mChangeListenr;


    /*指示器位置改变事件回掉接口*/
    public interface OnChangeListener {
        /**
         * 指示器位置回调函数
         *
         * @param last    原指示器位置
         * @param current 当前指示器位置
         */
        void onChange(int last, int current);
    }

    public void setOnChangeListener(OnChangeListener listener) {
        mChangeListenr = listener;
    }

    private OnItemClickListener mOnItemClickListener;

    public interface OnItemClickListener {
        public void onItemClick(int index);
    }

    public void setOnItemClickListener(OnItemClickListener listener) {
        mOnItemClickListener = listener;
    }

    public AutoScrollCustomFragmentViewPager(Context ctx) {
        this(ctx, null);
    }

    public AutoScrollCustomFragmentViewPager(Context context, AttributeSet attrs) {
        super(context, attrs);
        this.mCurIndex = 0;
        this.mIsFirst = true;
        LayoutInflater inflater = LayoutInflater.from(getContext());
        inflater.inflate(R.layout.autoscroll_custom_view_banner, this, true);
        mViewFlipper = (ViewPager) findViewById(R.id.auto_scroll_custom_view_banner);
        mIndicatorLayout = (LinearLayout) findViewById(R.id.auto_scroll_view_indicator);
        mIndicatorList = new ArrayList();
    }

    public void setAuto(boolean f) {
        mNeedAuto = f;
    }

    public void setCanScroll(boolean canScroll) {
        mCanScroll = canScroll;
    }

    @Override
    protected void onAttachedToWindow() {
        super.onAttachedToWindow();
        Log.e(TAG, "onAttachedToWindow");
        if (mNeedAuto) {
            startFlipperTimer();
        }
    }

    @Override
    protected void onDetachedFromWindow() {
        super.onDetachedFromWindow();
        Log.e(TAG, "onDetachedFromWindow");
        stopFlipperTimer();
    }

    public void setIndicatorUnselectBg(int mIndicatorUnselectBg) {
        this.mIndicatorUnselectBg = mIndicatorUnselectBg;
    }

    public void setIndicatorImg(int imgRes) {
        mIndicatorBg = imgRes;
    }

    public void setIndicatorLyBg(int bgRes) {
        mIndicatorLyBg = bgRes;
    }


    private IFragmentItem IFragmentItem;


    /*指示器位置改变事件回掉接口*/
    public interface IFragmentItem {
        Fragment getItem(int position);
    }


    class MyFragmentPageAdapter
            extends FragmentPagerAdapter
    {

        public MyFragmentPageAdapter(FragmentManager fm) {
            super(fm);
        }

        @Override
        public Fragment getItem(int position) {
            return IFragmentItem.getItem(position % mTotal);
        }

        @Override
        public int getCount() {

            if (mTotal == 1) {
                return 1;
            } else {
                return Integer.MAX_VALUE;
            }


        }
    }
    //设置点点的距离
    public void setIndicatorMarrginBottom(int marginDp){
        if(null != getContext()){
            RelativeLayout.LayoutParams lp= (RelativeLayout.LayoutParams )mIndicatorLayout.getLayoutParams();
            lp.bottomMargin = ScreenTools.instance(getContext()).dip2px(marginDp);
        }
    }

    MyFragmentPageAdapter pageAdapter;

    public void initData(FragmentManager fragmentManager, int size, IFragmentItem IFragmentItem) {
        if (size <= 0) {
            return;
        }
        this.IFragmentItem = IFragmentItem;
        mViewFlipper.removeAllViews();
        mIndicatorLayout.removeAllViews();
        mIndicatorList.clear();
        mTotal = size;
        pageAdapter = new MyFragmentPageAdapter(fragmentManager);
        mViewFlipper.setAdapter(pageAdapter);
        mViewFlipper.setCurrentItem(0);
        mViewFlipper.setOnPageChangeListener(onPageChangeListener);
        for (int i = 0; i < mTotal; i++) {
            ScreenTools screenTools =
                    ScreenTools.instance(getContext().getApplicationContext());
            ImageView imageView = new ImageView(getContext());
            LinearLayout.LayoutParams params = new LinearLayout.LayoutParams(
                    ViewGroup.LayoutParams.WRAP_CONTENT, ViewGroup.LayoutParams.WRAP_CONTENT);
            params.rightMargin = screenTools.dip2px(8);
            if (mIndicatorUnselectBg > 0) {
                imageView.setImageResource(mIndicatorUnselectBg);
            } else {
                imageView.setImageResource(R.drawable.view_pager_indicator_unselect_point);
            }

            mIndicatorLayout.addView(imageView, params);
            mIndicatorList.add(imageView);
        }

        mIndicatorLayout.setBackgroundResource(mIndicatorLyBg);
        if (mTotal != 1) {
            mIndicatorLayout.setVisibility(View.VISIBLE);
            mViewFlipper.setCurrentItem(mTotal * 50, false);
        } else {
            mCanScroll = false;
            mIndicatorLayout.setVisibility(View.GONE);
        }
        updateIndicator(0, 0);
    }

    static class MyHandler
            extends Handler
    {

        WeakReference<AutoScrollCustomFragmentViewPager> mBanner;

        MyHandler(AutoScrollCustomFragmentViewPager banner) {
            mBanner = new WeakReference<AutoScrollCustomFragmentViewPager>(banner);
        }

        @Override
        public void handleMessage(Message msg) {
            super.handleMessage(msg);
            if (null == mBanner.get()) {
                return;
            }
            if (mBanner.get().mIsFirst != false) {
                mBanner.get().flipperShowNext();
            } else {
                mBanner.get().mIsFirst = true;
            }
        }
    }

    /**
     * 启动MyViewFlipper中的定时器
     */
    public void startFlipperTimer() {
        stopFlipperTimer();
        myHandler = new MyHandler(this);
        this.mTimer = new Timer();
        this.myTimerTask = new AutoScrollTimerTask();
        this.mTimer.scheduleAtFixedRate(myTimerTask, 0, TIMER_PERIOD);
    }


    /**
     * 暂停flipper定时器
     */
    public void stopFlipperTimer() {
        if (this.myTimerTask != null) {
            myTimerTask.mCount = 0;
            this.myTimerTask.cancel();
            this.myTimerTask = null;
        }
        if (this.mTimer != null) {
            this.mTimer.cancel();
            this.mTimer = null;
        }
    }

    /**
     * 重置计数~
     */
    public void resetTimer() {
        if (null != myTimerTask) {
            myTimerTask.mCount = 0;
        }
    }


    ViewPager.OnPageChangeListener onPageChangeListener = new ViewPager.SimpleOnPageChangeListener() {

        @Override
        public void onPageSelected(int arg0) {
            updateIndicator(mCurIndex, arg0);
            mCurIndex = arg0;
            resetTimer();

            if (mChangeListenr != null) {
                mChangeListenr.onChange(mLastIndex % mTotal, arg0 % mTotal);
            }
            mLastIndex = arg0;
        }
    };

    /**
     * 往后翻页
     */
    public void flipperShowNext() {
        if (mTotal == 0) {
            return;
        }
        if (!mCanScroll) {
            return;
        }

        if (mViewFlipper == null) {
            return;
        }

        int old = mCurIndex;
        mCurIndex = mCurIndex + 1;
        try {
            mViewFlipper.setCurrentItem(mCurIndex);
            updateIndicator(old, mCurIndex);
        } catch (Exception e) {

        }
    }

    private void updateIndicator(int old, int now) {
        old %= mTotal;
        now %= mTotal;
        ImageView oldView = mIndicatorList.get(old);
        ImageView curView = mIndicatorList.get(now);
        if (mIndicatorUnselectBg > 0) {
            oldView.setImageResource(mIndicatorUnselectBg);
        } else {
            oldView.setImageResource(R.drawable.view_pager_indicator_unselect_point);
        }

        if (mIndicatorBg > 0) {
            curView.setImageResource(mIndicatorBg);
        } else {
            curView.setImageResource(R.drawable.view_pager_indicator_select_point);
        }
        oldView.setSelected(false);
        curView.setSelected(true);
    }

    public int getTotal() {
        return this.mTotal;
    }

    class AutoScrollTimerTask
            extends TimerTask
    {

        @Override
        public void run() {
            if (this.mCount >= TIMER_COUNTER) {
                myHandler.sendMessage(new Message());
                this.mCount = 0;
            } else {
                this.mCount++;
            }
        }

        public int mCount = 0;
    }

    public int getCurIndex() {
        if (mTotal == 0) {
            return 0;
        }
        return mCurIndex % mTotal;
    }


}
