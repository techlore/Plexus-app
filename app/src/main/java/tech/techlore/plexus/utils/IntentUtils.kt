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

import android.app.Activity
import android.content.ActivityNotFoundException
import android.content.Intent
import android.net.Uri
import android.view.View
import androidx.coordinatorlayout.widget.CoordinatorLayout
import tech.techlore.plexus.R
import tech.techlore.plexus.activities.AppDetailsActivity
import tech.techlore.plexus.activities.SubmitActivity
import tech.techlore.plexus.utils.UiUtils.Companion.showSnackbar

class IntentUtils {
    
    companion object {
        
        // App details activity
        fun startDetailsActivity(activityFrom: Activity, packageName: String) {
            activityFrom.startActivity(Intent(activityFrom, AppDetailsActivity::class.java)
                                           .putExtra("packageName", packageName))
        }
        
        fun startSubmitActivity(activityFrom: Activity,
                                name: String,
                                packageName: String,
                                installedVersion: String,
                                installedBuild: Int,
                                installedFrom: String,
                                isInPlexusData: Boolean) {
            activityFrom.startActivity(Intent(activityFrom, SubmitActivity::class.java)
                                           .putExtra("name", name)
                                           .putExtra("packageName", packageName)
                                           .putExtra("installedVersion", installedVersion)
                                           .putExtra("installedBuild", installedBuild)
                                           .putExtra("installedFrom", installedFrom)
                                           .putExtra("isInPlexusData", isInPlexusData))
            activityFrom.finish()
            activityFrom.overridePendingTransition(R.anim.fade_in_slide_from_bottom, R.anim.no_movement)
        }
        
        // Open links
        fun openURL(activity: Activity,
                    URL: String,
                    coordinatorLayout: CoordinatorLayout,
                    anchorView: View?) {
            try {
                activity.startActivity(Intent(Intent.ACTION_VIEW, Uri.parse(URL)))
            }
            // If no browser installed, show snackbar
            catch (e: ActivityNotFoundException) {
                showSnackbar(coordinatorLayout,
                             activity.getString(R.string.no_browsers),
                             anchorView)
            }
        }
        
    }
}