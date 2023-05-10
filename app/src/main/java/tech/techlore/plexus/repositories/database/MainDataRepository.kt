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

package tech.techlore.plexus.repositories.database

import android.content.Context
import com.bumptech.glide.Glide
import com.bumptech.glide.RequestManager
import com.bumptech.glide.load.engine.DiskCacheStrategy
import kotlinx.coroutines.Deferred
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.async
import kotlinx.coroutines.awaitAll
import kotlinx.coroutines.withContext
import retrofit2.awaitResponse
import tech.techlore.plexus.appmanager.ApplicationManager
import tech.techlore.plexus.dao.MainDataDao
import tech.techlore.plexus.models.get.apps.GetAppsRoot
import tech.techlore.plexus.models.main.MainData
import tech.techlore.plexus.utils.DbUtils.Companion.truncatedDgScore
import tech.techlore.plexus.utils.DbUtils.Companion.truncatedMgScore
import tech.techlore.plexus.utils.ListUtils.Companion.scannedInstalledAppsList

class MainDataRepository(private val mainDataDao: MainDataDao) {
    
    suspend fun plexusDataIntoDB(context: Context) {
        withContext(Dispatchers.IO) {
            val apiRepository = (context.applicationContext as ApplicationManager).apiRepository
            val appsCall = apiRepository.getAppsWithScores(pageNumber = 1)
            val appsResponse = appsCall.awaitResponse()
            val requestManager = Glide.with(context)
            
            if (appsResponse.isSuccessful) {
                appsResponse.body()?.let { getAppsRoot ->
                    
                    // Insert/update all apps in db
                    onRequestSuccessful(getAppsRoot, requestManager)
                    
                    // Retrieve remaining apps in parallel
                    if (getAppsRoot.meta.totalPages > 1) {
                        val requests = mutableListOf<Deferred<Unit>>()
                        for (pageNumber in 2 .. getAppsRoot.meta.totalPages) {
                            val request = async {
                                val remAppsCall = apiRepository.getAppsWithScores(pageNumber)
                                val remAppsResponse = remAppsCall.awaitResponse()
                                if (remAppsResponse.isSuccessful) {
                                    remAppsResponse.body()?.let { root ->
                                        onRequestSuccessful(root, requestManager)
                                    }
                                }
                
                            }
                            requests.add(request)
                        }
        
                        // Wait for all requests to complete
                        requests.awaitAll()
                    }
                }
            }
        }
    }
    
    private suspend fun onRequestSuccessful(getAppsRoot: GetAppsRoot,
                                            glideRequestManager: RequestManager) {
        // Insert/update all apps in db
        for (appData in getAppsRoot.appData) {
            
            appData.iconUrl?.let {
                // Preload icon into cache
                glideRequestManager
                    .load(appData.iconUrl)
                    .diskCacheStrategy(DiskCacheStrategy.ALL) // Cache strategy
                    .preload()
            }
            
            mainDataDao
                .insertOrUpdatePlexusData(MainData(name = appData.name,
                                                   packageName = appData.packageName,
                                                   iconUrl = appData.iconUrl ?: "",
                                                   dgScore = truncatedDgScore(appData.scores[1].score),
                                                   totalDgRatings = appData.scores[1].totalRatings,
                                                   mgScore = truncatedMgScore(appData.scores[0].score),
                                                   totalMgRatings = appData.scores[0].totalRatings))
        }
    }
    
    suspend fun installedAppsIntoDB(context: Context) {
        withContext(Dispatchers.IO) {
        
            val installedApps = scannedInstalledAppsList(context)
            val databaseApps = mainDataDao.getInstalledApps() as ArrayList<MainData>
        
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
    
    suspend fun getAppByPackage(packageName: String): MainData? {
        return withContext(Dispatchers.IO){
            mainDataDao.getAppByPackage (packageName)
        }
    }
    
    suspend fun insertOrUpdatePlexusData(mainData: MainData) {
        return withContext(Dispatchers.IO) {
            mainDataDao.insertOrUpdatePlexusData(mainData)
        }
    }
    
    suspend fun updateIsInPlexusData(mainData: MainData) {
        return withContext(Dispatchers.IO) {
            mainDataDao.updateIsInPlexusData(mainData)
        }
    }
}