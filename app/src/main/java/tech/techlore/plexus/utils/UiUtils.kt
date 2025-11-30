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

import android.animation.Animator
import android.animation.AnimatorSet
import android.animation.ObjectAnimator
import android.content.Context
import android.content.pm.PackageManager
import android.content.res.ColorStateList
import android.os.Build
import android.text.format.DateFormat
import android.view.View
import android.view.Window
import android.widget.ImageView
import android.widget.TextView
import androidx.appcompat.app.AppCompatDelegate
import androidx.coordinatorlayout.widget.CoordinatorLayout
import androidx.core.animation.doOnEnd
import androidx.core.animation.doOnStart
import androidx.core.content.ContextCompat
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import androidx.core.view.isVisible
import androidx.core.view.updatePadding
import androidx.core.widget.NestedScrollView
import androidx.interpolator.view.animation.FastOutSlowInInterpolator
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
import tech.techlore.plexus.objects.DeviceState
import java.time.Instant
import java.time.ZoneId
import java.time.format.DateTimeFormatter
import java.time.format.DateTimeParseException
import java.time.format.FormatStyle

class UiUtils {
    
    companion object : KoinComponent {
        
        fun setAppTheme(selectedTheme: Int) {
            when (selectedTheme) {
                0 -> {
                    if (Build.VERSION.SDK_INT >= 29) {
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
        
        fun MaterialTextView.setDgMgTextWithIcon(context: Context,
                                                 appendStatusString: Boolean = false) {
            val (statusIcon, statusText) =
                if (DeviceState.isDeviceMicroG) {
                    Pair(ContextCompat.getDrawable(context, R.drawable.ic_microg),
                         if (! appendStatusString) context.getString(R.string.microG)
                         else "${context.getString(R.string.microG)} ${context.getString(R.string.status)}")
                }
                else {
                    Pair(ContextCompat.getDrawable(context, R.drawable.ic_degoogled),
                         if (! appendStatusString) context.getString(R.string.de_Googled)
                         else "${context.getString(R.string.de_Googled)} ${context.getString(R.string.status)}")
                }
            setCompoundDrawablesWithIntrinsicBounds(statusIcon, null, null, null)
            text = statusText
        }
        
        // Adjust recyclerview for edge to edge
        fun RecyclerView.adjustEdgeToEdge(context: Context) {
            ViewCompat.setOnApplyWindowInsetsListener(this) { v, windowInsets ->
                val insets = windowInsets.getInsets(WindowInsetsCompat.Type.systemBars()
                                                            or WindowInsetsCompat.Type.displayCutout())
                v.updatePadding(left = insets.left,
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
        
        fun NavController.refreshFragment() {
            val action =
                when (currentDestination!!.id) {
                    R.id.plexusDataFragment -> R.id.action_global_to_plexusDataFragment
                    R.id.installedAppsFragment -> R.id.action_global_to_installedAppsFragment
                    R.id.favoritesFragment -> R.id.action_global_to_favoritesFragment
                    R.id.searchFragment -> R.id.action_searchFragment_self
                    R.id.myRatingsFragment -> R.id.action_global_to_myRatingsFragment
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
                    // Since there's a delay in displaying icons when fast scrolling,
                    // don't use Coil to load icons directly to ImageView for installed apps
                    setImageDrawable(context.packageManager.getApplicationIcon(packageName))
                }
                catch (e: PackageManager.NameNotFoundException) {
                    e.printStackTrace()
                }
            }
            else {
                load(
                    iconUrl
                        ?.takeIf { it.startsWith("https://play-lh") }
                        ?.let {
                            // Assuming 55dp = 165px for 480 DPI
                            // append "=w165-h165" to the Play Store url
                            // this will prevent downloading 512x512 px icon
                            // & will download only 165x165 px icon
                            "${it}=w${get<Int>(named("displayedIconSize"))}-h${get<Int>(named("displayedIconSize"))}"
                        }
                    ?: iconUrl
                ) {
                    size(get<Int>(named("displayedIconSize")))
                    placeholder(R.drawable.ic_apk)
                    fallback(R.drawable.ic_apk)
                    error(R.drawable.ic_apk)
                }
            }
        }
        
        fun mapStatusChipToScoreRange(sortChipId: Int): Pair<Float, Float> {
            return when (sortChipId) {
                R.id.sortNotTested -> Pair(0.0f, 0.0f)
                R.id.sortBroken -> Pair(1.0f, 1.9f)
                R.id.sortBronze -> Pair(2.0f, 2.9f)
                R.id.sortSilver -> Pair(3.0f, 3.4f)
                R.id.sortGold -> Pair(3.5f, 4.0f)
                else -> Pair(- 1.0f, - 1.0f)
            }
        }
        
        fun mapScoreRangeToStatusString(context: Context, score: Float): String {
            return when (score) {
                0.0f -> context.getString(R.string.na)
                in 1.0f..1.9f -> context.getString(R.string.broken_title)
                in 2.0f..2.9f -> context.getString(R.string.bronze_title)
                in 3.0f..3.4f -> context.getString(R.string.silver_title)
                else -> context.getString(R.string.gold_title)
            }
        }
        
        fun MaterialTextView.setStatusStyleWithoutIcon(context: Context, statusString: String) {
            val (textColor, backgroundTint) =
                when (statusString) {
                    context.getString(R.string.na) -> Pair(null, null) // No background tint. Only show outline
                    
                    context.getString(R.string.broken_title) ->
                        Pair(context.resources.getColor(R.color.color_broken_status_text, context.theme),
                             context.resources.getColor(R.color.color_broken_status, context.theme))
                    
                    context.getString(R.string.bronze_title) ->
                        Pair(context.resources.getColor(R.color.color_bronze_status_text, context.theme),
                             context.resources.getColor(R.color.color_bronze_status, context.theme))
                    
                    context.getString(R.string.silver_title) ->
                        Pair(context.resources.getColor(R.color.color_silver_status_text, context.theme),
                             context.resources.getColor(R.color.color_silver_status, context.theme))
                    
                    else ->
                        Pair(context.resources.getColor(R.color.color_gold_status_text, context.theme),
                             context.resources.getColor(R.color.color_gold_status, context.theme))
                }
            
            text = statusString
            textColor?.let{setTextColor(ColorStateList.valueOf(it))}
            backgroundTint?.let { backgroundTintList = ColorStateList.valueOf(it) }
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
            if (isVisible) {
                setCompoundDrawablesWithIntrinsicBounds(icon, null, null, null)
                text = installedFromText
            }
        }
        
        fun MaterialTextView.setStatusStyleWithIcon(context: Context,
                                                    googleLib: String,
                                                    ratingScore: Int) {
            val statusIcon =
                when (googleLib) {
                    "native" -> ContextCompat.getDrawable(context, R.drawable.ic_degoogled)
                    else -> ContextCompat.getDrawable(context, R.drawable.ic_microg)
                }
            
            val (statusString, textColor, backgroundTint) =
                when (ratingScore) {
                    1 -> Triple(context.getString(R.string.broken_title),
                                context.resources.getColor(R.color.color_broken_status_text, context.theme),
                                context.resources.getColor(R.color.color_broken_status, context.theme))
                    
                    2 -> Triple(context.getString(R.string.bronze_title),
                                context.resources.getColor(R.color.color_bronze_status_text, context.theme),
                                context.resources.getColor(R.color.color_bronze_status, context.theme))
                    
                    3 -> Triple(context.getString(R.string.silver_title),
                                context.resources.getColor(R.color.color_silver_status_text, context.theme),
                                context.resources.getColor(R.color.color_silver_status, context.theme))
                    
                    else -> Triple(context.getString(R.string.gold_title),
                                   context.resources.getColor(R.color.color_gold_status_text, context.theme),
                                   context.resources.getColor(R.color.color_gold_status, context.theme))
                }
            
            setCompoundDrawablesWithIntrinsicBounds(statusIcon, null, null, null)
            text = statusString
            setTextColor(ColorStateList.valueOf(textColor))
            backgroundTintList = ColorStateList.valueOf(backgroundTint)
        }
        
        fun String.formatRfc3339ToLocalized(context: Context): String {
            return try {
                val instant = Instant.parse(this)
                val zone = ZoneId.systemDefault()
                val locale = context.resources.configuration.locales[0]
                
                val dateFormatter =
                    DateTimeFormatter
                        .ofLocalizedDate(FormatStyle.MEDIUM)
                        .withLocale(locale)
                        .withZone(zone)
                
                val timeFormatter =
                    DateTimeFormatter
                        .ofPattern(
                            if (DateFormat.is24HourFormat(context)) "HH:mm" else "hh:mm a",
                            locale
                        )
                        .withZone(zone)
                
                buildString {
                    append(dateFormatter.format(instant))
                    append(" \u007C ")
                    append(timeFormatter.format(instant))
                }
            }
            catch (_: DateTimeParseException) {
                this
            }
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
                    // Manually trigger nested scroll events
                    // to notify HideViewOnScrollBehavior for bottom Constraint layout
                    // This won't scroll to top visually
                    startNestedScroll(ViewCompat.SCROLL_AXIS_VERTICAL)
                    dispatchNestedScroll(0, - 1, 0, 0, null)
                    stopNestedScroll()
                    
                    // Actually scroll to top
                    fling(0)
                    smoothScrollTo(0, 0)
                }
            }
        }
        
        fun View.showViewWithAnim(isFab: Boolean =  false) {
            AnimatorSet().apply {
                playTogether(
                    arrayListOf<Animator>().apply {
                        add(ObjectAnimator.ofFloat(this@showViewWithAnim, "alpha", 0.0f, 1.0f))
                        if (isFab) {
                            add(ObjectAnimator.ofFloat(this@showViewWithAnim, "scaleX", 0.8f, 1.0f))
                            add(ObjectAnimator.ofFloat(this@showViewWithAnim, "scaleY", 0.8f, 1.0f))
                        }
                    }
                )
                duration = if (!isFab) 300L else 250L
                interpolator = FastOutSlowInInterpolator()
                doOnStart { isVisible = true }
                start()
            }
        }
        
        fun View.hideViewWithAnim(isFab: Boolean = false, onEndAction: () -> Unit = {}) {
            AnimatorSet().apply {
                playTogether(
                    arrayListOf<Animator>().apply {
                        add(ObjectAnimator.ofFloat(this@hideViewWithAnim, "alpha", 1.0f, 0.0f))
                        if (isFab) {
                            add(ObjectAnimator.ofFloat(this@hideViewWithAnim, "scaleX", 1.0f, 0.4f))
                            add(ObjectAnimator.ofFloat(this@hideViewWithAnim, "scaleY", 1.0f, 0.4f))
                        }
                    }
                )
                duration = if (!isFab) 300L else 150L
                interpolator = FastOutSlowInInterpolator()
                start()
                doOnEnd {
                    isVisible = false
                    onEndAction()
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