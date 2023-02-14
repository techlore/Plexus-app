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

package tech.techlore.plexus.repositories

import android.content.Context
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import retrofit2.awaitResponse
import tech.techlore.plexus.dao.MainDataDao
import tech.techlore.plexus.models.main.MainData
import tech.techlore.plexus.utils.ApiUtils.Companion.createService
import tech.techlore.plexus.utils.ListUtils.Companion.scannedInstalledAppsList

class MainDataRepository(private val mainDataDao: MainDataDao) {
    
    suspend fun plexusDataIntoDB() {
        withContext(Dispatchers.IO) {
            val call = createService().getApplications()
            val response = call.awaitResponse()
            
            if (response.isSuccessful) {
                response.body()?.let {
                    for (data in it.data) {
                        mainDataDao.insertOrUpdatePlexusData(data)
                    }
                }
            }
        }
    }
    
    suspend fun installedAppsIntoDB(context: Context) {
        withContext(Dispatchers.IO) {
        
            val installedApps = scannedInstalledAppsList(context)
            val databaseApps = installedAppsListFromDB()
        
            // Find uninstalled apps
            val uninstalledApps = databaseApps.filterNot { databaseApp ->
                installedApps.any { installedApp ->
                    installedApp.packageName == databaseApp.packageName
                }
            }
    
            // Delete uninstalled apps from db
            uninstalledApps.forEach {
                if (! it.isInPlexusData) {
                    mainDataDao.delete(it)
                }
                else {
                    it.isInstalled = false
                    it.installedVersion = ""
                    it.installedFrom = ""
                    mainDataDao.update(it)
                }
            }
        
            // Insert/update new data
            installedApps.forEach {
                mainDataDao.insertOrUpdateInstalledApps(it)
            }
        }
    }
    
    suspend fun plexusDataListFromDB(): ArrayList<MainData> {
        return withContext(Dispatchers.IO) {
            mainDataDao.getNotInstalledApps() as ArrayList<MainData>
        }
    }
    
    suspend fun installedAppsListFromDB(): ArrayList<MainData> {
        return withContext(Dispatchers.IO) {
            mainDataDao.getInstalledApps() as ArrayList<MainData>
        }
    }
    
    suspend fun favListFromDB(): ArrayList<MainData> {
        return withContext(Dispatchers.IO) {
            mainDataDao.getFavApps() as ArrayList<MainData>
        }
    }
    
    fun getAppByPackage(packageName: String): MainData? {
        return mainDataDao.getAppByPackage(packageName)
    }
    
    fun getNotInstalledAppByPackage(packageName: String): MainData? {
        return mainDataDao.getNotInstalledAppByPackage(packageName)
    }
    
    fun getInstalledAppByPackage(packageName: String): MainData? {
        return mainDataDao.getInstalledAppByPackage(packageName)
    }
    
    suspend fun insert(mainData: MainData) {
        mainDataDao.insert(mainData)
    }
    
    suspend fun update(mainData: MainData) {
        mainDataDao.update(mainData)
    }
    
    suspend fun insertOrUpdatePlexusData(mainData: MainData) {
        mainDataDao.insertOrUpdatePlexusData(mainData)
    }
    
    suspend fun insertOrUpdateInstalledApps(mainData: MainData) {
        mainDataDao.insertOrUpdateInstalledApps(mainData)
    }
    
    suspend fun updateFavApps(mainData: MainData) {
        mainDataDao.updateFavApps(mainData)
    }
    
    suspend fun delete(mainData: MainData) {
        mainDataDao.delete(mainData)
    }
}