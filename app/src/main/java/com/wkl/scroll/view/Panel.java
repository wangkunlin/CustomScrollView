package com.wkl.scroll.view;

import android.content.Context;
import android.graphics.Rect;
import android.support.v4.view.ViewConfigurationCompat;
import android.util.AttributeSet;
import android.view.MotionEvent;
import android.view.VelocityTracker;
import android.view.View;
import android.view.ViewConfiguration;
import android.view.ViewGroup;
import android.widget.Scroller;

import com.wkl.scroll.R;

/**
 * Panel.java
 * Author: wangkunlin
 * Date: 2016-01-26
 * Email: 1040057694@qq.com
 */
public class Panel extends ViewGroup {
    public Panel(Context context) {
        this(context, null);
    }

    public Panel(Context context, AttributeSet attrs) {
        this(context, attrs, 0);
    }

    private Scroller scroller;
    private int touchSlop;
    private Rect openRect;
    private Rect closeRect;
    private View first;
    private View second;
    private int lastX;
    private int downX;
    private boolean scroll;
    private boolean canceled;
    private float velocity;
    private boolean isOpen = true;
    private VelocityTracker vt;
    private float maxVelocity;

    public Panel(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        scroller = new Scroller(context);
        ViewConfiguration configuration = ViewConfiguration.get(context);
        maxVelocity = configuration.getScaledMaximumFlingVelocity();
        touchSlop = ViewConfigurationCompat.getScaledPagingTouchSlop(configuration);
        openRect = new Rect();
        closeRect = new Rect();
    }

    @Override
    public boolean onInterceptTouchEvent(MotionEvent event) {
        if (!scroller.isFinished()) {
            return super.onInterceptTouchEvent(event);
        }
        int action = event.getAction();
        switch (action & MotionEvent.ACTION_MASK) {
            case MotionEvent.ACTION_DOWN: {
                downX = (int) event.getX();
                int downY = (int) event.getY();
                if (isOpen) {
                    if (!openRect.contains(downX, downY)) {
                        return super.onInterceptTouchEvent(event);
                    }
                } else {
                    if (!closeRect.contains(downX, downY)) {
                        return super.onInterceptTouchEvent(event);
                    }
                }
                lastX = downX;
                if (vt == null) {
                    vt = VelocityTracker.obtain();
                }
            }
            break;
            case MotionEvent.ACTION_MOVE: {
                int moveX = (int) event.getX();
                int diff = Math.abs(moveX - downX);
                if (diff > touchSlop) {
                    scroll = true;
                    return true;
                }
                lastX = moveX;
            }
            break;
        }
        return super.onInterceptTouchEvent(event);
    }

    @Override
    public boolean onTouchEvent(MotionEvent event) {
        int action = event.getAction();
        switch (action & MotionEvent.ACTION_MASK) {
            case MotionEvent.ACTION_MOVE: {
                if (canceled) {
                    return true;
                }
                if (vt == null) {
                    vt = VelocityTracker.obtain();
                }
                vt.addMovement(event);
                vt.computeCurrentVelocity(1000, maxVelocity);
                velocity = vt.getXVelocity();
                int moveX = (int) event.getX();
                int moveY = (int) event.getY();
                int diff = Math.abs(moveX - downX);
                int scrollX = lastX - moveX;
                lastX = moveX;
                if (scroll) {
                    if (!openRect.contains(moveX, moveY)) {
                        canceled = true;
                        onlyScroll(diff);
                        return true;
                    }
                    scroll = true;
                    if (getScrollX() + scrollX > 0) {
                        scrollTo(0, 0);
                        return true;
                    }
                    if (getScrollX() + scrollX < first.getWidth() - getWidth()) {
                        scrollTo(first.getWidth() - getWidth(), 0);
                        return true;
                    }
                    scrollBy(scrollX, 0);
                }
            }
            break;
            case MotionEvent.ACTION_UP: {
                if (canceled) {
                    clearState();
                    return true;
                }
                int upX = (int) event.getX();
                if (scroll) {
                    if (Math.abs(velocity) > 2500) {
                        completeScroll();
                        clearState();
                        return true;
                    }
                    int diff = Math.abs(upX - downX);
                    onlyScroll(diff);
                }
            }
            clearState();
            break;
            case MotionEvent.ACTION_CANCEL:
            case MotionEvent.ACTION_OUTSIDE:
                clearState();
                break;
        }
        return true;
    }

    private void completeScroll() {
        int dx;
        if (velocity > 0) { // right
            if (!isOpen) {
                return;
            }
            dx = -(getScrollX() + getWidth()) + first.getWidth();
        } else { // left
            if (isOpen) {
                return;
            }
            dx = -getScrollX();
        }
        scroller.startScroll(getScrollX(), 0, dx, 0);
        invalidate();
    }

    private void onlyScroll(int diff) {
        int dx;
        if (velocity > 0) { // right
            if (isOpen) { // opened
                if (diff >= openRect.width() / 3) {
                    dx = -(getScrollX() + getWidth()) + first.getWidth();
                } else {
                    dx = -getScrollX();
                }
            } else { // closed
                if (diff >= openRect.width() / 3) {
                    dx = -getScrollX();
                } else {
                    dx = -(getScrollX() + getWidth()) + first.getWidth();
                }
            }
        } else {
            if (isOpen) {
                if (diff >= openRect.width() / 3) {
                    dx = -(getScrollX() + getWidth()) + first.getWidth();
                } else {
                    dx = -getScrollX();
                }
            } else {
                if (diff >= openRect.width() / 3) {
                    dx = -getScrollX();
                } else {
                    dx = -(getScrollX() + getWidth()) + first.getWidth();
                }
            }
        }
        scroller.startScroll(getScrollX(), 0, dx, 0);
        invalidate();
    }

    public boolean isOpen() {
        return isOpen;
    }

    @Override
    protected void onScrollChanged(int l, int t, int oldl, int oldt) {
        super.onScrollChanged(l, t, oldl, oldt);
        if (isOpen) {
            if (l == -second.getWidth()) {
                first.setBackgroundResource(R.drawable.close);
                isOpen = false;
            }
        } else {
            if (l == 0) {
                first.setBackgroundResource(R.drawable.open);
                isOpen = true;
            }
        }
    }

    private Runnable close = new Runnable() {
        @Override
        public void run() {
            if (scroller.isFinished()) {
                scroller.startScroll(getScrollX(), 0, -second.getWidth(), 0);
                invalidate();
            }
        }
    };

    public void close() {
        if (isOpen) {
            post(close);
        }
    }

    private Runnable open = new Runnable() {
        @Override
        public void run() {
            if (scroller.isFinished()) {
                scroller.startScroll(getScrollX(), 0, second.getWidth(), 0);
                invalidate();
            }
        }
    };

    public void open() {
        if (!isOpen) {
            post(open);
        }
    }

    @Override
    public void computeScroll() {
        super.computeScroll();
        if (scroller.computeScrollOffset()) {
            scrollTo(scroller.getCurrX(), scroller.getCurrY());
            invalidate();
        }
    }

    private void clearState() {
        if (vt != null) {
            vt.clear();
            vt.recycle();
            vt = null;
        }
        velocity = 0;
        lastX = 0;
        downX = 0;
        scroll = false;
        canceled = false;
    }

    @Override
    protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
        int widthMode = MeasureSpec.getMode(widthMeasureSpec);
        int widthSize = MeasureSpec.getSize(widthMeasureSpec);
        int heightMode = MeasureSpec.getMode(heightMeasureSpec);
        int heightSize = MeasureSpec.getSize(heightMeasureSpec);
        MarginLayoutParams lp;

        first = getChildAt(0);
        lp = (MarginLayoutParams) first.getLayoutParams();
        int firstWidthMargin = lp.leftMargin + lp.rightMargin;
        int firstTopMargin = lp.topMargin;
        int firstBottomMargin = lp.bottomMargin;

        int firstWidthSpec = getChildMeasureSpec(widthMeasureSpec, 0, lp.width);
        int firstHeightSpec = getChildMeasureSpec(heightMeasureSpec, 0, lp.height);
        first.measure(firstWidthSpec, firstHeightSpec);

        int firstWidth = first.getMeasuredWidth();
        int firstHeight = first.getMeasuredHeight();

        second = getChildAt(1);
        lp = (MarginLayoutParams) second.getLayoutParams();
        int secondWidthMargin = lp.leftMargin;
        int secondTopMargin = lp.topMargin;
        int secondBottomMargin = lp.bottomMargin;

        int secondWidthSpec = getChildMeasureSpec(widthMeasureSpec, 0, lp.width);
        int secondHeightSpec = MeasureSpec.makeMeasureSpec(firstHeight, MeasureSpec.EXACTLY);
        second.measure(secondWidthSpec, secondHeightSpec);

        int secondWidth = second.getMeasuredWidth();

        int measuredWidth;
        int measuredHeight;

        if (widthMode == MeasureSpec.EXACTLY) {
            measuredWidth = widthSize;
            int total = firstWidthMargin + firstWidth + secondWidthMargin + secondWidth;
            if (total > widthSize) {
                int tmpWidth = secondWidth + widthSize - total;
                secondWidthSpec = getChildMeasureSpec(widthMeasureSpec, 0, tmpWidth);
                second.measure(secondWidthSpec, secondHeightSpec);
            }
        } else {
            measuredWidth = firstWidth + secondWidth + firstWidthMargin + secondWidthMargin;
        }

        if (heightMode == MeasureSpec.EXACTLY) {
            measuredHeight = heightSize;
        } else {
            int topMargin = Math.max(firstTopMargin, secondTopMargin);
            int bottomMargin = Math.max(firstBottomMargin, secondBottomMargin);
            measuredHeight = firstHeight + topMargin + bottomMargin;
        }
        setMeasuredDimension(measuredWidth, measuredHeight);
    }

    @Override
    protected void onLayout(boolean changed, int l, int t, int r, int b) {
        int width = getMeasuredWidth();
        MarginLayoutParams lp;

        second = getChildAt(1);
        lp = (MarginLayoutParams) second.getLayoutParams();
        int secondWidth = second.getMeasuredWidth();
        int childHeight = second.getMeasuredHeight();
        int secondLeftMargin = lp.leftMargin;
        int secondTopMargin = lp.topMargin;

        first = getChildAt(0);
        lp = (MarginLayoutParams) first.getLayoutParams();
        int firstWidth = first.getMeasuredWidth();
        int firstLeftMargin = lp.leftMargin;
        int firstTopMargin = lp.topMargin;
        int firstRightMargin = lp.rightMargin;
        int totalWidth = secondWidth + firstWidth + secondLeftMargin +
                firstLeftMargin + firstRightMargin;
        if (width <= totalWidth) { // left to right layout
            int left = firstLeftMargin;
            int top = Math.max(secondTopMargin, firstTopMargin);
            int right = left + firstWidth;
            int bottom = top + childHeight;

            first.layout(left, top, right, bottom);

            left = right + firstRightMargin + secondLeftMargin;
            second.layout(left, top, width, bottom);
            openRect.left = 0;
            openRect.top = top;
            openRect.right = width;
            openRect.bottom = childHeight;
        } else { // right to left layout
            int left = width - secondWidth;
            int top = Math.max(secondTopMargin, firstTopMargin);
            int right = width;
            int bottom = top + childHeight;

            second.layout(left, top, right, bottom);

            left = left - secondLeftMargin - firstRightMargin - firstWidth;
            right = left + firstWidth;
            first.layout(left, top, right, bottom);

            openRect.left = left;
            openRect.top = top;
            openRect.right = width;
            openRect.bottom = bottom;
        }
        closeRect.left = getWidth() - first.getWidth();
        closeRect.top = Math.max(secondTopMargin, firstTopMargin);
        closeRect.right = getWidth();
        closeRect.bottom = closeRect.top + childHeight;
    }

    @Override
    public LayoutParams generateLayoutParams(AttributeSet attrs) {
        return new MarginLayoutParams(getContext(), attrs);
    }
}
