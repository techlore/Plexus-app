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

package tech.techlore.plexus.api

import okhttp3.ResponseBody
import retrofit2.Call
import retrofit2.http.Body
import retrofit2.http.GET
import retrofit2.http.Headers
import retrofit2.http.POST
import retrofit2.http.Path
import retrofit2.http.Query
import tech.techlore.plexus.models.get.apps.GetAppsRoot
import tech.techlore.plexus.models.get.apps.GetSingleAppRoot
import tech.techlore.plexus.models.get.ratings.RatingsRoot
import tech.techlore.plexus.models.post.PostAppRoot
import tech.techlore.plexus.models.post.PostRatingRoot

interface ApiService {
    
    @GET("apps?scores=true&limit=150")
    fun getAppsWithScores(@Query("page") pageNumber: Int): Call<GetAppsRoot>
    
    @GET("apps/{packageName}?scores=true")
    fun getSingleAppWithScores(@Path("packageName") packageName: String): Call<GetSingleAppRoot>
    
    @GET("apps/{packageName}/ratings?limit=150")
    fun getRatings(@Path("packageName") packageName: String,
                   @Query("page") pageNumber: Int): Call<RatingsRoot>
    
    @POST("apps")
    @Headers("Content-Type: application/json")
    fun postApp(@Body postAppRoot: PostAppRoot): Call<ResponseBody>
    
    @POST("apps/{packageName}/ratings")
    @Headers("Content-Type: application/json")
    fun postRating(@Path("packageName") packageName: String, @Body postRatingRoot: PostRatingRoot): Call<ResponseBody>
    
}