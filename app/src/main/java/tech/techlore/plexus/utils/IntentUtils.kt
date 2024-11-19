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
import android.content.ActivityNotFoundException
import android.content.Intent
import android.net.Uri
import android.view.View
import android.widget.Toast
import androidx.coordinatorlayout.widget.CoordinatorLayout
import tech.techlore.plexus.R
import tech.techlore.plexus.activities.AppDetailsActivity
import tech.techlore.plexus.utils.UiUtils.Companion.showSnackbar

class IntentUtils {
    
    companion object {
        
        // App details activity
        fun Activity.startDetailsActivity(packageName: String) {
            startActivity(Intent(this, AppDetailsActivity::class.java)
                              .putExtra("packageName", packageName))
        }
        
        // Open links
        fun Activity.openURL(url: String,
                             coordinatorLayout: CoordinatorLayout,
                             anchorView: View?) {
            try {
                startActivity(Intent(Intent.ACTION_VIEW, Uri.parse(url)))
            }
            // If no browser installed, show snackbar
            catch (_: ActivityNotFoundException) {
                showSnackbar(coordinatorLayout,
                             getString(R.string.no_browsers),
                             anchorView)
            }
        }
        fun Activity.openURL(url: String) {
            try {
                startActivity(Intent(Intent.ACTION_VIEW, Uri.parse(url)))
            }
            // If no browser installed, show toast
            catch (_: ActivityNotFoundException) {
                Toast.makeText(this, getString(R.string.no_browsers), Toast.LENGTH_SHORT).show()
            }
        }
        
    }
}