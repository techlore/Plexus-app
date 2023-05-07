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
import tech.techlore.plexus.utils.UiUtils.Companion.mapStatusChipIdToRatingScore
import java.util.ArrayList

class MyRatingsRepository(private val ratingsDao: MyRatingsDao) {
    
    suspend fun insertOrUpdateMyRatings(myRating: MyRating) {
        return withContext(Dispatchers.IO){
            ratingsDao.insertOrUpdateMyRatings(myRating)
        }
    }
    
    suspend fun getSortedMyRatings(statusRadioPref: Int,
                                   statusChipPref: Int,
                                   orderPref: Int): ArrayList<MyRating> {
        return withContext(Dispatchers.IO) {
    
            val (googleLib, ratingScore) =
                when(statusRadioPref) {
                    R.id.my_ratings_radio_dg_status -> Pair("none", mapStatusChipIdToRatingScore(statusChipPref))
                    R.id.my_ratings_radio_mg_status -> Pair("micro_g", mapStatusChipIdToRatingScore(statusChipPref))
                    else -> Pair("", -1)
                }
    
            val isAsc = orderPref != R.id.my_ratings_sort_z_a
            
            ratingsDao.getSortedMyRatings(googleLib, ratingScore, isAsc) as ArrayList<MyRating>
        }
    }
    
}