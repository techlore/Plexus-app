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
import tech.techlore.plexus.models.get.main.MainData

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
            existingData.dgScore = mainData.dgScore
            existingData.totalDgRatings = mainData.totalDgRatings
            existingData.mgScore = mainData.mgScore
            existingData.totalMgRatings = mainData.totalMgRatings
            existingData.ratingsList = mainData.ratingsList
            existingData.isInPlexusData = mainData.isInPlexusData
            update(existingData)
        }
    }
    
    @Transaction
    suspend fun insertOrUpdateInstalledApps(mainData: MainData) {
        
        val existingApp = getAppByPackage(mainData.packageName)
        
        if (existingApp == null) {
            mainData.isInPlexusData = false
            insert(mainData)
        }
        else {
            existingApp.installedVersion = mainData.installedVersion
            existingApp.installedBuild = mainData.installedBuild
            existingApp.isInstalled = mainData.isInstalled
            existingApp.installedFrom = mainData.installedFrom
            update(existingApp)
        }
    }
    
    @Transaction
    suspend fun updateFavApps(mainData: MainData) {
        val existingApp = getAppByPackage(mainData.packageName)
        if (existingApp != null) {
            existingApp.isFav = mainData.isFav
            update(existingApp)
        }
    }
    
    @Transaction
    suspend fun updateIsInPlexusData(mainData: MainData) {
        val existingData = getAppByPackage(mainData.packageName)
        if (existingData != null) {
            existingData.isInPlexusData = mainData.isInPlexusData
            update(existingData)
        }
    }
    
    @Query("SELECT * FROM main_table WHERE packageName = :packageName")
    fun getAppByPackage(packageName: String): MainData?
    
    @Query("SELECT * FROM main_table WHERE isInstalled")
    suspend fun getInstalledApps(): List<MainData>
    
    @Query("""
        SELECT * FROM main_table
        WHERE isInPlexusData
        AND ((dgScore >= :dgScoreFrom AND dgScore <= :dgScoreTo) OR (:dgScoreFrom = -1 AND :dgScoreTo = -1))
        AND ((mgScore >= :mgScoreFrom AND mgScore <= :mgScoreTo) OR (:mgScoreFrom = -1 AND :mgScoreTo = -1))
        ORDER BY
        CASE WHEN :isAsc = 1 THEN name END ASC,
        CASE WHEN :isAsc = 0 THEN name END DESC
    """)
    suspend fun getSortedPlexusDataApps(dgScoreFrom: Float,
                                        dgScoreTo: Float,
                                        mgScoreFrom: Float,
                                        mgScoreTo: Float,
                                        isAsc: Boolean): List<MainData>
    // -1 is for ignoring the score when required,
    // so it doesn't include it while filtering
    
    @Query("""
        SELECT * FROM main_table
        WHERE isInstalled
        AND (installedFrom = :installedFrom OR :installedFrom = '')
        AND ((dgScore >= :dgScoreFrom AND dgScore <= :dgScoreTo) OR (:dgScoreFrom = -1 AND :dgScoreTo = -1))
        AND ((mgScore >= :mgScoreFrom AND mgScore <= :mgScoreTo) OR (:mgScoreFrom = -1 AND :mgScoreTo = -1))
        ORDER BY
        CASE WHEN :isAsc = 1 THEN name END ASC,
        CASE WHEN :isAsc = 0 THEN name END DESC
    """)
    suspend fun getSortedInstalledApps(installedFrom: String,
                                       dgScoreFrom: Float,
                                       dgScoreTo: Float,
                                       mgScoreFrom: Float,
                                       mgScoreTo: Float,
                                       isAsc: Boolean): List<MainData>
    // -1 is for ignoring the score when required,
    // so it doesn't include it while filtering
    
    @Query("""
        SELECT * FROM main_table
        WHERE isFav
        AND (installedFrom = :installedFrom OR :installedFrom = '')
        AND ((dgScore >= :dgScoreFrom AND dgScore <= :dgScoreTo) OR (:dgScoreFrom = -1 AND :dgScoreTo = -1))
        AND ((mgScore >= :mgScoreFrom AND mgScore <= :mgScoreTo) OR (:mgScoreFrom = -1 AND :mgScoreTo = -1))
        ORDER BY
        CASE WHEN :isAsc = 1 THEN name END ASC,
        CASE WHEN :isAsc = 0 THEN name END DESC
    """)
    suspend fun getSortedFavApps(installedFrom: String,
                                 dgScoreFrom: Float,
                                 dgScoreTo: Float,
                                 mgScoreFrom: Float,
                                 mgScoreTo: Float,
                                 isAsc: Boolean): List<MainData>
    // -1 is for ignoring the score when required,
    // so it doesn't include it while filtering
    
    @Query("""
        SELECT * FROM main_table
        WHERE name LIKE '%' || :searchQuery || '%' OR packageName LIKE '%' || :searchQuery || '%'
        ORDER BY name ASC
    """)
    suspend fun searchFromDb(searchQuery: String): List<MainData>
    
}