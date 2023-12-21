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
import coil.ImageLoader
import coil.request.ImageRequest
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
import tech.techlore.plexus.utils.PackageUtils.Companion.scannedInstalledAppsList
import tech.techlore.plexus.utils.ScoreUtils.Companion.truncatedScore

class MainDataRepository(private val mainDataDao: MainDataDao) {
    
    suspend fun plexusDataIntoDB(context: Context) {
        withContext(Dispatchers.IO) {
            val apiRepository = (context.applicationContext as ApplicationManager).apiRepository
            val appsCall = apiRepository.getAppsWithScores(pageNumber = 1)
            val appsResponse = appsCall.awaitResponse()
            val imageLoader = ImageLoader.Builder(context).build()
            val imageRequest = ImageRequest.Builder(context)
            
            if (appsResponse.isSuccessful) {
                appsResponse.body()?.let { getAppsRoot ->
                    
                    // Insert/update all apps in db
                    onRequestSuccessful(getAppsRoot, imageLoader, imageRequest)
                    
                    // Retrieve remaining apps in parallel
                    if (getAppsRoot.meta.totalPages > 1) {
                        val requests = mutableListOf<Deferred<Unit>>()
                        for (pageNumber in 2 .. getAppsRoot.meta.totalPages) {
                            val request = async {
                                val remAppsCall = apiRepository.getAppsWithScores(pageNumber)
                                val remAppsResponse = remAppsCall.awaitResponse()
                                if (remAppsResponse.isSuccessful) {
                                    remAppsResponse.body()?.let { root ->
                                        onRequestSuccessful(root, imageLoader, imageRequest)
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
                                            imageLoader: ImageLoader,
                                            imageRequest: ImageRequest.Builder) {
        // Insert/update all apps in db
        getAppsRoot.appData.forEach { appData ->
            
            appData.iconUrl?.let { iconUrl ->
                val preloadRequest =
                    imageRequest
                        .data(iconUrl)
                        .size(150, 150)
                        .build()
                imageLoader.enqueue(preloadRequest)
            }
            
            mainDataDao
                .insertOrUpdatePlexusData(MainData(name = appData.name,
                                                   packageName = appData.packageName,
                                                   iconUrl = appData.iconUrl ?: "",
                                                   dgScore = truncatedScore(appData.scoresRoot.dgScore.score),
                                                   totalDgRatings = appData.scoresRoot.dgScore.totalRatings,
                                                   mgScore = truncatedScore(appData.scoresRoot.mgScore.score),
                                                   totalMgRatings = appData.scoresRoot.mgScore.totalRatings))
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
                if (!it.isInPlexusData) {
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
    
    suspend fun updateSingleApp(context: Context, packageName: String) {
        withContext(Dispatchers.IO) {
            val apiRepository = (context.applicationContext as ApplicationManager).apiRepository
            val singleAppCall = apiRepository.getSingleAppWithScores(packageName)
            val singleAppResponse = singleAppCall.awaitResponse()
            if (singleAppResponse.isSuccessful) {
                singleAppResponse.body()?.let { getSingleAppRoot ->
                    val appData = getSingleAppRoot.appData
                    mainDataDao
                        .insertOrUpdatePlexusData(MainData(name = appData.name,
                                                           packageName = appData.packageName,
                                                           iconUrl = appData.iconUrl ?: "",
                                                           dgScore = truncatedScore(appData.scoresRoot.dgScore.score),
                                                           totalDgRatings = appData.scoresRoot.dgScore.totalRatings,
                                                           mgScore = truncatedScore(appData.scoresRoot.mgScore.score),
                                                           totalMgRatings = appData.scoresRoot.mgScore.totalRatings,
                                                           isInPlexusData = true))
                }
            }
        }
    }
    
}