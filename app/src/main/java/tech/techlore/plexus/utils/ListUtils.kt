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
import android.content.pm.ApplicationInfo
import android.content.pm.PackageManager
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import tech.techlore.plexus.R
import tech.techlore.plexus.models.main.MainData

class ListUtils {
    
    companion object {
        
        suspend fun scannedInstalledAppsList(context: Context): List<MainData> {
            return withContext(Dispatchers.IO) {
                
                val packageManager = context.packageManager
                val installedAppsList = ArrayList<MainData>()
                
                packageManager.getInstalledApplications(PackageManager.GET_META_DATA)
                    // No system apps
                    // Only scan for user installed apps
                    // OR system apps updated by user
                    .filter {
                        it.flags and ApplicationInfo.FLAG_SYSTEM != 1
                        || it.flags and ApplicationInfo.FLAG_UPDATED_SYSTEM_APP !=0
                    }
                    .mapNotNull {
                        val installedApp =
                            MainData(name = it.loadLabel(packageManager).toString(),
                                     packageName = it.packageName,
                                     installedVersion =
                                     packageManager.getPackageInfo(it.packageName, 0).versionName,
                                     installedBuild = packageManager.getPackageInfo(it.packageName, 0).versionCode,
                                     installedFrom =
                                     when(packageManager.getInstallerPackageName(it.packageName)) {
                                         "com.android.vending", "com.aurora.store" -> "googlePlay"
                                         "org.fdroid.fdroid" -> "fdroid"
                                         else -> "other"
                                     },
                                     isInstalled = true)
                        
                        installedAppsList.add(installedApp)
                    }
                
                installedAppsList
            }
        }
        
        // Data status sort
        fun statusSort(preferenceKey: Int, mainData: MainData,
                       status: String, mainDataList: ArrayList<MainData>) {
            
            when (preferenceKey) {
                0, R.id.sort_not_tested -> if (status == "X") mainDataList.add(mainData)
                R.id.sort_broken -> if (status == "1") mainDataList.add(mainData)
                R.id.sort_bronze -> if (status == "2") mainDataList.add(mainData)
                R.id.sort_silver -> if (status == "3") mainDataList.add(mainData)
                R.id.sort_gold -> if (status == "4") mainDataList.add(mainData)
            }
        }
        
    }
}