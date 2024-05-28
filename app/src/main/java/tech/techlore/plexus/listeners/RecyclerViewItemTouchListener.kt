/*
 *     Copyright (C) 2022-present Techlore
 *
 *     This program is free software: you can redistribute it and/or modify
 *     it under the terms of the GNU General Public License as published by
 *     the Free Software Foundation, either version 3 of the License, or
 *     (at your option) any later version.
 *
 *     This program is distributed in the hope that it will be useful,
 *     but WITHOUT ANY WARRANTY; without even the implied warranty of
 *     MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 *     GNU General Public License for more details.
 *
 *     You should have received a copy of the GNU General Public License
 *     along with this program.  If not, see <https://www.gnu.org/licenses/>.
 */

package tech.techlore.plexus.listeners

import android.view.GestureDetector
import android.view.GestureDetector.SimpleOnGestureListener
import android.view.MotionEvent
import androidx.recyclerview.widget.RecyclerView
import com.google.android.material.bottomsheet.BottomSheetBehavior
import tech.techlore.plexus.activities.MainActivity

class RecyclerViewItemTouchListener(private val mainActivity: MainActivity) : RecyclerView.OnItemTouchListener {

    private val gestureDetector = GestureDetector(mainActivity, SimpleOnGestureListener())

    override fun onInterceptTouchEvent(rv: RecyclerView, e: MotionEvent): Boolean {
        // Stop background clicks when bottom sheet is expanded
        if (mainActivity.bottomSheetBehavior.state != BottomSheetBehavior.STATE_COLLAPSED) {
            return true
        }
        val child = rv.findChildViewUnder(e.x, e.y)
        return child != null && gestureDetector.onTouchEvent(e)
    }

    override fun onTouchEvent(rv: RecyclerView, e: MotionEvent) {}

    override fun onRequestDisallowInterceptTouchEvent(disallowIntercept: Boolean) {}
}