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
import android.content.pm.PackageManager
import android.content.res.ColorStateList
import android.view.View
import android.widget.ImageView
import android.widget.TextView
import androidx.coordinatorlayout.widget.CoordinatorLayout
import androidx.core.content.ContextCompat
import androidx.navigation.NavController
import coil.ImageLoader
import coil.request.CachePolicy
import coil.request.ImageRequest
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
            textView.apply {
                setSingleLine()
                isSelected = true
            }
        }
        
        fun refreshFragment(navController: NavController) {
            val action =
                when(navController.currentDestination!!.id) {
                    R.id.plexusDataFragment -> R.id.action_plexusDataFragment_self
                    R.id.favoritesFragment -> R.id.action_favoritesFragment_self
                    R.id.submitRatingFragment -> R.id.action_submitRatingFragment_self
                    R.id.myRatingsFragment -> R.id.action_myRatingsFragment_self
                    R.id.userRatingsFragment -> R.id.action_userRatingsFragment_self
                    else -> R.id.action_myRatingsDetailsFragment_self
                }
            
            navController.navigate(action)
        }
        
        fun displayAppIcon(context: Context,
                           imageView: ImageView,
                           isInstalled: Boolean,
                           packageName: String,
                           iconUrl: String) {
            imageView.apply {
                if (isInstalled) {
                    try {
                        setImageDrawable(context.packageManager.getApplicationIcon(packageName))
                        // Don't use Coil to load icons directly to ImageView
                        // as there's a delay in displaying icons when fast scrolling
                    }
                    catch (e: PackageManager.NameNotFoundException) {
                        e.printStackTrace()
                    }
                }
                else {
                    val imageRequest =
                        ImageRequest.Builder(context)
                            .data(iconUrl)
                            .memoryCachePolicy(CachePolicy.ENABLED)
                            .placeholder(R.drawable.ic_apk)
                            .fallback(R.drawable.ic_apk)
                            .target(this)
                            .build()
                    
                    ImageLoader.Builder(context)
                        .build()
                        .enqueue(imageRequest)
                }
            }
        }
        
        fun mapStatusChipToScoreRange(preferenceManager: PreferenceManager, sortKey: String): Pair<Float, Float> {
            return when (preferenceManager.getInt(sortKey)) {
                R.id.sortNotTested -> Pair(0.0f, 0.0f)
                R.id.sortBroken -> Pair(1.0f, 1.9f)
                R.id.sortBronze -> Pair(2.0f, 2.9f)
                R.id.sortSilver -> Pair(3.0f, 3.9f)
                R.id.sortGold -> Pair(4.0f, 4.0f)
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
        
        fun mapScoreRangeToColor(context: Context, score: Float): Int {
            return when(score) {
                0.0f -> context.resources.getColor(R.color.color_primary, context.theme)
                in 1.0f..1.9f -> context.resources.getColor(R.color.color_broken_status, context.theme)
                in 2.0f..2.9f -> context.resources.getColor(R.color.color_bronze_status, context.theme)
                in 3.0f..3.9f -> context.resources.getColor(R.color.color_silver_status, context.theme)
                else -> context.resources.getColor(R.color.color_gold_status, context.theme)
            }
        }
        
        fun mapStatusStringToColor(context: Context, status: String): Int? {
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
                R.id.ratingsSortBroken, R.id.submitBroken -> 1
                R.id.ratingsSortBronze, R.id.submitBronze -> 2
                R.id.ratingsSortSilver, R.id.submitSilver -> 3
                else -> 4
            }
        }
        
        fun setInstalledFromTextViewStyle(context: Context, installedFrom: String, textView: MaterialTextView) {
            val (icon, installedFromText) =
                when (installedFrom) {
                    
                    "google_play_alternative" ->
                        Pair(ContextCompat.getDrawable(context, R.drawable.ic_google_play),
                             context.getString(R.string.google_play_alt))
                    
                    "fdroid" ->
                        Pair(ContextCompat.getDrawable(context, R.drawable.ic_fdroid),
                             context.getString(R.string.fdroid))
                    
                    "apk" ->
                        Pair(ContextCompat.getDrawable(context, R.drawable.ic_apk),
                             context.getString(R.string.apk))
                    
                    else ->
                        Pair(ContextCompat.getDrawable(context, R.drawable.ic_cancel),
                             context.getString(R.string.na))
                }
            
            textView.apply {
                setCompoundDrawablesWithIntrinsicBounds(icon, null, null, null)
                text = installedFromText
            }
        }
        
        fun setStatusTextViewStyle(context: Context,
                                   googleLib: String,
                                   ratingScore: Int,
                                   textView: MaterialTextView) {
            val statusIcon =
                when (googleLib) {
                    "native" -> ContextCompat.getDrawable(context, R.drawable.ic_degoogled)
                    else -> ContextCompat.getDrawable(context, R.drawable.ic_microg)
                }
            
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
            
            textView.apply {
                setCompoundDrawablesWithIntrinsicBounds(statusIcon, null, null, null)
                text = statusString
                backgroundTintList = ColorStateList.valueOf(backgroundTint)
            }
        }
        
        fun mapInstalledFromChipIdToString(installedFromChip: Int): String {
            return when (installedFromChip) {
                R.id.ratingsChipInstalledGooglePlayAlt -> "google_play_alternative"
                R.id.ratingsChipInstalledFdroid -> "fdroid"
                else -> "apk"
            }
        }
        
        fun showSnackbar(coordinatorLayout: CoordinatorLayout, message: String, anchorView: View?) {
            Snackbar.make(coordinatorLayout, message, BaseTransientBottomBar.LENGTH_SHORT)
                .setAnchorView(anchorView) // Above FAB, bottom bar etc.
                .show()
        }
        
    }
}