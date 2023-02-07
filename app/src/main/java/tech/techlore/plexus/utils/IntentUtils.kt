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

package tech.techlore.plexus.utils

import android.app.Activity
import android.content.ActivityNotFoundException
import android.content.Intent
import android.net.Uri
import android.view.View
import androidx.coordinatorlayout.widget.CoordinatorLayout
import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentManager
import com.google.android.material.snackbar.BaseTransientBottomBar
import com.google.android.material.snackbar.Snackbar
import tech.techlore.plexus.R
import tech.techlore.plexus.activities.AppDetailsActivity
import tech.techlore.plexus.models.InstalledApp
import tech.techlore.plexus.models.PlexusData

class IntentUtils {
    
    companion object {
        // Send array list with intent
        fun sendListsIntent(activityFrom: Activity, activityTo: Class<*>,
                            plexusDataList: ArrayList<PlexusData>, installedAppsList: ArrayList<InstalledApp>) {
            
            activityFrom.startActivity(Intent(activityFrom, activityTo)
                                           .putParcelableArrayListExtra("plexusDataList", plexusDataList)
                                           .putParcelableArrayListExtra("installedAppsList", installedAppsList))
        }
    
        // App details activity
        fun appDetailsActivity(activityFrom: Activity, packageName: String, fromFragment: String) {
    
            activityFrom.startActivity(Intent(activityFrom, AppDetailsActivity::class.java)
                                           .putExtra("packageName", packageName)
                                           .putExtra("fromFrag", fromFragment))
            
            activityFrom.overridePendingTransition(R.anim.fade_scale_in, R.anim.no_movement)
        }
    
        // Refresh fragment
        fun reloadFragment(fragmentManager: FragmentManager, fragment: Fragment) {
            fragmentManager.beginTransaction().detach(fragment).commitNow()
            fragmentManager.beginTransaction().attach(fragment).commitNow()
        }
    
        // Open links
        fun openURL(activity: Activity, URL: String, coordinatorLayout: CoordinatorLayout, anchorView: View?) {
            try {
                activity.startActivity(Intent(Intent.ACTION_VIEW, Uri.parse(URL)))
            } // If no browser installed, show snackbar
            catch (e: ActivityNotFoundException) {
                Snackbar.make(coordinatorLayout,
                              R.string.no_browsers,
                              BaseTransientBottomBar.LENGTH_SHORT)
                    .setAnchorView(anchorView) // Above FAB, bottom bar etc.
                    .show()
            }
        }
    
        // Share
        fun share(activity: Activity, nameString: String, packageNameString: String, /*String plexusVersionString,
                             String dgStatusString, String mgStatusString,
                             String dgNotesString, String mgNotesString,*/
                  playStoreString: String) {
    
            activity.startActivity(Intent.createChooser(Intent(Intent.ACTION_SEND)
                                                            .setType("text/plain")
                                                            .putExtra(Intent.EXTRA_TEXT,
                                                                      """
                                    ${activity.getString(R.string.app_name)}: $nameString
                                    ${activity.getString(R.string.package_name)}: $packageNameString
                                    ${activity.getString(R.string.play_store)}: $playStoreString
                                                            """.trimIndent()),
                                                            activity.getString(R.string.share)))
        }
    }
}