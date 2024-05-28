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
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import tech.techlore.plexus.R
import tech.techlore.plexus.appmanager.ApplicationManager
import tech.techlore.plexus.dao.MainDataDao
import tech.techlore.plexus.models.main.MainData
import tech.techlore.plexus.models.minimal.MainDataMinimal
import tech.techlore.plexus.preferences.PreferenceManager
import tech.techlore.plexus.preferences.PreferenceManager.Companion.DG_STATUS_SORT
import tech.techlore.plexus.preferences.PreferenceManager.Companion.MG_STATUS_SORT
import tech.techlore.plexus.utils.UiUtils.Companion.mapScoreRangeToStatusString
import tech.techlore.plexus.utils.UiUtils.Companion.mapStatusChipToScoreRange

class MainDataMinimalRepository(private val context: Context, private val mainDataDao: MainDataDao) {
    
    private val appManager = context.applicationContext as ApplicationManager
    
    private suspend fun mapToMinimalData(mainData: MainData): MainDataMinimal {
        return withContext(Dispatchers.IO) {
            
            MainDataMinimal(name = mainData.name,
                            packageName = mainData.packageName,
                            iconUrl = mainData.iconUrl ?: "",
                            installedFrom = mainData.installedFrom,
                            dgStatus = mapScoreRangeToStatusString(context, mainData.dgScore),
                            mgStatus = mapScoreRangeToStatusString(context, mainData.mgScore),
                            isInstalled = mainData.isInstalled,
                            isFav = mainData.isFav)
        }
    }
    
    suspend fun miniPlexusDataListFromDB(statusRadioPref: Int,
                                         orderPref: Int): ArrayList<MainDataMinimal> {
        return withContext(Dispatchers.IO) {
            
            val preferenceManager = appManager.preferenceManager
            
            val (dgScoreFrom, dgScoreTo) =
                getScoreRange(preferenceManager, statusRadioPref, R.id.radioDgStatus, DG_STATUS_SORT)
            
            val (mgScoreFrom, mgScoreTo) =
                getScoreRange(preferenceManager, statusRadioPref, R.id.radioMgStatus, MG_STATUS_SORT)
            
            val isAsc = orderPref != R.id.sortZA
            
            mainDataDao
                .getSortedPlexusDataApps(dgScoreFrom, dgScoreTo, mgScoreFrom, mgScoreTo, isAsc)
                .map { mapToMinimalData(it) }
                    as ArrayList<MainDataMinimal>
        }
    }
    
    suspend fun miniInstalledAppsListFromDB(installedFromPref: Int,
                                            statusRadioPref: Int,
                                            orderPref: Int): ArrayList<MainDataMinimal> {
        return withContext(Dispatchers.IO) {
            
            val preferenceManager = appManager.preferenceManager
            
            val installedFrom =
                when(installedFromPref) {
                    R.id.sortInstalledGooglePlayAlt -> "google_play_alternative"
                    R.id.sortInstalledFdroid -> "fdroid"
                    R.id.sortInstalledApk -> "apk"
                    else -> ""
                }
    
            val (dgScoreFrom, dgScoreTo) =
                getScoreRange(preferenceManager, statusRadioPref, R.id.radioDgStatus, DG_STATUS_SORT)
    
            val (mgScoreFrom, mgScoreTo) =
                getScoreRange(preferenceManager, statusRadioPref, R.id.radioMgStatus, MG_STATUS_SORT)
            
            val isAsc = orderPref != R.id.sortZA
            
            mainDataDao
                .getSortedInstalledApps(installedFrom, dgScoreFrom, dgScoreTo, mgScoreFrom, mgScoreTo, isAsc)
                .map { mapToMinimalData(it) }
                    as ArrayList<MainDataMinimal>
        }
    }
    
    suspend fun miniFavListFromDB(installedFromPref: Int,
                                  statusRadioPref: Int,
                                  orderPref: Int): ArrayList<MainDataMinimal> {
        return withContext(Dispatchers.IO) {
            
            val preferenceManager = appManager.preferenceManager
            
            val installedFrom =
                when(installedFromPref) {
                    R.id.sortInstalledGooglePlayAlt -> "google_play_alternative"
                    R.id.sortInstalledFdroid -> "fdroid"
                    R.id.sortInstalledApk -> "apk"
                    else -> ""
                }
    
            val (dgScoreFrom, dgScoreTo) =
                getScoreRange(preferenceManager, statusRadioPref, R.id.radioDgStatus, DG_STATUS_SORT)
    
            val (mgScoreFrom, mgScoreTo) =
                getScoreRange(preferenceManager, statusRadioPref, R.id.radioMgStatus, MG_STATUS_SORT)
            
            val isAsc = orderPref != R.id.sortZA
            
            mainDataDao.getSortedFavApps(installedFrom, dgScoreFrom, dgScoreTo, mgScoreFrom, mgScoreTo, isAsc)
                .map { mapToMinimalData(it) }
                    as ArrayList<MainDataMinimal>
        }
    }
    
    private fun getScoreRange(preferenceManager: PreferenceManager,
                              statusRadioPref: Int,
                              radioBtnId: Int,
                              sortKey: String): Pair<Float, Float> {
        return if (statusRadioPref == radioBtnId) {
            mapStatusChipToScoreRange(preferenceManager, sortKey)
        } else {
            Pair(-1.0f, -1.0f)
        }
    }
    
    suspend fun updateFav(mainDataMinimal: MainDataMinimal) {
        return withContext(Dispatchers.IO) {
            val existingData = mainDataDao.getAppByPackage(mainDataMinimal.packageName)
            existingData.apply {
                if (this != null) {
                    isFav = mainDataMinimal.isFav
                    mainDataDao.update(this)
                }
            }
        }
    }
    
    suspend fun searchFromDb(searchQuery: String): ArrayList<MainDataMinimal> {
        return withContext(Dispatchers.IO) {
            mainDataDao
                .searchFromDb(searchQuery)
                .map { mapToMinimalData(it) }
                    as ArrayList<MainDataMinimal>
        }
    }
}