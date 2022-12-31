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
import android.content.res.ColorStateList;
import android.content.res.TypedArray;
import android.graphics.drawable.Drawable;
import android.graphics.drawable.GradientDrawable;
import android.os.Build;

import androidx.annotation.AttrRes;
import androidx.annotation.ColorInt;
import androidx.annotation.DrawableRes;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.content.res.AppCompatResources;
import androidx.core.graphics.drawable.DrawableCompat;

class Utils {

    @ColorInt
    public static int getColorFromAttrRes(@AttrRes int attrRes, @NonNull Context context) {
        ColorStateList colorStateList = getColorStateListFromAttrRes(attrRes, context);
        return colorStateList != null ? colorStateList.getDefaultColor() : 0;
    }

    @Nullable
    public static ColorStateList getColorStateListFromAttrRes(@AttrRes int attrRes,
                                                              @NonNull Context context) {
        TypedArray a = context.obtainStyledAttributes(new int[] { attrRes });
        int resId;
        try {
            resId = a.getResourceId(0, 0);
            if (resId != 0) {
                return AppCompatResources.getColorStateList(context, resId);
            }
            return a.getColorStateList(0);
        } finally {
            a.recycle();
        }
    }

    // Work around the bug that GradientDrawable didn't actually implement tinting until
    // Lollipop MR1 (API 22).
    @Nullable
    public static Drawable getGradientDrawableWithTintAttr(@DrawableRes int drawableRes,
                                                           @AttrRes int tintAttrRes,
                                                           @NonNull Context context) {
        Drawable drawable = AppCompatResources.getDrawable(context, drawableRes);
        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.LOLLIPOP_MR1
                && drawable instanceof GradientDrawable) {
            drawable = DrawableCompat.wrap(drawable);
            drawable.setTintList(getColorStateListFromAttrRes(tintAttrRes, context));
        }
        return drawable;
    }
}
