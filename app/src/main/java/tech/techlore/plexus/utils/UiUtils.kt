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

import android.widget.TextView
import androidx.navigation.NavController
import tech.techlore.plexus.R

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
                
                    R.id.installedAppsFragment -> R.id.action_installedAppsFragment_self
                
                    else -> R.id.action_favoritesFragment_self
                }
        
            navController.navigate(action)
        }
        
        /*fun mapScoreToStatusChip(score: Int): Int {
            return when(score) {
                1 -> R.id.
                2 -> "bronze"
                3 -> "silver"
                4 -> "gold"
                else -> "notTested"
            }
        }*/
    }
}