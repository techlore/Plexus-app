/*
 * Copyright (c) 2022 Techlore
 *
 *  This file is part of Plexus.
 *
 *  Plexus is free software: you can redistribute it and/or modify
 *  it under the terms of the GNU General Public License as published by
 *  the Free Software Foundation, either version 3 of the License, or
 *  (at your option) any later version.
 *
 *  Plexus is distributed in the hope that it will be useful,
 *  but WITHOUT ANY WARRANTY; without even the implied warranty of
 *  MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 *  GNU General Public License for more details.
 *
 *  You should have received a copy of the GNU General Public License
 *  along with Plexus.  If not, see <https://www.gnu.org/licenses/>.
 */

package me.zhanghai.android.fastscroll;

import android.graphics.Canvas;
import android.view.MotionEvent;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

public abstract class SimpleViewHelper implements FastScroller.ViewHelper {

    @Nullable
    private Runnable mOnPreDrawListener;

    @Nullable
    private Runnable mOnScrollChangedListener;

    @Nullable
    private Predicate<MotionEvent> mOnTouchEventListener;
    private boolean mListenerInterceptingTouchEvent;

    @Override
    public void addOnPreDrawListener(@Nullable Runnable listener) {
        mOnPreDrawListener = listener;
    }

    public void draw(@NonNull Canvas canvas) {

        if (mOnPreDrawListener != null) {
            mOnPreDrawListener.run();
        }

        superDraw(canvas);
    }

    @Override
    public void addOnScrollChangedListener(@Nullable Runnable listener) {
        mOnScrollChangedListener = listener;
    }

    public void onScrollChanged(int left, int top, int oldLeft, int oldTop) {
        superOnScrollChanged(left, top, oldLeft, oldTop);

        if (mOnScrollChangedListener != null) {
            mOnScrollChangedListener.run();
        }
    }

    @Override
    public void addOnTouchEventListener(@Nullable Predicate<MotionEvent> listener) {
        mOnTouchEventListener = listener;
    }

    public boolean onInterceptTouchEvent(@NonNull MotionEvent event) {

        if (mOnTouchEventListener != null && mOnTouchEventListener.test(event)) {

            int actionMasked = event.getActionMasked();
            if (actionMasked != MotionEvent.ACTION_UP
                    && actionMasked != MotionEvent.ACTION_CANCEL) {
                mListenerInterceptingTouchEvent = true;
            }

            if (actionMasked != MotionEvent.ACTION_CANCEL) {
                MotionEvent cancelEvent = MotionEvent.obtain(event);
                cancelEvent.setAction(MotionEvent.ACTION_CANCEL);
                superOnInterceptTouchEvent(cancelEvent);
                cancelEvent.recycle();
            } else {
                superOnInterceptTouchEvent(event);
            }

            return true;
        }

        return superOnInterceptTouchEvent(event);
    }

    public boolean onTouchEvent(@NonNull MotionEvent event) {

        if (mOnTouchEventListener != null) {
            if (mListenerInterceptingTouchEvent) {

                mOnTouchEventListener.test(event);

                int actionMasked = event.getActionMasked();
                if (actionMasked == MotionEvent.ACTION_UP
                        || actionMasked == MotionEvent.ACTION_CANCEL) {
                    mListenerInterceptingTouchEvent = false;
                }

                return true;
            } else {
                int actionMasked = event.getActionMasked();
                if (actionMasked != MotionEvent.ACTION_DOWN && mOnTouchEventListener.test(event)) {

                    if (actionMasked != MotionEvent.ACTION_UP
                            && actionMasked != MotionEvent.ACTION_CANCEL) {
                        mListenerInterceptingTouchEvent = true;
                    }

                    if (actionMasked != MotionEvent.ACTION_CANCEL) {
                        MotionEvent cancelEvent = MotionEvent.obtain(event);
                        cancelEvent.setAction(MotionEvent.ACTION_CANCEL);
                        superOnTouchEvent(cancelEvent);
                        cancelEvent.recycle();
                    } else {
                        superOnTouchEvent(event);
                    }

                    return true;
                }
            }
        }

        return superOnTouchEvent(event);
    }

    @Override
    public int getScrollRange() {
        return computeVerticalScrollRange();
    }

    @Override
    public int getScrollOffset() {
        return computeVerticalScrollOffset();
    }

    @Override
    public void scrollTo(int offset) {
        scrollTo(getScrollX(), offset);
    }

    protected abstract void superDraw(@NonNull Canvas canvas);

    protected abstract void superOnScrollChanged(int left, int top, int oldLeft, int oldTop);

    protected abstract boolean superOnInterceptTouchEvent(@NonNull MotionEvent event);

    protected abstract boolean superOnTouchEvent(@NonNull MotionEvent event);

    protected abstract int computeVerticalScrollRange();

    protected abstract int computeVerticalScrollOffset();

    protected abstract int getScrollX();

    protected abstract void scrollTo(int x, int y);
}
