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

package tech.techlore.plexus.dao

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Transaction
import androidx.room.Update
import tech.techlore.plexus.models.main.MainData

@Dao
interface MainDataDao {
    
    @Insert(onConflict = OnConflictStrategy.IGNORE)
    suspend fun insert(mainData: MainData)
    
    @Update
    suspend fun update(mainData: MainData)
    
    @Delete
    suspend fun delete(mainData: MainData)
    
    @Transaction
    suspend fun insertOrUpdatePlexusData(mainData: MainData) {
        
        val existingData = getAppByPackage(mainData.packageName)
        
        if (existingData == null) {
            insert(mainData)
        }
        else {
            existingData.name = mainData.name
            existingData.packageName = mainData.packageName
            update(existingData)
        }
    }
    
    @Transaction
    suspend fun insertOrUpdateInstalledApps(mainData: MainData) {
        
        val existingApps = getAppByPackage(mainData.packageName)
        
        if (existingApps == null) {
            mainData.isInPlexusData = false
            insert(mainData)
        }
        else {
            existingApps.installedVersion = mainData.installedVersion
            existingApps.isInstalled = mainData.isInstalled
            existingApps.installedFrom = mainData.installedFrom
            update(existingApps)
        }
    }
    
    @Transaction
    suspend fun updateFavApps(mainData: MainData) {
        
        val existingApps = getAppByPackage(mainData.packageName)
        
        if (existingApps != null) {
            existingApps.isFav = mainData.isFav
            update(existingApps)
        }
    }
    
    @Query("SELECT * FROM main_table WHERE packageName = :packageName")
    fun getAppByPackage(packageName: String): MainData?
    
    @Query("SELECT * FROM main_table WHERE packageName = :packageName AND isInstalled = false")
    fun getNotInstalledAppByPackage(packageName: String): MainData?
    
    @Query("SELECT * FROM main_table WHERE packageName = :packageName AND isInstalled = true")
    fun getInstalledAppByPackage(packageName: String): MainData?
    
    @Query("SELECT * FROM main_table WHERE packageName = :packageName AND isFav = true")
    fun getFavoriteAppByPackage(packageName: String): MainData?
    
    @Query("SELECT * FROM main_table WHERE isInstalled = false")
    suspend fun getNotInstalledApps(): List<MainData>
    
    @Query("SELECT * FROM main_table WHERE isInstalled = true")
    suspend fun getInstalledApps(): List<MainData>
    
    @Query("SELECT * FROM main_table WHERE isFav = true")
    suspend fun getFavApps(): List<MainData>
    
    @Query("""
        SELECT * FROM main_table
        WHERE isInstalled = false
        ORDER BY
        CASE WHEN :isAsc = 1 THEN name END ASC,
        CASE WHEN :isAsc = 0 THEN name END DESC
    """)
    suspend fun getSortedNotInstalledApps(isAsc: Boolean): List<MainData>
    
    @Query("""
        SELECT * FROM main_table
        WHERE (installedFrom = :installedFrom OR :installedFrom = '')
        AND isInstalled = true
        ORDER BY
        CASE WHEN :isAsc = 1 THEN name END ASC,
        CASE WHEN :isAsc = 0 THEN name END DESC
    """)
    suspend fun getSortedInstalledApps(installedFrom: String, isAsc: Boolean): List<MainData>
    
    /*@Query("SELECT * FROM main_table")
    suspend fun getAll(): List<PlexusData>*/
    
    /*@Query("SELECT * FROM main_table WHERE installedFrom = :installedFrom")
    fun getInstalledAppsByInstaller(installers: List<String>): List<MainData>
    
    @Query("SELECT * FROM main_table WHERE dgStatus = :dgStatus")
    fun getInstalledAppsByDGRating(): List<MainData>
    
    @Query("SELECT * FROM main_table ORDER BY name DESC")
    fun getInstalledAppsByMGRating(): List<MainData>*/
}