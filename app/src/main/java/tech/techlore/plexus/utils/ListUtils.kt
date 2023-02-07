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
import tech.techlore.plexus.dao.InstalledAppsDao
import tech.techlore.plexus.dao.PlexusDataDao
import tech.techlore.plexus.models.InstalledApp
import tech.techlore.plexus.models.PlexusData

class ListUtils {
    
    companion object {
        
        suspend fun scannedInstalledAppsList(context: Context): List<InstalledApp> {
            return withContext(Dispatchers.IO) {
                val packageManager = context.packageManager
                val installedAppsList = ArrayList<InstalledApp>()
    
                for (appInfo in packageManager.getInstalledApplications(PackageManager.GET_META_DATA)) {
        
                    val installedApp = InstalledApp()
        
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
                        installedApp.installedFrom = packageManager.getInstallerPackageName(appInfo.packageName)
            
                        installedAppsList.add(installedApp)
                    }
                }
                installedAppsList
            }
        }
    
        suspend fun getPlexusDataList(plexusDataDao: PlexusDataDao): ArrayList<PlexusData> {
            return withContext(Dispatchers.IO) {
                plexusDataDao.getAll() as ArrayList<PlexusData>
            }
        }
    
        suspend fun getInstalledAppsList(installedAppsDao: InstalledAppsDao): ArrayList<InstalledApp> {
            return withContext(Dispatchers.IO) {
                installedAppsDao.getAll() as ArrayList<InstalledApp>
            }
        }
        
    }
}