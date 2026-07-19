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

import android.content.Context
import androidx.paging.Pager
import androidx.paging.PagingConfig
import androidx.paging.PagingData
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.async
import kotlinx.coroutines.awaitAll
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.withContext
import org.koin.core.component.KoinComponent
import org.koin.core.component.get
import org.koin.core.component.inject
import org.koin.core.qualifier.named
import tech.techlore.plexus.R
import tech.techlore.plexus.dao.MainDataDao
import tech.techlore.plexus.models.get.apps.GetAppsRoot
import tech.techlore.plexus.models.main.MainData
import tech.techlore.plexus.models.mini.MainDataMini
import tech.techlore.plexus.objects.DataState
import tech.techlore.plexus.preferences.PreferenceManager
import tech.techlore.plexus.preferences.PreferenceManager.Companion.DG_STATUS_SORT
import tech.techlore.plexus.preferences.PreferenceManager.Companion.LAST_UPDATED
import tech.techlore.plexus.preferences.PreferenceManager.Companion.MG_STATUS_SORT
import tech.techlore.plexus.repositories.api.ApiRepository
import tech.techlore.plexus.utils.PackageUtils.Companion.scannedInstalledAppsList
import tech.techlore.plexus.utils.ScoreUtils.Companion.truncatedScore
import tech.techlore.plexus.utils.UiUtils.Companion.mapStatusChipToScoreRange
import java.time.format.DateTimeFormatter
import java.util.Date

class MainDataRepository(private val mainDataDao: MainDataDao): KoinComponent {
    
    private val apiRepository by inject<ApiRepository>()
    private val prefManager by inject<PreferenceManager>()
    private val pagingConfig = PagingConfig(pageSize = 25, prefetchDistance = 10, enablePlaceholders = false)
    
    suspend fun plexusDataIntoDB() {
        withContext(Dispatchers.IO) {
            val lastUpdated = prefManager.getString(LAST_UPDATED)
            val currentDateTime = Date()
            val appsResponse = apiRepository.getAppsWithScores(pageNumber = 1, lastUpdated)
            
            // Insert/update all apps in db
            onRequestSuccessful(appsResponse)
            
            // Retrieve remaining apps in parallel
            if (appsResponse.meta.totalPages > 1) {
                val maxThreads = 6
                (2 .. appsResponse.meta.totalPages step maxThreads).forEach {
                    val lastPageInBatch = minOf(it + maxThreads - 1).coerceAtMost(appsResponse.meta.totalPages)
                    (it .. lastPageInBatch).map { pageNumber ->
                        async {
                            onRequestSuccessful(apiRepository.getAppsWithScores(pageNumber, lastUpdated))
                        }
                    }.awaitAll() // Wait for all requests to complete
                }
            }
            
            currentDateTime.let {
                DataState.lastFullDataUpdateTimeMs = it.time
                prefManager.setString(
                    LAST_UPDATED,
                    get<DateTimeFormatter>(named("formattedLastUpdatedDate"))
                        .format(it.toInstant())
                )
            }
        }
    }
    
    private suspend fun onRequestSuccessful(getAppsRoot: GetAppsRoot) {
        // Insert/update all apps in db
        getAppsRoot.appData.forEach { appData ->
            mainDataDao.insertOrUpdatePlexusData(
                MainData(name = appData.name,
                         packageName = appData.packageName,
                         iconUrl = appData.iconUrl.orEmpty(),
                         dgScore = appData.scoresRoot.dgScore.score.truncatedScore(),
                         totalDgRatings = appData.scoresRoot.dgScore.totalRatings,
                         mgScore = appData.scoresRoot.mgScore.score.truncatedScore(),
                         totalMgRatings = appData.scoresRoot.mgScore.totalRatings)
            )
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
    
    private fun getScoreRange(statusToggleBtnPref: Int,
                              toggleBtnId: Int,
                              sortChipId: Int): Pair<Float, Float> {
        return if (statusToggleBtnPref == toggleBtnId) mapStatusChipToScoreRange(sortChipId)
        else Pair(-1.0f, -1.0f)
    }
    
    fun miniPlexusDataListFromDB(statusToggleBtnPref: Int,
                                 orderPref: Int): Flow<PagingData<MainDataMini>> {
        val (dgScoreFrom, dgScoreTo) =
            getScoreRange(statusToggleBtnPref, R.id.toggleDgStatus, prefManager.getInt(DG_STATUS_SORT))
        
        val (mgScoreFrom, mgScoreTo) =
            getScoreRange(statusToggleBtnPref, R.id.toggleMgStatus, prefManager.getInt(MG_STATUS_SORT))
        
        val isAsc = orderPref != R.id.sortZA
        
        return Pager(
            config = pagingConfig,
            pagingSourceFactory = {
                mainDataDao.getSortedPlexusDataApps(dgScoreFrom, dgScoreTo, mgScoreFrom, mgScoreTo, isAsc)
            }
        ).flow
    }
    
    fun miniInstalledAppsListFromDB(installedFromPref: Int,
                                    statusToggleBtnPref: Int,
                                    orderPref: Int): Flow<PagingData<MainDataMini>> {
        
        val installedFrom =
            when(installedFromPref) {
                R.id.sortInstalledGooglePlayAlt -> "google_play_alternative"
                R.id.sortInstalledFdroid -> "fdroid"
                R.id.sortInstalledApk -> "apk"
                else -> ""
            }
        
        val (dgScoreFrom, dgScoreTo) =
            getScoreRange(statusToggleBtnPref, R.id.toggleDgStatus, prefManager.getInt(DG_STATUS_SORT))
        
        val (mgScoreFrom, mgScoreTo) =
            getScoreRange(statusToggleBtnPref, R.id.toggleMgStatus, prefManager.getInt(MG_STATUS_SORT))
        
        val isAsc = orderPref != R.id.sortZA
        
        return Pager(
            config = pagingConfig,
            pagingSourceFactory = {
                mainDataDao.getSortedInstalledApps(installedFrom, dgScoreFrom, dgScoreTo, mgScoreFrom, mgScoreTo, isAsc)
            }
        ).flow
    }
    
    fun miniFavListFromDB(installedFromPref: Int,
                          statusToggleBtnPref: Int,
                          orderPref: Int): Flow<PagingData<MainDataMini>> {
        
        val installedFrom =
            when(installedFromPref) {
                R.id.sortInstalledGooglePlayAlt -> "google_play_alternative"
                R.id.sortInstalledFdroid -> "fdroid"
                R.id.sortInstalledApk -> "apk"
                else -> ""
            }
        
        val (dgScoreFrom, dgScoreTo) =
            getScoreRange(statusToggleBtnPref, R.id.toggleDgStatus, prefManager.getInt(DG_STATUS_SORT))
        
        val (mgScoreFrom, mgScoreTo) =
            getScoreRange(statusToggleBtnPref, R.id.toggleMgStatus, prefManager.getInt(MG_STATUS_SORT))
        
        val isAsc = orderPref != R.id.sortZA
        
        return Pager(
            config = pagingConfig,
            pagingSourceFactory = {
                mainDataDao.getSortedFavApps(installedFrom, dgScoreFrom, dgScoreTo, mgScoreFrom, mgScoreTo, isAsc)
            }
        ).flow
    }
    
    suspend fun updateFav(packageName: String, isFav: Boolean) {
        return withContext(Dispatchers.IO) {
            mainDataDao.updateFav(packageName, isFav)
        }
    }
    
    fun searchInDb(searchQuery: String, orderChipId: Int): Flow<PagingData<MainDataMini>> {
        return Pager(
            config = pagingConfig,
            pagingSourceFactory = {
                mainDataDao.searchInDb(searchQuery.trim(), orderChipId != R.id.sortZA)
            }
        ).flow
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
                         iconUrl = appData.iconUrl.orEmpty(),
                         dgScore = appData.scoresRoot.dgScore.score.truncatedScore(),
                         totalDgRatings = appData.scoresRoot.dgScore.totalRatings,
                         mgScore = appData.scoresRoot.mgScore.score.truncatedScore(),
                         totalMgRatings = appData.scoresRoot.mgScore.totalRatings,
                         isInPlexusData = true)
            )
        }
    }
    
}