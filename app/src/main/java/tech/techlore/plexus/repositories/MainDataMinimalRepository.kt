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

import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import tech.techlore.plexus.dao.MainDataDao
import tech.techlore.plexus.models.main.MainData
import tech.techlore.plexus.models.minimal.MainDataMinimal

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
    
    suspend fun miniPlexusDataListFromDB(): ArrayList<MainDataMinimal> {
        return withContext(Dispatchers.IO) {
            mainDataDao.getNotInstalledApps().map {
                mapToMinimalData(it)
            } as ArrayList<MainDataMinimal>
        }
    }
    
    suspend fun miniInstalledAppsListFromDB(): ArrayList<MainDataMinimal> {
        return withContext(Dispatchers.IO) {
            mainDataDao.getInstalledApps().map {
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
}