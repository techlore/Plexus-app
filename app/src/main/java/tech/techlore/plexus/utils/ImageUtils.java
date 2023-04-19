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

package tech.techlore.plexus.utils;

import android.app.ActivityManager;
import android.content.Context;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.drawable.Drawable;
import android.util.LruCache;

public class ImageUtils {
    
    public static LruCache<String, Bitmap> BitmapCache(Context context) {
        ActivityManager activityManager = (ActivityManager) context.getSystemService(Context.ACTIVITY_SERVICE);
        int cacheSize = activityManager.getMemoryClass() * 1024 * 1024 / 8;
        
        return new LruCache<String, Bitmap>(cacheSize) {
            protected int sizeOf(String key, Bitmap bitmap) {
                return bitmap.getByteCount();
            }
        };
    }
    
    public static Bitmap GetBitmapFromCache(Context context, String key) {
        return BitmapCache(context).get(key);
    }
    
    public static void AddBitmapToCache(Context context, String key, Bitmap bitmap) {
        if (GetBitmapFromCache(context, key) == null) {
            BitmapCache(context).put(key, bitmap);
        }
    }
    
    public static Bitmap GetAppIconAsBitmap(Context context, String packageName) {
        try {
            Drawable appIcon = context.getPackageManager().getApplicationIcon(packageName);
            Bitmap bitmap = Bitmap.createBitmap(64, 64, Bitmap.Config.ARGB_8888);
            Canvas canvas = new Canvas(bitmap);
            appIcon.setBounds(0, 0, canvas.getWidth(), canvas.getHeight());
            appIcon.draw(canvas);
            AddBitmapToCache(context, packageName, bitmap);
            return bitmap;
        }
        catch (PackageManager.NameNotFoundException e) {
            return null;
        }
    }
    
}
