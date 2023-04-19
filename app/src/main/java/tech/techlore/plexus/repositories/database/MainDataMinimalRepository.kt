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
import tech.techlore.plexus.R
import tech.techlore.plexus.dao.MainDataDao
import tech.techlore.plexus.models.main.MainData
import tech.techlore.plexus.models.minimal.MainDataMinimal
import tech.techlore.plexus.preferences.PreferenceManager
import tech.techlore.plexus.preferences.PreferenceManager.Companion.DG_STATUS_SORT
import tech.techlore.plexus.preferences.PreferenceManager.Companion.MG_STATUS_SORT

class MainDataMinimalRepository(private val mainDataDao: MainDataDao) {
    
    private suspend fun mapToMinimalData(mainData: MainData): MainDataMinimal {
        return withContext(Dispatchers.IO) {
            MainDataMinimal(mainData.name,
                            mainData.packageName,
                            mainData.installedFrom,
                            mainData.isInstalled,
                            mainData.isFav)
        }
    }
    
    suspend fun miniPlexusDataListFromDB(context: Context,
                                         statusRadioPref: Int,
                                         orderPref: Int): ArrayList<MainDataMinimal> {
        return withContext(Dispatchers.IO) {
            
            val preferenceManager = PreferenceManager(context)
            
            val (dgScoreFrom, dgScoreTo) =
                when (statusRadioPref) {
                    R.id.radio_dg_status ->
                        when (preferenceManager.getInt(DG_STATUS_SORT)) {
                            R.id.sort_not_tested -> Pair(0.0,0.0)
                            R.id.sort_broken -> Pair(1.0,1.9)
                            R.id.sort_bronze -> Pair(2.0,2.9)
                            R.id.sort_silver -> Pair(3.0,3.9)
                            R.id.sort_gold -> Pair(4.0,4.0)
                            else -> Pair(-1.0,-1.0)
                        }
                    else -> Pair(-1.0,-1.0)
                }
            
            val (mgScoreFrom, mgScoreTo) =
                when (statusRadioPref) {
                    R.id.radio_mg_status ->
                        when (preferenceManager.getInt(MG_STATUS_SORT)) {
                            R.id.sort_not_tested -> Pair(0.0,0.0)
                            R.id.sort_broken -> Pair(1.0,1.9)
                            R.id.sort_bronze -> Pair(2.0,2.9)
                            R.id.sort_silver -> Pair(3.0,3.9)
                            R.id.sort_gold -> Pair(4.0,4.0)
                            else -> Pair(-1.0,-1.0)
                        }
                    else -> Pair(-1.0,-1.0)
                }
            
            val isAsc = orderPref != R.id.sort_z_a
            
            mainDataDao.getSortedNotInstalledApps(dgScoreFrom,
                                                  dgScoreTo,
                                                  mgScoreFrom,
                                                  mgScoreTo,
                                                  isAsc).map {
                mapToMinimalData(it)
            } as ArrayList<MainDataMinimal>
        }
    }
    
    suspend fun miniInstalledAppsListFromDB(context: Context,
                                            filterPref: Int,
                                            statusRadioPref: Int,
                                            orderPref: Int): ArrayList<MainDataMinimal> {
        return withContext(Dispatchers.IO) {
    
            val preferenceManager = PreferenceManager(context)
            
            val installedFrom =
                when(filterPref) {
                    R.id.menu_play_apps -> "googlePlay"
                    R.id.menu_fdroid_apps -> "fdroid"
                    R.id.menu_other_apps -> "other"
                    else -> ""
                }
    
            val (dgScoreFrom, dgScoreTo) =
                when (statusRadioPref) {
                    R.id.radio_dg_status ->
                        when (preferenceManager.getInt(DG_STATUS_SORT)) {
                            R.id.sort_not_tested -> Pair(0.0,0.0)
                            R.id.sort_broken -> Pair(1.0,1.9)
                            R.id.sort_bronze -> Pair(2.0,2.9)
                            R.id.sort_silver -> Pair(3.0,3.9)
                            R.id.sort_gold -> Pair(4.0,4.0)
                            else -> Pair(-1.0,-1.0)
                        }
                    else -> Pair(-1.0,-1.0)
                }
    
            val (mgScoreFrom, mgScoreTo) =
                when (statusRadioPref) {
                    R.id.radio_mg_status ->
                        when (preferenceManager.getInt(MG_STATUS_SORT)) {
                            R.id.sort_not_tested -> Pair(0.0,0.0)
                            R.id.sort_broken -> Pair(1.0,1.9)
                            R.id.sort_bronze -> Pair(2.0,2.9)
                            R.id.sort_silver -> Pair(3.0,3.9)
                            R.id.sort_gold -> Pair(4.0,4.0)
                            else -> Pair(-1.0,-1.0)
                        }
                    else -> Pair(-1.0,-1.0)
                }
    
            val isAsc = orderPref != R.id.sort_z_a
            
            mainDataDao.getSortedInstalledApps(installedFrom,
                                               dgScoreFrom,
                                               dgScoreTo,
                                               mgScoreFrom,
                                               mgScoreTo,
                                               isAsc).map {
                mapToMinimalData(it)
            } as ArrayList<MainDataMinimal>
        }
    }
    
    suspend fun miniFavoritesListFromDB(): ArrayList<MainDataMinimal> {
        return withContext(Dispatchers.IO) {
            mainDataDao.getFavApps().map {
                mapToMinimalData(it)
            } as ArrayList<MainDataMinimal>
        }
    }
    
    suspend fun update(mainDataMinimal: MainDataMinimal) {
        val existingData = mainDataDao.getAppByPackage(mainDataMinimal.packageName)
        if (existingData != null) {
            existingData.isFav = mainDataMinimal.isFav
            mainDataDao.update(existingData)
        }
    }
    
    suspend fun miniInstalledListByInstaller(): ArrayList<MainDataMinimal> {
        return withContext(Dispatchers.IO) {
            mainDataDao.getFavApps().map {
                mapToMinimalData(it)
            } as ArrayList<MainDataMinimal>
        }
    }
}