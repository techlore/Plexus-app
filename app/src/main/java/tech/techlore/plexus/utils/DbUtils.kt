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
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import retrofit2.awaitResponse
import tech.techlore.plexus.dao.InstalledAppsDao
import tech.techlore.plexus.dao.PlexusDataDao

class DbUtils {
    
    companion object {
        
        suspend fun plexusDataIntoDB(plexusDataDao: PlexusDataDao) {
            return withContext(Dispatchers.IO) {
                val call = ApiUtils.createService().getApplications()
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
                for (app in ListUtils.scannedInstalledAppsList(context)) {
                    installedAppsDao.insertOrUpdate(app)
                }
            }
        }
        
    }
}