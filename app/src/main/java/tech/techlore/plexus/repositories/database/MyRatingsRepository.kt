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

package tech.techlore.plexus.repositories.database

import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import tech.techlore.plexus.R
import tech.techlore.plexus.dao.MyRatingsDao
import tech.techlore.plexus.models.myratings.MyRating
import tech.techlore.plexus.models.myratings.MyRatingDetails

class MyRatingsRepository(private val ratingsDao: MyRatingsDao) {
    
    suspend fun insertOrUpdateMyRatings(name: String,
                                        packageName: String,
                                        iconUrl: String?,
                                        myRatingDetails: MyRatingDetails) {
        return withContext(Dispatchers.IO){
            ratingsDao.insertOrUpdateMyRatings(name, packageName, iconUrl, myRatingDetails)
        }
    }
    
    suspend fun getMyRatingsByPackage(packageName: String): MyRating? {
        return withContext(Dispatchers.IO) {
            ratingsDao.getMyRatingsByPackage(packageName)
        }
    }
    
    suspend fun getSortedMyRatingsByName(orderPref: Int): ArrayList<MyRating> {
        return withContext(Dispatchers.IO) {
            val isAsc = orderPref != R.id.sortZA
            ratingsDao.getSortedMyRatingsByName(isAsc) as ArrayList<MyRating>
        }
    }
    
    suspend fun deleteAllRatings() {
        withContext(Dispatchers.IO){
            ratingsDao.deleteAllRatings()
        }
    }
    
}