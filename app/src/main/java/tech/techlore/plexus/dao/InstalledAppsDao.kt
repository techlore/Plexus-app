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

package tech.techlore.plexus.dao

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Transaction
import androidx.room.Update
import tech.techlore.plexus.models.InstalledApp

@Dao
interface InstalledAppsDao {
    
    @Query("SELECT * FROM installed_table WHERE packageName = :packageName")
    fun getInstalledAppByPackage(packageName: String): InstalledApp?
    
    @Query("SELECT * FROM installed_table")
    suspend fun getAll(): List<InstalledApp>
    
    @Insert(onConflict = OnConflictStrategy.IGNORE)
    suspend fun insert(installedApp: InstalledApp)
    
    @Update
    suspend fun update(installedApp: InstalledApp)
    
    @Transaction
    suspend fun insertOrUpdate(installedApp: InstalledApp) {
        
        val existingApp = getInstalledAppByPackage(installedApp.packageName)
        
        if (existingApp == null) {
            insert(installedApp)
        }
        else {
            existingApp.name = installedApp.name
            existingApp.installedVersion = installedApp.installedVersion
            update(existingApp)
        }
    }
    
    @Delete
    suspend fun delete(installedApp: InstalledApp)
    
}