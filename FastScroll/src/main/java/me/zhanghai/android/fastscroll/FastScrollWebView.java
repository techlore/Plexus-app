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

import android.annotation.SuppressLint;
import android.content.Context;
import android.graphics.Canvas;
import android.util.AttributeSet;
import android.view.MotionEvent;
import android.webkit.WebView;

import androidx.annotation.AttrRes;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.annotation.StyleRes;

@SuppressLint("MissingSuperCall")
public class FastScrollWebView extends WebView implements ViewHelperProvider {

    @NonNull
    private final ViewHelper mViewHelper = new ViewHelper();

    public FastScrollWebView(@NonNull Context context) {
        super(context);

        init();
    }

    public FastScrollWebView(@NonNull Context context, @Nullable AttributeSet attrs) {
        super(context, attrs);

        init();
    }

    public FastScrollWebView(@NonNull Context context, @Nullable AttributeSet attrs,
                             @AttrRes int defStyleAttr) {
        super(context, attrs, defStyleAttr);

        init();
    }

    public FastScrollWebView(@NonNull Context context, @Nullable AttributeSet attrs,
                             @AttrRes int defStyleAttr, @StyleRes int defStyleRes) {
        super(context, attrs, defStyleAttr, defStyleRes);

        init();
    }

    private void init() {
        setVerticalScrollBarEnabled(false);
        setScrollContainer(true);
    }

    @NonNull
    @Override
    public FastScroller.ViewHelper getViewHelper() {
        return mViewHelper;
    }

    @Override
    public void draw(@NonNull Canvas canvas) {
        mViewHelper.draw(canvas);
    }

    @Override
    protected void onScrollChanged(int left, int top, int oldLeft, int oldTop) {
        mViewHelper.onScrollChanged(left, top, oldLeft, oldTop);
    }

    @Override
    public boolean onInterceptTouchEvent(@NonNull MotionEvent event) {
        return mViewHelper.onInterceptTouchEvent(event);
    }

    @Override
    public boolean onTouchEvent(@NonNull MotionEvent event) {
        return mViewHelper.onTouchEvent(event);
    }

    private class ViewHelper extends SimpleViewHelper {

        @Override
        protected void superDraw(@NonNull Canvas canvas) {
            FastScrollWebView.super.draw(canvas);
        }

        @Override
        protected void superOnScrollChanged(int left, int top, int oldLeft, int oldTop) {
            FastScrollWebView.super.onScrollChanged(left, top, oldLeft, oldTop);
        }

        @Override
        protected boolean superOnInterceptTouchEvent(@NonNull MotionEvent event) {
            return FastScrollWebView.super.onInterceptTouchEvent(event);
        }

        @Override
        protected boolean superOnTouchEvent(@NonNull MotionEvent event) {
            return FastScrollWebView.super.onTouchEvent(event);
        }

        @Override
        protected int computeVerticalScrollRange() {
            return FastScrollWebView.this.computeVerticalScrollRange();
        }

        @Override
        protected int computeVerticalScrollOffset() {
            return FastScrollWebView.this.computeVerticalScrollOffset();
        }

        @Override
        protected int getScrollX() {
            return FastScrollWebView.this.getScrollX();
        }

        @Override
        protected void scrollTo(int x, int y) {
            FastScrollWebView.this.scrollTo(x, y);
        }
    }
}
