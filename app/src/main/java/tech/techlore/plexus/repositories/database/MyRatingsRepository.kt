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

import android.content.Context
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import tech.techlore.plexus.R
import tech.techlore.plexus.dao.MyRatingsDao
import tech.techlore.plexus.models.myratings.MyRating
import tech.techlore.plexus.utils.UiUtils.Companion.mapInstalledFromChipIdToString
import tech.techlore.plexus.utils.UiUtils.Companion.mapStatusChipIdToRatingScore
import java.util.ArrayList

class MyRatingsRepository(private val ratingsDao: MyRatingsDao) {
    
    suspend fun insertOrUpdateMyRatings(myRating: MyRating) {
        return withContext(Dispatchers.IO){
            ratingsDao.insertOrUpdateMyRatings(myRating)
        }
    }
    
    suspend fun getMyRatingsList(): ArrayList<MyRating> {
        return withContext(Dispatchers.IO) {
            ratingsDao.getMyRatingsList() as ArrayList<MyRating>
        }
    }
    
    suspend fun getSortedMyRatings(context: Context,
                                   versionPref: String,
                                   romPref: String,
                                   androidPref: String,
                                   installedFromPref: Int,
                                   statusRadioPref: Int,
                                   statusChipPref: Int,
                                   orderPref: Int): ArrayList<MyRating> {
        return withContext(Dispatchers.IO) {
            
            val version =
                if (versionPref == context.getString(R.string.any)) ""
                else versionPref.substringBefore(" (")
            
            val rom =
                if (romPref == context.getString(R.string.any)) ""
                else romPref
            
            val android =
                if (androidPref == context.getString(R.string.any)) ""
                else androidPref
            
            val installedFromString = mapInstalledFromChipIdToString(installedFromPref)
            
            val (googleLib, ratingScore) =
                when(statusRadioPref) {
                    R.id.my_ratings_radio_dg_status -> Pair("none", mapStatusChipIdToRatingScore(statusChipPref))
                    R.id.my_ratings_radio_mg_status -> Pair("micro_g", mapStatusChipIdToRatingScore(statusChipPref))
                    else -> Pair("", -1)
                }
            
            val isAsc = orderPref != R.id.my_ratings_sort_z_a
            
            ratingsDao.getSortedMyRatings(version,
                                          rom,
                                          android,
                                          installedFromString,
                                          googleLib,
                                          ratingScore,
                                          isAsc) as ArrayList<MyRating>
        }
    }
    
}