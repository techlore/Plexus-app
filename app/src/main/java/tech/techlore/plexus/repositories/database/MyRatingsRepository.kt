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

package tech.techlore.plexus.repositories.database

import androidx.paging.Pager
import androidx.paging.PagingConfig
import androidx.paging.PagingData
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.withContext
import tech.techlore.plexus.R
import tech.techlore.plexus.dao.MyRatingsDao
import tech.techlore.plexus.models.mini.MyRatingMini
import tech.techlore.plexus.models.myratings.MyRating
import tech.techlore.plexus.models.myratings.MyRatingDetails

class MyRatingsRepository(private val myRatingsDao: MyRatingsDao) {
    
    suspend fun insertOrUpdateMyRatings(name: String,
                                        packageName: String,
                                        iconUrl: String?,
                                        myRatingDetails: MyRatingDetails) {
        return withContext(Dispatchers.IO){
            myRatingsDao.insertOrUpdateMyRatings(name, packageName, iconUrl, myRatingDetails)
        }
    }
    
    suspend fun getMyRatingByPackage(packageName: String): MyRating? {
        return withContext(Dispatchers.IO) {
            myRatingsDao.getMyRatingByPackage(packageName)
        }
    }
    
    fun getSortedMyRatingsByName(orderPref: Int): Flow<PagingData<MyRatingMini>> {
        return Pager(
            config = PagingConfig(pageSize = 25, prefetchDistance = 10, enablePlaceholders = false),
            pagingSourceFactory = {
                myRatingsDao.getSortedMyRatingsByName(orderPref != R.id.sortZA)
            }
        ).flow
    }
    
    suspend fun deleteSingleRatingDetail(packageName: String, ratingId: String) {
        withContext(Dispatchers.IO){
            myRatingsDao.deleteSingleRatingDetail(packageName, ratingId)
        }
    }
    
    suspend fun deleteSingleMyRating(packageName: String) {
        withContext(Dispatchers.IO){
            myRatingsDao.deleteSingleMyRating(packageName)
        }
    }
    
    suspend fun deleteAllMyRatings() {
        withContext(Dispatchers.IO){
            myRatingsDao.deleteAllMyRatings()
        }
    }
    
}