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

import android.content.Context
import android.content.pm.ApplicationInfo
import android.content.pm.PackageManager
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import retrofit2.await
import retrofit2.awaitResponse
import tech.techlore.plexus.R
import tech.techlore.plexus.models.InstalledApp
import tech.techlore.plexus.models.PlexusData
import tech.techlore.plexus.utils.ApiUtils.Companion.createService

class ListUtils {
    
    companion object {
    
        // Populate Plexus data list
        suspend fun populateDataList(): ArrayList<PlexusData> {
            return withContext(Dispatchers.IO) {
                val call = createService().getApplications()
                val response = call.awaitResponse()
                val root = response.body()!!
                root.data
            }
        }
    
        // Scan all installed apps and populate respective list
        fun scanInstalledApps(context: Context,
                              plexusDataList: ArrayList<PlexusData>,
                              installedAppsList: ArrayList<InstalledApp>) {
            
            val packageManager = context.packageManager
            
            for (appInfo in packageManager.getInstalledApplications(PackageManager.GET_META_DATA)) {
                
                val installedApp = InstalledApp()
                val plexusVersion = "NA"
                val dgStatus = "X"
                val mgStatus = "X"
                val dgNotes = "X"
                val mgNotes = "X"
            
                // No system apps
                // Only scan for user installed apps
                // OR system apps updated by user
                if (appInfo.flags and ApplicationInfo.FLAG_SYSTEM != 1
                    || appInfo.flags and ApplicationInfo.FLAG_UPDATED_SYSTEM_APP != 0) {
                    
                    installedApp.name = appInfo.loadLabel(packageManager).toString()
                    installedApp.packageName = appInfo.packageName
                    try {
                        val packageInfo = packageManager.getPackageInfo(appInfo.packageName, 0)
                        installedApp.installedVersion = packageInfo.versionName
                    }
                    catch (e: PackageManager.NameNotFoundException) {
                        e.printStackTrace()
                    }
                
                    // Search for package name in Plexus data
                    // To set Plexus version, status & notes
                    for (plexusData in plexusDataList) {
                        if (plexusData.packageName.contains(appInfo.packageName)) {
//                        plexusVersion = plexusData.version;
//                        dgStatus = plexusData.dgStatus;
//                        mgStatus = plexusData.mgStatus;
//                        dgNotes = plexusData.dgNotes;
//                        mgNotes = plexusData.mgNotes;
                        }
                    }
                    
                    installedApp.plexusVersion = plexusVersion
                    installedApp.dgRating = dgStatus
                    installedApp.mgRating = mgStatus
                    installedApp.dgNotes = dgNotes
                    installedApp.mgNotes = mgNotes
                    installedAppsList.add(installedApp)
                }
            }
        }
    
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