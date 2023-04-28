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
import tech.techlore.plexus.models.myratings.MyRating

@Dao
interface MyRatingsDao {
    
    @Insert(onConflict = OnConflictStrategy.IGNORE)
    suspend fun insert(myRating: MyRating)
    
    @Update
    suspend fun update(myRating: MyRating)
    
    @Delete
    suspend fun delete(myRating: MyRating)
    
    @Query("SELECT * FROM my_ratings_table WHERE id = :id")
    suspend fun getRatingById(id: String): MyRating?
    
    @Query("SELECT * FROM my_ratings_table WHERE packageName = :packageName")
    suspend fun getRatingByPackage(packageName: String): MyRating?
    
    @Transaction
    suspend fun insertOrUpdateMyRatings(myRating: MyRating) {
        
        val existingRating = getRatingById(myRating.id)
    
        if (existingRating == null) {
            insert(myRating)
        }
        else{
            existingRating.ratingScore = myRating.ratingScore
            existingRating.notes = myRating.notes
            update(existingRating)
        }
    }
    
    @Query("""
        SELECT * FROM my_ratings_table
        WHERE (googleLib = :googleLib OR :googleLib = '')
        AND (ratingScore == :ratingScore OR :ratingScore = -1)
        ORDER BY
        CASE WHEN :isAsc = 1 THEN name END ASC,
        CASE WHEN :isAsc = 0 THEN name END DESC
    """)
    suspend fun getSortedMyRatings(googleLib: String,
                                   ratingScore: Int,
                                   isAsc: Boolean): List<MyRating>
    // -1 is for ignoring the score when required,
    // so it doesn't include it while filtering
    
}