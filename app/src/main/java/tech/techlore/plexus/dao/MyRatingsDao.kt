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
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Transaction
import androidx.room.Update
import tech.techlore.plexus.models.myratings.MyRating
import tech.techlore.plexus.models.myratings.MyRatingDetails

@Dao
interface MyRatingsDao {
    
    @Insert(onConflict = OnConflictStrategy.IGNORE)
    suspend fun insert(myRating: MyRating)
    
    @Update
    suspend fun update(myRating: MyRating)
    
    @Query("SELECT * FROM my_ratings_table WHERE packageName = :packageName")
    suspend fun getMyRatingsByPackage(packageName: String): MyRating?
    
    @Transaction
    suspend fun insertOrUpdateMyRatings(name: String,
                                        packageName: String,
                                        iconUrl: String?,
                                        myRatingDetails: MyRatingDetails) {
        
        val existingRating = getMyRatingsByPackage(packageName)
        
        existingRating.apply {
            if (this == null) {
                val myRatingDetailsList = listOf(myRatingDetails)
                insert(MyRating(name = name,
                                packageName = packageName,
                                iconUrl = iconUrl,
                                ratingsDetails = myRatingDetailsList))
            }
            else{
                ratingsDetails += myRatingDetails
                update(this)
            }
        }
    }
    
    @Query("""
        SELECT * FROM my_ratings_table
        ORDER BY
        CASE WHEN :isAsc = 1 THEN LOWER(name) END ASC,
        CASE WHEN :isAsc = 0 THEN LOWER(name) END DESC
    """)
    suspend fun getSortedMyRatingsByName(isAsc: Boolean): List<MyRating>
    
    @Query("DELETE FROM my_ratings_table")
    suspend fun deleteAllRatings()
    
}