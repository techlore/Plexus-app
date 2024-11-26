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

package tech.techlore.plexus.utils

import android.app.Activity
import android.app.Activity.OVERRIDE_TRANSITION_CLOSE
import android.app.Activity.OVERRIDE_TRANSITION_OPEN
import android.content.Context
import android.content.pm.PackageManager
import android.content.res.ColorStateList
import android.os.Build
import android.view.View
import android.view.Window
import android.widget.ImageView
import android.widget.TextView
import androidx.appcompat.app.AppCompatDelegate
import androidx.coordinatorlayout.widget.CoordinatorLayout
import androidx.core.content.ContextCompat
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import androidx.core.view.isVisible
import androidx.core.view.updatePadding
import androidx.core.widget.NestedScrollView
import androidx.navigation.NavController
import androidx.recyclerview.widget.RecyclerView
import coil3.load
import coil3.request.error
import coil3.request.fallback
import coil3.request.placeholder
import com.google.android.material.snackbar.BaseTransientBottomBar
import com.google.android.material.snackbar.Snackbar
import com.google.android.material.textview.MaterialTextView
import org.koin.core.component.KoinComponent
import org.koin.core.component.get
import org.koin.core.qualifier.named
import tech.techlore.plexus.R
import tech.techlore.plexus.preferences.PreferenceManager

class UiUtils {
    
    companion object : KoinComponent {
        
        fun setAppTheme(selectedTheme: Int) {
            when(selectedTheme) {
                0 -> {
                    if (Build.VERSION.SDK_INT >= 29){
                        AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_FOLLOW_SYSTEM)
                    }
                    else {
                        AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_NO)
                    }
                }
                R.id.followSystem -> AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_FOLLOW_SYSTEM)
                R.id.light -> AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_NO)
                R.id.dark -> AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_YES)
            }
        }
        
        fun Window.setNavBarContrastEnforced() {
            if (Build.VERSION.SDK_INT >= 29) {
                isNavigationBarContrastEnforced = false
            }
        }
        
        // Adjust recyclerview for edge to edge
        fun RecyclerView.adjustEdgeToEdge(context: Context) {
            ViewCompat.setOnApplyWindowInsetsListener(this) { v, windowInsets ->
                val insets = windowInsets.getInsets(WindowInsetsCompat.Type.systemBars()
                                                            or WindowInsetsCompat.Type.displayCutout())
                v.updatePadding(left = insets.left,
                                top = insets.top + convertDpToPx(context, 10f),
                                right = insets.right,
                                bottom = insets.bottom + convertDpToPx(context, 10f))
                
                WindowInsetsCompat.CONSUMED
            }
        }
        
        fun convertDpToPx(context: Context, dp: Float): Int {
            return (dp * context.resources.displayMetrics.density).toInt()
        }
        
        // Horizontally scroll text,
        // if text is too long
        fun TextView.hScroll() {
            setSingleLine()
            isSelected = true
        }
        
        fun Activity.overrideTransition(isClosingTransition: Boolean = false,
                                        enterAnim: Int,
                                        exitAnim: Int) {
            if (Build.VERSION.SDK_INT >= 34) {
                overrideActivityTransition(
                    if (isClosingTransition) OVERRIDE_TRANSITION_CLOSE else OVERRIDE_TRANSITION_OPEN,
                    enterAnim,
                    exitAnim
                )
            }
            else overridePendingTransition(enterAnim, exitAnim)
        }
        
        fun NavController.refreshFragment() {
            val action =
                when(currentDestination!!.id) {
                    R.id.plexusDataFragment -> R.id.action_plexusDataFragment_self
                    R.id.installedAppsFragment -> R.id.action_installedAppsFragment_self
                    R.id.favoritesFragment -> R.id.action_favoritesFragment_self
                    R.id.myRatingsFragment -> R.id.action_myRatingsFragment_self
                    R.id.allRatingsFragment -> R.id.action_allRatingsFragment_self
                    else -> R.id.action_myRatingsDetailsFragment_self
                }
            
            navigate(action)
        }
        
        fun ImageView.displayAppIcon(context: Context,
                                     isInstalled: Boolean,
                                     packageName: String,
                                     iconUrl: String?) {
            if (isInstalled) {
                try {
                    // Don't use Coil to load icons directly to ImageView
                    // as there's a delay in displaying icons when fast scrolling
                    setImageDrawable(context.packageManager.getApplicationIcon(packageName))
                }
                catch (e: PackageManager.NameNotFoundException) {
                    e.printStackTrace()
                }
            }
            else {
                load(iconUrl) {
                    size(get<Int>(named("displayedIconSize")))
                    placeholder(R.drawable.ic_apk)
                    fallback(R.drawable.ic_apk)
                    error(R.drawable.ic_apk)
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
        
        fun MaterialTextView.setInstalledFromStyle(context: Context, installedFrom: String) {
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
                        Pair(null,
                             "")
                }
            
            isVisible = icon != null
            if (isVisible){
                setCompoundDrawablesWithIntrinsicBounds(icon, null, null, null)
                text = installedFromText
            }
        }
        
        fun MaterialTextView.setStatusStyle(context: Context,
                                            googleLib: String,
                                            ratingScore: Int) {
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
            
            setCompoundDrawablesWithIntrinsicBounds(statusIcon, null, null, null)
            text = statusString
            backgroundTintList = ColorStateList.valueOf(backgroundTint)
        }
        
        fun mapInstalledFromChipIdToString(installedFromChip: Int): String {
            return when (installedFromChip) {
                R.id.ratingsChipInstalledGooglePlayAlt -> "google_play_alternative"
                R.id.ratingsChipInstalledFdroid -> "fdroid"
                else -> "apk"
            }
        }
        
        fun NestedScrollView.scrollToTop() {
            if (scrollY != 0) {
                post {
                    fling(0)
                    smoothScrollTo(0, 0)
                }
            }
        }
        
        fun showSnackbar(coordinatorLayout: CoordinatorLayout, message: String, anchorView: View?) {
            Snackbar.make(coordinatorLayout, message, BaseTransientBottomBar.LENGTH_SHORT)
                .setAnchorView(anchorView) // Above FAB, bottom bar etc.
                .show()
        }
        
    }
}