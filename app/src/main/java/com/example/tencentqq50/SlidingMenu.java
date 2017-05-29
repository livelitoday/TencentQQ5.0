package com.example.tencentqq50;

import android.content.Context;
import android.content.res.TypedArray;
import android.graphics.Color;
import android.icu.text.LocaleDisplayNames;
import android.util.AttributeSet;
import android.util.DisplayMetrics;
import android.util.Log;
import android.util.TypedValue;
import android.view.GestureDetector;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.widget.FrameLayout;
import android.widget.HorizontalScrollView;
import android.widget.ImageView;

import static android.icu.lang.UCharacter.GraphemeClusterBreak.V;

/**
 * Created by zhiyuan on 2017/5/26.
 * 自定义侧滑栏View
 */

public class SlidingMenu extends HorizontalScrollView {
    final String TAG = this.getClass().getSimpleName();
    //菜单View--菜单布局
    private View mMenuView;
    //默认菜单的宽度
    private int mMenuWidth;
    //内容View
    //private View mContentView;
    private GestureDetector mGestureDetector;
    //
    public boolean isMenuOpen = false;
    private ImageView mShadowView;

    public SlidingMenu(Context context) {
        //在代码中new的时候调用
        this(context, null);
    }

    public SlidingMenu(Context context, AttributeSet attrs) {
        //写在布局文件中的时候调用
        this(context, attrs, 0);
    }

    public SlidingMenu(Context context, AttributeSet attrs, int defStyleAttr) {
        //写在布局文件中的时候调用，但是会有style
        super(context, attrs, defStyleAttr);
        TypedArray array = context.obtainStyledAttributes(attrs, R.styleable.SlidingMenu);
        //获取自定义属性
        Log.d(TAG, "SlidingMenu: " + defStyleAttr);
        float rightPadding = array.getDimension(R.styleable.SlidingMenu_rightPadding, dip2px(defStyleAttr));
        mMenuWidth = (int) (getScreenWidth() - rightPadding);
        array.recycle();
        //处理手势动作
        mGestureDetector = new GestureDetector(context, new GestureListener());
    }

    //用java代码动态设置布局宽度
    //整个布局加载完毕执行onFinishInflate方法
    @Override
    protected void onFinishInflate() {
        super.onFinishInflate();
        //获取菜单和内容的View
        //(1)先获取根部局(容器)：LinearLayout
        ViewGroup rootContainer = (ViewGroup) getChildAt(0);
        //(2)获取菜单View--菜单布局
        mMenuView = rootContainer.getChildAt(0);
        //(3)获取内容View
        //把原来的内容从根部局里面移除，实现侧滑时内容视图的阴影效果

        View oldContentView = rootContainer.getChildAt(1);
        rootContainer.removeView(oldContentView);
        //新建一个布局容器=原来的内容+阴影
        FrameLayout newContentView = new FrameLayout(getContext());
        newContentView.addView(oldContentView);
        mShadowView = new ImageView(getContext());
        mShadowView.setBackgroundColor(Color.parseColor("#99000000"));
        newContentView.addView(mShadowView);
        //重新将包含阴影效果的视图加入到容器中
        rootContainer.addView(newContentView, 1);

        //指定菜单栏和内容的宽度
        //(1)菜单宽度=屏幕宽度- rightPadding   用户自定义(自定义属性)
        mMenuView.getLayoutParams().width = mMenuWidth;
        //(2)内容宽度=屏幕宽度
        newContentView.getLayoutParams().width = getScreenWidth();

        //默认打开时是关闭的，向左滚动菜单的宽度
        //处理内容的阴影效果,滑动到不同的位置显示不同的透明度，需要监听滑动到的位置

    }

    //摆放子View
    @Override
    protected void onLayout(boolean changed, int l, int t, int r, int b) {
        super.onLayout(changed, l, t, r, b);
        if (changed) {
            //向左滚动菜单的宽度
            scrollTo(mMenuWidth, 0);
        }
    }

    /**
     * dip转换为px
     *
     * @param dip
     * @return
     */
    private int dip2px(int dip) {
        //获取手机屏幕分辨率  px=dp*160

        return (int) TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, dip, getResources().getDisplayMetrics());
    }

    /**
     * 获取屏幕宽度
     *
     * @return
     */
    public int getScreenWidth() {
        DisplayMetrics metrics = getResources().getDisplayMetrics();
        return metrics.widthPixels;
    }

    /**
     * onTouch事件处理，根据滑动距离确定滑动到那一边
     * 滑动距离 < mContentWidth 关闭侧滑栏
     * 滑动距离 >= mContentWidth 打开侧滑栏状态
     *
     * @param ev
     * @return
     */
    @Override
    public boolean onTouchEvent(MotionEvent ev) {
        //处理手指快速滑动
        if (mGestureDetector.onTouchEvent(ev)) {
            return false;
        }
        switch (ev.getAction()) {
            case MotionEvent.ACTION_UP:
                int currentScrollX = getScrollX();//当前滚动距离
                Log.d(TAG, "onTouchEvent: getScrollX() " + currentScrollX);
                Log.d(TAG, "onTouchEvent: mMenuWidth/2-->" + (mMenuWidth / 2));
                if (currentScrollX > mMenuWidth / 2) {
                    //到菜单打开状态
                    closeMenu();
                } else {
                    //到菜单关闭状态
                    openMenu();
                }
                return false;
        }
        return super.onTouchEvent(ev);
    }

    /**
     * 菜单关闭状态
     */
    private void closeMenu() {
        smoothScrollTo(mMenuWidth, 0);
        isMenuOpen = false;
    }

    /**
     * 菜单打开状态
     */
    private void openMenu() {
        smoothScrollTo(0, 0);
        isMenuOpen = true;
    }

    /**
     * 手势处理类的回调,只需要复写快速滑动函数
     * 处理手指快速滑动的使用拦截
     */
    class GestureListener extends GestureDetector.SimpleOnGestureListener {

        //监听快速滑动事件
        @Override
        public boolean onFling(MotionEvent e1, MotionEvent e2, float velocityX, float velocityY) {
            Log.d(TAG, "onFling: " + velocityX);
            //向右滑动-->正数
            //向左滑动-->负数
            /*菜单打开，向左快速滑动就会关闭菜单
            * 菜单关闭，向右快速滑动就会打开菜单
            * */
            //判断手势的动作类型，如果是纵向的滑动应该不能相应菜单的切换
            if(Math.abs(velocityX) < Math.abs(velocityY)){
                return false;
            }
            if (velocityX > 0 && !isMenuOpen) {
                //打开菜单
                toggleMenu();
                return true;
            }
            if (velocityX < 0 && isMenuOpen) {
                //关闭菜单
                toggleMenu();
                return true;
            }

            return super.onFling(e1, e2, velocityX, velocityY);
        }
    }

    /**
     * 切换菜单的状态
     */
    public void toggleMenu() {
        if (isMenuOpen) {
            closeMenu();
        } else {
            openMenu();
        }

    }

    /**
     * 滚动状态改变，时不断回调的方法
     *
     * @param l    当前滚动的距离 scrollX
     * @param t
     * @param oldl
     * @param oldt
     */
    @Override
    protected void onScrollChanged(int l, int t, int oldl, int oldt) {
        //Log.d(TAG, "onScrollChanged: " + l);
        //实现抽屉效果
        mMenuView.setTranslationX(l * 0.8f);
        //滑动到不同的位置设置不同的透明度，设置梯度值
        float scale = l * 1f / mMenuWidth;
        mShadowView.setAlpha(1 - scale);//
        super.onScrollChanged(l, t, oldl, oldt);
    }

    //控制事件的分发
    @Override
    public boolean dispatchTouchEvent(MotionEvent ev) {

        int figureX = (int) ev.getX();
        if (isMenuOpen && figureX > mMenuWidth) {//手指按下位置在右部
            toggleMenu();
            //停止事件分发
            return false;
        }
        return super.dispatchTouchEvent(ev);
    }

    //事件的拦截
    @Override
    public boolean onInterceptTouchEvent(MotionEvent ev) {
        return super.onInterceptTouchEvent(ev);
    }

}
