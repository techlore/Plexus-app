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
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import retrofit2.awaitResponse
import tech.techlore.plexus.appmanager.ApplicationManager
import tech.techlore.plexus.dao.MainDataDao
import tech.techlore.plexus.models.get.main.MainData
import tech.techlore.plexus.models.get.scores.DgScore
import tech.techlore.plexus.models.get.scores.MgScore
import tech.techlore.plexus.utils.ListUtils.Companion.scannedInstalledAppsList

class MainDataRepository(private val mainDataDao: MainDataDao) {
    
    suspend fun plexusDataIntoDB(context: Context) {
        withContext(Dispatchers.IO) {
            val apiRepository = (context.applicationContext as ApplicationManager).apiRepository
            val appsCall = apiRepository.getApps()
            val appsResponse = appsCall.awaitResponse()
            
            if (appsResponse.isSuccessful) {
                appsResponse.body()?.let { root ->
                    for (mainData in root.mainData) {
                        
                        // De-googled score
                        val dgScoreCall = apiRepository.getDgScore(mainData.packageName)
                        val dgScoreResponse = dgScoreCall.awaitResponse()
                        
                        if (dgScoreResponse.isSuccessful) {
                            dgScoreResponse.body()?.let { dgScoreRoot ->
                                mainData.dgScore = DgScore(dgScoreRoot.dgScoreData.dgPkgName,
                                                           dgScoreRoot.dgScoreData.dgDenominator,
                                                           dgScoreRoot.dgScoreData.dgGoogleLib,
                                                           dgScoreRoot.dgScoreData.dgScore,
                                                           dgScoreRoot.dgScoreData.totalDgRatings)
                            }
                        }
                        
                        // MicroG score
                        val mgScoreCall = apiRepository.getMgScore(mainData.packageName)
                        val mgScoreResponse = mgScoreCall.awaitResponse()
                        
                        if (mgScoreResponse.isSuccessful) {
                            mgScoreResponse.body()?.let { mgScoreRoot ->
                                mainData.mgScore = MgScore(mgScoreRoot.mgScoreData.mgPkgName,
                                                           mgScoreRoot.mgScoreData.mgDenominator,
                                                           mgScoreRoot.mgScoreData.mgGoogleLib,
                                                           mgScoreRoot.mgScoreData.mgScore,
                                                           mgScoreRoot.mgScoreData.totalMgRatings)
                            }
                        }
                        
                        // Ratings
                        val ratingsCall = apiRepository.getRatings(mainData.packageName)
                        val ratingsResponse = ratingsCall.awaitResponse()
                        
                        if (ratingsResponse.isSuccessful) {
                            ratingsResponse.body()?.let { ratingsRoot ->
                                mainData.ratingsList = ratingsRoot.ratingsData
                            }
                        }
                        
                        mainDataDao.insertOrUpdatePlexusData(mainData)
                    }
                }
            }
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
    
    suspend fun updateIsInPlexusData(mainData: MainData) {
        return withContext(Dispatchers.IO) {
            val existingData = mainDataDao.getAppByPackage(mainData.packageName)
            if (existingData != null) {
                existingData.isInPlexusData = mainData.isInPlexusData
                mainDataDao.update(existingData)
            }
        }
    }
}