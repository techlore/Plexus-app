/*
 *     Copyright (C) 2022-present Techlore
 *
 *     This program is free software: you can redistribute it and/or modify
 *     it under the terms of the GNU General Public License as published by
 *     the Free Software Foundation, either version 3 of the License, or
 *     (at your option) any later version.
 *
 *     This program is distributed in the hope that it will be useful,
 *     but WITHOUT ANY WARRANTY; without even the implied warranty of
 *     MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 *     GNU General Public License for more details.
 *
 *     You should have received a copy of the GNU General Public License
 *     along with this program.  If not, see <https://www.gnu.org/licenses/>.
 */

package tech.techlore.plexus.repositories.database

import android.annotation.SuppressLint
import android.content.Context
import kotlinx.coroutines.Deferred
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.async
import kotlinx.coroutines.awaitAll
import kotlinx.coroutines.withContext
import org.koin.core.component.KoinComponent
import org.koin.core.component.inject
import tech.techlore.plexus.dao.MainDataDao
import tech.techlore.plexus.models.get.apps.GetAppsRoot
import tech.techlore.plexus.models.main.MainData
import tech.techlore.plexus.preferences.PreferenceManager
import tech.techlore.plexus.preferences.PreferenceManager.Companion.LAST_UPDATED
import tech.techlore.plexus.repositories.api.ApiRepository
import tech.techlore.plexus.utils.PackageUtils.Companion.scannedInstalledAppsList
import tech.techlore.plexus.utils.ScoreUtils.Companion.truncatedScore
import java.text.SimpleDateFormat
import java.util.Date
import java.util.TimeZone

class MainDataRepository(private val mainDataDao: MainDataDao): KoinComponent {
    
    private val apiRepository by inject<ApiRepository>()
    
    suspend fun plexusDataIntoDB() {
        withContext(Dispatchers.IO) {
            val prefManager by inject<PreferenceManager>()
            val lastUpdated = prefManager.getString(LAST_UPDATED)
            val currentDateTime = Date()
            val appsResponse = apiRepository.getAppsWithScores(pageNumber = 1, lastUpdated)
            
            // Insert/update all apps in db
            onRequestSuccessful(appsResponse)
            // Retrieve remaining apps in parallel
            if (appsResponse.meta.totalPages > 1) {
                val requests = mutableListOf<Deferred<Unit>>()
                (2 .. appsResponse.meta.totalPages).forEach { pageNumber ->
                    val request =
                        async {
                            val remAppsResponse = apiRepository.getAppsWithScores(pageNumber, lastUpdated)
                            onRequestSuccessful(remAppsResponse)
                        }
                    requests.add(request)
                }
                requests.awaitAll() // Wait for all requests to complete
            }
            
            prefManager.setString(LAST_UPDATED, currentDateTime.formatRFC3339())
        }
    }
    
    private suspend fun onRequestSuccessful(getAppsRoot: GetAppsRoot) {
        // Insert/update all apps in db
        getAppsRoot.appData.forEach { appData ->
            mainDataDao.insertOrUpdatePlexusData(
                MainData(name = appData.name,
                         packageName = appData.packageName,
                         iconUrl = appData.iconUrl ?: "",
                         dgScore = appData.scoresRoot.dgScore.score.truncatedScore(),
                         totalDgRatings = appData.scoresRoot.dgScore.totalRatings,
                         mgScore = appData.scoresRoot.mgScore.score.truncatedScore(),
                         totalMgRatings = appData.scoresRoot.mgScore.totalRatings)
            )
        }
    }
    
    @SuppressLint("SimpleDateFormat")
    private fun Date.formatRFC3339(): String {
        return SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss'Z'").apply {
            timeZone = TimeZone.getTimeZone("UTC")
        }.format(this)
    }
    
    suspend fun deleteNonRatedAppsFromDb() {
        // Apps with no ratings were removed from Plexus DB recently,
        // so delete those apps from local DB too.
        // This will be removed after a few versions, as by then local DB won't have any such apps
        withContext(Dispatchers.IO) {
            mainDataDao.getNonRatedPlexusDataApps().takeIf { it.isNotEmpty() }?.forEach {
                mainDataDao.delete(it)
            }
        }
    }
    
    suspend fun installedAppsIntoDB(context: Context) {
        withContext(Dispatchers.IO) {
            val installedApps = scannedInstalledAppsList(context)
            val installedAppsPackageNames = installedApps.map { it.packageName }.toSet()
            
            // Find & delete uninstalled apps from db
            mainDataDao.getInstalledApps().forEach {
                if (it.packageName !in installedAppsPackageNames) {
                    when {
                        !it.isInPlexusData-> mainDataDao.delete(it)
                        else ->
                            it.apply {
                                isInstalled = false
                                installedVersion = ""
                                installedFrom = ""
                                mainDataDao.update(it)
                            }
                    }
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
            mainDataDao.getAppByPackage(packageName)
        }
    }
    
    suspend fun updateSingleApp(packageName: String) {
        withContext(Dispatchers.IO) {
            val singleAppResponse = apiRepository.getSingleAppWithScores(packageName)
            val appData = singleAppResponse.appData
            mainDataDao.insertOrUpdatePlexusData(
                MainData(name = appData.name,
                         packageName = appData.packageName,
                         iconUrl = appData.iconUrl ?: "",
                         dgScore = appData.scoresRoot.dgScore.score.truncatedScore(),
                         totalDgRatings = appData.scoresRoot.dgScore.totalRatings,
                         mgScore = appData.scoresRoot.mgScore.score.truncatedScore(),
                         totalMgRatings = appData.scoresRoot.mgScore.totalRatings,
                         isInPlexusData = true)
            )
        }
    }
    
}