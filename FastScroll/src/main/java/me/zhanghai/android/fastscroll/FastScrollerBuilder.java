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

import android.content.Context;
import android.graphics.Rect;
import android.graphics.drawable.Drawable;
import android.view.ViewGroup;
import android.webkit.WebView;
import android.widget.ScrollView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.core.util.Consumer;
import androidx.core.widget.NestedScrollView;
import androidx.recyclerview.widget.RecyclerView;

import tech.techlore.plexus.me.zhanghai.android.fastscroll.R;

public class FastScrollerBuilder {

    @NonNull
    private final ViewGroup mView;

    @Nullable
    private FastScroller.ViewHelper mViewHelper;

    @Nullable
    private PopupTextProvider mPopupTextProvider;

    @Nullable
    private Rect mPadding;

    @NonNull
    private Drawable mTrackDrawable;

    @NonNull
    private Drawable mThumbDrawable;

    @NonNull
    private Consumer<TextView> mPopupStyle;

    @Nullable
    private FastScroller.AnimationHelper mAnimationHelper;

    public FastScrollerBuilder(@NonNull ViewGroup view) {
        mView = view;
        useDefaultStyle();
    }

    @NonNull
    public FastScrollerBuilder setViewHelper(@Nullable FastScroller.ViewHelper viewHelper) {
        mViewHelper = viewHelper;
        return this;
    }

    @NonNull
    public FastScrollerBuilder setPopupTextProvider(@Nullable PopupTextProvider popupTextProvider) {
        mPopupTextProvider = popupTextProvider;
        return this;
    }

    @NonNull
    public FastScrollerBuilder setPadding(int left, int top, int right, int bottom) {
        if (mPadding == null) {
            mPadding = new Rect();
        }
        mPadding.set(left, top, right, bottom);
        return this;
    }

    @NonNull
    public FastScrollerBuilder setPadding(@Nullable Rect padding) {
        if (padding != null) {
            if (mPadding == null) {
                mPadding = new Rect();
            }
            mPadding.set(padding);
        } else {
            mPadding = null;
        }
        return this;
    }

    @NonNull
    public FastScrollerBuilder setTrackDrawable(@NonNull Drawable trackDrawable) {
        mTrackDrawable = trackDrawable;
        return this;
    }

    @NonNull
    public FastScrollerBuilder setThumbDrawable(@NonNull Drawable thumbDrawable) {
        mThumbDrawable = thumbDrawable;
        return this;
    }

    @NonNull
    public FastScrollerBuilder setPopupStyle(@NonNull Consumer<TextView> popupStyle) {
        mPopupStyle = popupStyle;
        return this;
    }

    @NonNull
    public FastScrollerBuilder useDefaultStyle() {
        Context context = mView.getContext();
        mTrackDrawable = Utils.getGradientDrawableWithTintAttr(R.drawable.afs_track,
                androidx.appcompat.R.attr.colorControlNormal, context);
        mThumbDrawable = Utils.getGradientDrawableWithTintAttr(R.drawable.afs_thumb,
                androidx.appcompat.R.attr.colorControlActivated, context);
        mPopupStyle = PopupStyles.DEFAULT;
        return this;
    }

    @NonNull
    public FastScrollerBuilder useMd2Style() {
        Context context = mView.getContext();
        mTrackDrawable = Utils.getGradientDrawableWithTintAttr(R.drawable.afs_md2_track,
                androidx.appcompat.R.attr.colorControlNormal, context);
        mThumbDrawable = Utils.getGradientDrawableWithTintAttr(R.drawable.afs_md2_thumb,
                androidx.appcompat.R.attr.colorControlActivated, context);
        mPopupStyle = PopupStyles.MD2;
        return this;
    }

    public void setAnimationHelper(@Nullable FastScroller.AnimationHelper animationHelper) {
        mAnimationHelper = animationHelper;
    }

    public void disableScrollbarAutoHide() {
        DefaultAnimationHelper animationHelper = new DefaultAnimationHelper(mView);
        animationHelper.setScrollbarAutoHideEnabled(false);
        mAnimationHelper = animationHelper;
    }

    @NonNull
    public FastScroller build() {
        return new FastScroller(mView, getOrCreateViewHelper(), mPadding, mTrackDrawable,
                mThumbDrawable, mPopupStyle, getOrCreateAnimationHelper());
    }

    @NonNull
    private FastScroller.ViewHelper getOrCreateViewHelper() {
        if (mViewHelper != null) {
            return mViewHelper;
        }
        if (mView instanceof ViewHelperProvider) {
            return ((ViewHelperProvider) mView).getViewHelper();
        } else if (mView instanceof RecyclerView) {
            return new RecyclerViewHelper((RecyclerView) mView, mPopupTextProvider);
        } else if (mView instanceof NestedScrollView) {
            throw new UnsupportedOperationException("Please use "
                    + FastScrollNestedScrollView.class.getSimpleName() + " instead of "
                    + NestedScrollView.class.getSimpleName() + "for fast scroll");
        } else if (mView instanceof ScrollView) {
            throw new UnsupportedOperationException("Please use "
                    + FastScrollScrollView.class.getSimpleName() + " instead of "
                    + ScrollView.class.getSimpleName() + "for fast scroll");
        } else if (mView instanceof WebView) {
            throw new UnsupportedOperationException("Please use "
                    + FastScrollWebView.class.getSimpleName() + " instead of "
                    + WebView.class.getSimpleName() + "for fast scroll");
        } else {
            throw new UnsupportedOperationException(mView.getClass().getSimpleName()
                    + " is not supported for fast scroll");
        }
    }

    @NonNull
    private FastScroller.AnimationHelper getOrCreateAnimationHelper() {
        if (mAnimationHelper != null) {
            return mAnimationHelper;
        }
        return new DefaultAnimationHelper(mView);
    }
}
