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
        
        existingData?.apply {
            name = mainData.name
            iconUrl = mainData.iconUrl
            dgScore = mainData.dgScore
            totalDgRatings = mainData.totalDgRatings
            mgScore = mainData.mgScore
            totalMgRatings = mainData.totalMgRatings
            isInPlexusData = mainData.isInPlexusData
            update(this)
        } ?: insert(mainData)
    }
    
    @Transaction
    suspend fun insertOrUpdateInstalledApps(mainData: MainData) {
        
        val existingApp = getAppByPackage(mainData.packageName)
        
        existingApp?.apply {
            installedVersion = mainData.installedVersion
            installedBuild = mainData.installedBuild
            isInstalled = mainData.isInstalled
            installedFrom = mainData.installedFrom
            update(this)
        } ?: run {
            mainData.isInPlexusData = false
            insert(mainData)
        }
    }
    
    @Query("SELECT * FROM main_table WHERE packageName = :packageName")
    fun getAppByPackage(packageName: String): MainData?
    
    @Query("SELECT * FROM main_table WHERE isInstalled")
    suspend fun getInstalledApps(): List<MainData>
    
    @Query("""
        SELECT * FROM main_table
        WHERE isInPlexusData
        AND ((dgScore BETWEEN :dgScoreFrom AND :dgScoreTo) OR (:dgScoreFrom = -1 AND :dgScoreTo = -1))
        AND ((mgScore BETWEEN :mgScoreFrom AND :mgScoreTo) OR (:mgScoreFrom = -1 AND :mgScoreTo = -1))
        ORDER BY
        CASE WHEN :isAsc = 1 THEN LOWER(name) END ASC,
        CASE WHEN :isAsc = 0 THEN LOWER(name) END DESC
    """)
    suspend fun getSortedPlexusDataApps(dgScoreFrom: Float,
                                        dgScoreTo: Float,
                                        mgScoreFrom: Float,
                                        mgScoreTo: Float,
                                        isAsc: Boolean): List<MainData>
    // -1 is for ignoring the score when required,
    // so it isn't included while sorting
    
    @Query("""
        SELECT * FROM main_table
        WHERE isInstalled
        AND (installedFrom = :installedFrom OR :installedFrom = '')
        AND ((dgScore BETWEEN :dgScoreFrom AND :dgScoreTo) OR (:dgScoreFrom = -1 AND :dgScoreTo = -1))
        AND ((mgScore BETWEEN :mgScoreFrom AND :mgScoreTo) OR (:mgScoreFrom = -1 AND :mgScoreTo = -1))
        ORDER BY
        CASE WHEN :isAsc = 1 THEN LOWER(name) END ASC,
        CASE WHEN :isAsc = 0 THEN LOWER(name) END DESC
    """)
    suspend fun getSortedInstalledApps(installedFrom: String,
                                       dgScoreFrom: Float,
                                       dgScoreTo: Float,
                                       mgScoreFrom: Float,
                                       mgScoreTo: Float,
                                       isAsc: Boolean): List<MainData>
    // -1 is for ignoring the score when required,
    // so it isn't included while sorting
    
    @Query("""
        SELECT * FROM main_table
        WHERE isFav
        AND (installedFrom = :installedFrom OR :installedFrom = '')
        AND ((dgScore BETWEEN :dgScoreFrom AND :dgScoreTo) OR (:dgScoreFrom = -1 AND :dgScoreTo = -1))
        AND ((mgScore BETWEEN :mgScoreFrom AND :mgScoreTo) OR (:mgScoreFrom = -1 AND :mgScoreTo = -1))
        ORDER BY
        CASE WHEN :isAsc = 1 THEN LOWER(name) END ASC,
        CASE WHEN :isAsc = 0 THEN LOWER(name) END DESC
    """)
    suspend fun getSortedFavApps(installedFrom: String,
                                 dgScoreFrom: Float,
                                 dgScoreTo: Float,
                                 mgScoreFrom: Float,
                                 mgScoreTo: Float,
                                 isAsc: Boolean): List<MainData>
    // -1 is for ignoring the score when required,
    // so it isn't included while sorting
    
    @Query("""
        SELECT * FROM main_table
        WHERE name LIKE '%' || :searchQuery || '%' OR packageName LIKE '%' || :searchQuery || '%'
        ORDER BY
        CASE WHEN :isAsc = 1 THEN LOWER(name) END ASC,
        CASE WHEN :isAsc = 0 THEN LOWER(name) END DESC
    """)
    suspend fun searchInDb(searchQuery: String,
                           isAsc: Boolean): List<MainData>
    
}