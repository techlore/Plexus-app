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
import tech.techlore.plexus.dao.MyRatingsDao
import tech.techlore.plexus.models.myratings.MyRating
import java.util.ArrayList

class MyRatingsRepository(private val ratingsDao: MyRatingsDao) {
    
    suspend fun getRatingById(id: String): MyRating? {
        return withContext(Dispatchers.IO){
            ratingsDao.getRatingById(id)
        }
    }
    
    suspend fun getRatingByPackage(packageName: String): MyRating? {
        return withContext(Dispatchers.IO){
            ratingsDao.getRatingByPackage(packageName)
        }
    }
    
    suspend fun insertOrUpdateMyRatings(myRating: MyRating) {
        return withContext(Dispatchers.IO){
            ratingsDao.insertOrUpdateMyRatings(myRating)
        }
    }
    
    suspend fun getSortedMyRatings(): ArrayList<MyRating> {
        return withContext(Dispatchers.IO) {
            ratingsDao.getSortedMyRatings() as ArrayList<MyRating>
        }
    }
    
}