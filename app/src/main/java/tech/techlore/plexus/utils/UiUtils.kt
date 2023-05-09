/*
 * Copyright (c) 2022-present Techlore
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

package tech.techlore.plexus.utils

import android.content.Context
import android.content.res.ColorStateList
import android.view.View
import android.widget.TextView
import androidx.coordinatorlayout.widget.CoordinatorLayout
import androidx.core.content.ContextCompat
import androidx.navigation.NavController
import com.google.android.material.snackbar.BaseTransientBottomBar
import com.google.android.material.snackbar.Snackbar
import com.google.android.material.textview.MaterialTextView
import tech.techlore.plexus.R
import tech.techlore.plexus.preferences.PreferenceManager

class UiUtils {
    
    companion object {
        
        // Horizontally scroll text,
        // if text is too long
        fun hScrollText(textView: TextView) {
            textView.setSingleLine()
            textView.isSelected = true
        }
        
        fun refreshFragment(navController: NavController) {
            val currentFragment = navController.currentDestination!!
            val action =
                when(currentFragment.id) {
                    R.id.plexusDataFragment -> R.id.action_plexusDataFragment_self
                    
                    R.id.favoritesFragment -> R.id.action_favoritesFragment_self
    
                    R.id.submitRatingFragment -> R.id.action_submitRatingFragment_self
                    
                    R.id.myRatingsFragment -> R.id.action_myRatingsFragment_self
                    
                    else -> R.id.action_userRatingsFragment_self
                }
            
            navController.navigate(action)
        }
    
        fun mapStatusChipToScoreRange(preferenceManager: PreferenceManager, sortKey: String): Pair<Float, Float> {
            return when (preferenceManager.getInt(sortKey)) {
                R.id.sort_not_tested -> Pair(0.0f, 0.0f)
                R.id.sort_broken -> Pair(1.0f, 1.9f)
                R.id.sort_bronze -> Pair(2.0f, 2.9f)
                R.id.sort_silver -> Pair(3.0f, 3.9f)
                R.id.sort_gold -> Pair(4.0f, 4.0f)
                else -> Pair(-1.0f, -1.0f)
            }
        }
        
        fun mapScoreRangeToStatusString(context: Context, score: Float): String {
            return when(score) {
                0.0f -> context.getString(R.string.na)
                in 1.0f..1.9f -> context.getString(R.string.broken_title)
                in 2.0f..2.9f -> context.getString(R.string.bronze_title)
                in 3.0f..3.9f -> context.getString(R.string.silver_title)
                else -> context.getString(R.string.gold_title)
            }
        }
        
        fun mapStatusStringToBgColor(context: Context, status: String): Int? {
            return when(status) {
                context.getString(R.string.na) -> null // No background tint. Only show outline
                context.getString(R.string.broken_title) -> context.resources.getColor(R.color.color_broken_status, context.theme)
                context.getString(R.string.bronze_title) -> context.resources.getColor(R.color.color_bronze_status, context.theme)
                context.getString(R.string.silver_title) -> context.resources.getColor(R.color.color_silver_status, context.theme)
                else -> context.resources.getColor(R.color.color_gold_status, context.theme)
            }
        }
        
        fun mapStatusChipIdToRatingScore(statusChipId: Int): Int {
            return when (statusChipId) {
                R.id.my_ratings_sort_any -> -1
                R.id.my_ratings_sort_broken, R.id.user_ratings_sort_broken, R.id.submit_broken -> 1
                R.id.my_ratings_sort_bronze, R.id.user_ratings_sort_bronze, R.id.submit_bronze -> 2
                R.id.my_ratings_sort_silver, R.id.user_ratings_sort_silver, R.id.submit_silver -> 3
                else -> 4
            }
        }
    
        fun mapRatingScoreToStatusTextStyle(context: Context, ratingScore: Int, textView: MaterialTextView) {
            val (statusString, backgroundTint) =
                when(ratingScore) {
                    1 -> Pair(context.getString(R.string.broken_title),
                              context.resources.getColor(R.color.color_broken_status, context.theme))
                    2 -> Pair(context.getString(R.string.bronze_title),
                              context.resources.getColor(R.color.color_bronze_status, context.theme))
                    3 -> Pair(context.getString(R.string.silver_title),
                              context.resources.getColor(R.color.color_silver_status, context.theme))
                    else -> Pair(context.getString(R.string.gold_title),
                                 context.resources.getColor(R.color.color_gold_status, context.theme))
                }
        
            textView.text = statusString
            textView.backgroundTintList = ColorStateList.valueOf(backgroundTint)
        }
        
        fun statusTextViewIcon(context: Context, googleLib: String, textView: MaterialTextView) {
            val statusIcon =
                when (googleLib) {
                    "none" -> ContextCompat.getDrawable(context, R.drawable.ic_degoogled)
                    else -> ContextCompat.getDrawable(context, R.drawable.ic_microg)
                }
    
            textView.setCompoundDrawablesWithIntrinsicBounds(statusIcon, null, null, null)
            
        }
        
        fun showSnackbar(coordinatorLayout: CoordinatorLayout, message: String, anchorView: View?) {
            Snackbar.make(coordinatorLayout, message, BaseTransientBottomBar.LENGTH_SHORT)
                .setAnchorView(anchorView) // Above FAB, bottom bar etc.
                .show()
        }
        
    }
}