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

package tech.techlore.plexus.listeners;

import android.view.GestureDetector;
import android.view.MotionEvent;
import android.view.View;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.google.android.material.bottomsheet.BottomSheetBehavior;

import tech.techlore.plexus.activities.MainActivity;

public class RecyclerViewItemTouchListener implements RecyclerView.OnItemTouchListener {
    private final MainActivity mainActivity;
    private final GestureDetector gestureDetector;
    
    public RecyclerViewItemTouchListener(MainActivity mainActivity) {
        this.mainActivity = mainActivity;
        this.gestureDetector = new GestureDetector(mainActivity, new GestureDetector.SimpleOnGestureListener());
    }
    
    @Override
    public boolean onInterceptTouchEvent(@NonNull RecyclerView rv, @NonNull MotionEvent e) {
        // Stop background clicks when bottom sheet is expanded
        if (mainActivity.bottomSheetBehavior.getState() != BottomSheetBehavior.STATE_COLLAPSED) {
            return true;
        }
        View child = rv.findChildViewUnder(e.getX(), e.getY());
        return child != null && gestureDetector.onTouchEvent(e);
    }
    
    @Override
    public void onTouchEvent(@NonNull RecyclerView rv, @NonNull MotionEvent e) {
    }
    
    @Override
    public void onRequestDisallowInterceptTouchEvent(boolean disallowIntercept) {
    }
    
}
