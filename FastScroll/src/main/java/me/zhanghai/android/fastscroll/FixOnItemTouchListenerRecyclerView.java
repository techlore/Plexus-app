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
import android.util.AttributeSet;
import android.view.MotionEvent;

import androidx.annotation.AttrRes;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.recyclerview.widget.RecyclerView;

import java.util.ArrayList;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;

public class FixOnItemTouchListenerRecyclerView extends RecyclerView {

    @NonNull
    private final OnItemTouchDispatcher mOnItemTouchDispatcher = new OnItemTouchDispatcher();

    public FixOnItemTouchListenerRecyclerView(@NonNull Context context) {
        super(context);

        init();
    }

    public FixOnItemTouchListenerRecyclerView(@NonNull Context context,
                                              @Nullable AttributeSet attrs) {
        super(context, attrs);

        init();
    }

    public FixOnItemTouchListenerRecyclerView(@NonNull Context context,
                                              @Nullable AttributeSet attrs,
                                              @AttrRes int defStyleAttr) {
        super(context, attrs, defStyleAttr);

        init();
    }

    private void init() {
        super.addOnItemTouchListener(mOnItemTouchDispatcher);
    }

    @Override
    public void addOnItemTouchListener(@NonNull OnItemTouchListener listener) {
        mOnItemTouchDispatcher.addListener(listener);
    }

    @Override
    public void removeOnItemTouchListener(@NonNull OnItemTouchListener listener) {
        mOnItemTouchDispatcher.removeListener(listener);
    }

    private static class OnItemTouchDispatcher implements OnItemTouchListener {

        @NonNull
        private final List<OnItemTouchListener> mListeners = new ArrayList<>();

        @NonNull
        private final Set<OnItemTouchListener> mTrackingListeners = new LinkedHashSet<>();

        @Nullable
        private OnItemTouchListener mInterceptingListener;

        public void addListener(@NonNull OnItemTouchListener listener) {
            mListeners.add(listener);
        }

        public void removeListener(@NonNull OnItemTouchListener listener) {
            mListeners.remove(listener);
            mTrackingListeners.remove(listener);
            if (mInterceptingListener == listener) {
                mInterceptingListener = null;
            }
        }

        // @see RecyclerView#findInterceptingOnItemTouchListener
        @Override
        public boolean onInterceptTouchEvent(@NonNull RecyclerView recyclerView,
                                             @NonNull MotionEvent event) {
            int action = event.getAction();
            for (OnItemTouchListener listener : mListeners) {
                boolean intercepted = listener.onInterceptTouchEvent(recyclerView, event);
                if (action == MotionEvent.ACTION_CANCEL) {
                    mTrackingListeners.remove(listener);
                    continue;
                }
                if (intercepted) {
                    mTrackingListeners.remove(listener);
                    event.setAction(MotionEvent.ACTION_CANCEL);
                    for (OnItemTouchListener trackingListener : mTrackingListeners) {
                        trackingListener.onInterceptTouchEvent(recyclerView, event);
                    }
                    event.setAction(action);
                    mTrackingListeners.clear();
                    mInterceptingListener = listener;
                    return true;
                } else {
                    mTrackingListeners.add(listener);
                }
            }
            return false;
        }

        @Override
        public void onTouchEvent(@NonNull RecyclerView recyclerView, @NonNull MotionEvent event) {
            if (mInterceptingListener == null) {
                return;
            }
            mInterceptingListener.onTouchEvent(recyclerView, event);
            int action = event.getAction();
            if (action == MotionEvent.ACTION_CANCEL || action == MotionEvent.ACTION_UP) {
                mInterceptingListener = null;
            }
        }

        @Override
        public void onRequestDisallowInterceptTouchEvent(boolean disallowIntercept) {
            for (OnItemTouchListener listener : mListeners) {
                listener.onRequestDisallowInterceptTouchEvent(disallowIntercept);
            }
        }
    }
}
