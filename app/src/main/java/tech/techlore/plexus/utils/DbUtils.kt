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
import androidx.room.Room
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import retrofit2.awaitResponse
import tech.techlore.plexus.dao.InstalledAppsDao
import tech.techlore.plexus.dao.PlexusDataDao
import tech.techlore.plexus.database.MainDatabase
import tech.techlore.plexus.models.InstalledApp
import tech.techlore.plexus.models.PlexusData
import tech.techlore.plexus.utils.ApiUtils.Companion.createService
import tech.techlore.plexus.utils.ListUtils.Companion.scannedInstalledAppsList

class DbUtils {
    
    companion object {
    
        @Volatile
        private var INSTANCE: MainDatabase? = null
    
        fun getDatabase(context: Context): MainDatabase {
            return INSTANCE ?: synchronized(this) {
                val instance = Room.databaseBuilder(context.applicationContext,
                                                    MainDatabase::class.java,
                                                    "main_database").build()
            
                INSTANCE = instance
                instance
            }
        }
        
        suspend fun plexusDataIntoDB(plexusDataDao: PlexusDataDao) {
            return withContext(Dispatchers.IO) {
                val call = createService().getApplications()
                val response = call.awaitResponse()
                
                if (response.isSuccessful) {
                    response.body()?.let {
                        for (data in it.data) {
                            plexusDataDao.insertOrUpdate(data)
                        }
                    }
                }
            }
        }
        
        suspend fun installedAppsIntoDB(context: Context, installedAppsDao: InstalledAppsDao) {
            return withContext(Dispatchers.IO) {
    
                val installedApps = scannedInstalledAppsList(context)
                val databaseApps = installedAppsListFromDB(installedAppsDao)
    
                // Find uninstalled apps
                val uninstalledApps = databaseApps.filterNot { databaseApp ->
                    installedApps.any { installedApp ->
                        installedApp.packageName == databaseApp.packageName
                    }
                }
    
                // Delete uninstalled apps from db
                uninstalledApps.forEach {
                    installedAppsDao.delete(it)
                }
    
                // Insert/update new data
                installedApps.forEach {
                    installedAppsDao.insertOrUpdate(it)
                }
                
            }
        }
    
        suspend fun plexusDataListFromDB(plexusDataDao: PlexusDataDao): ArrayList<PlexusData> {
            return withContext(Dispatchers.IO) {
                plexusDataDao.getAll() as ArrayList<PlexusData>
            }
        }
    
        suspend fun installedAppsListFromDB(installedAppsDao: InstalledAppsDao): ArrayList<InstalledApp> {
            return withContext(Dispatchers.IO) {
                installedAppsDao.getAll() as ArrayList<InstalledApp>
            }
        }
        
    }
}