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

package tech.techlore.plexus.repositories.api

import okhttp3.ResponseBody
import retrofit2.Call
import tech.techlore.plexus.api.ApiService
import tech.techlore.plexus.models.get.apps.GetAppsRoot
import tech.techlore.plexus.models.get.apps.GetSingleAppRoot
import tech.techlore.plexus.models.get.ratings.RatingsRoot
import tech.techlore.plexus.models.post.PostAppRoot
import tech.techlore.plexus.models.post.PostRatingRoot

class ApiRepository(private val apiService: ApiService) {
    
    fun getAppsWithScores(pageNumber: Int): Call<GetAppsRoot> {
        return apiService.getAppsWithScores(pageNumber)
    }
    
    fun getSingleAppWithScores(packageName: String): Call<GetSingleAppRoot> {
        return apiService.getSingleAppWithScores(packageName)
    }
    
    fun getRatings(packageName: String, pageNumber: Int): Call<RatingsRoot> {
        return apiService.getRatings(packageName, pageNumber)
    }
    
    fun postApp(postAppRoot: PostAppRoot): Call<ResponseBody> {
        return apiService.postApp(postAppRoot)
    }
    
    fun postRating(packageName: String, postRatingRoot: PostRatingRoot): Call<ResponseBody> {
        return apiService.postRating(packageName, postRatingRoot)
    }
    
}