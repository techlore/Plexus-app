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

package tech.techlore.plexus.database

import androidx.room.Database
import androidx.room.RoomDatabase
import tech.techlore.plexus.dao.InstalledAppsDao
import tech.techlore.plexus.dao.PlexusDataDao
import tech.techlore.plexus.models.InstalledApp
import tech.techlore.plexus.models.PlexusData

@Database(entities = [PlexusData::class, InstalledApp::class], version = 1)
abstract class MainDatabase : RoomDatabase() {
    
    abstract fun plexusDataDao(): PlexusDataDao
    abstract fun installedDataDao(): InstalledAppsDao
    
}