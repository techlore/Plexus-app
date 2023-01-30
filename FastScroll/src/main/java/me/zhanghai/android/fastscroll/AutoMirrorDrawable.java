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
import android.graphics.Canvas;
import android.graphics.Rect;
import android.graphics.drawable.Drawable;
import android.graphics.drawable.DrawableWrapper;
import android.view.View;

import androidx.annotation.NonNull;
import androidx.core.graphics.drawable.DrawableCompat;

@SuppressLint("RestrictedApi")
class AutoMirrorDrawable extends DrawableWrapper {

    public AutoMirrorDrawable(@NonNull Drawable drawable) {
        super(drawable);
    }

    @Override
    public void draw(@NonNull Canvas canvas) {
        if (needMirroring()) {
            float centerX = getBounds().exactCenterX();
            canvas.scale(-1, 1, centerX, 0);
            super.draw(canvas);
            canvas.scale(-1, 1, centerX, 0);
        } else {
            super.draw(canvas);
        }
    }

    @Override
    public boolean onLayoutDirectionChanged(int layoutDirection) {
        super.onLayoutDirectionChanged(layoutDirection);
        return true;
    }

    @Override
    public boolean isAutoMirrored() {
        return true;
    }

    private boolean needMirroring() {
        return DrawableCompat.getLayoutDirection(this) == View.LAYOUT_DIRECTION_RTL;
    }

    @Override
    public boolean getPadding(@NonNull Rect padding) {
        boolean hasPadding = super.getPadding(padding);
        if (needMirroring()) {
            int paddingStart = padding.left;
            int paddingEnd = padding.right;
            padding.left = paddingEnd;
            padding.right = paddingStart;
        }
        return hasPadding;
    }
}
