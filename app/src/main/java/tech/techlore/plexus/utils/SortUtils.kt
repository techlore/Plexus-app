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

import tech.techlore.plexus.R
import tech.techlore.plexus.models.InstalledApp
import tech.techlore.plexus.models.PlexusData

class SortUtils {
    
    companion object {
    
        // Plexus data status sort
        fun PlexusDataStatusSort(preferenceKey: Int, plexusData: PlexusData,
                                 status: String, plexusDataList: ArrayList<PlexusData>) {
        
            when (preferenceKey) {
                0, R.id.sort_not_tested -> if (status == "X") plexusDataList.add(plexusData)
                R.id.sort_unusable -> if (status == "1") plexusDataList.add(plexusData)
                R.id.sort_acceptable -> if (status == "2") plexusDataList.add(plexusData)
                R.id.sort_good -> if (status == "3") plexusDataList.add(plexusData)
                R.id.sort_perfect -> if (status == "4") plexusDataList.add(plexusData)
            }
        }
    
        // Installed apps status sort
        fun installedAppsStatusSort(preferenceKey: Int, installedApp: InstalledApp,
                                    status: String, installedAppsList: ArrayList<InstalledApp>) {
            when (preferenceKey) {
                0, R.id.sort_not_tested -> if (status == "X") installedAppsList.add(installedApp)
                R.id.sort_unusable -> if (status == "1") installedAppsList.add(installedApp)
                R.id.sort_acceptable -> if (status == "2") installedAppsList.add(installedApp)
                R.id.sort_good -> if (status == "3") installedAppsList.add(installedApp)
                R.id.sort_perfect -> if (status == "4") installedAppsList.add(installedApp)
            }
        }
        
    }
    
}