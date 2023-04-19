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

import retrofit2.Call
import retrofit2.http.Body
import retrofit2.http.GET
import retrofit2.http.Headers
import retrofit2.http.POST
import retrofit2.http.Path
import tech.techlore.plexus.models.main.MainRoot
import tech.techlore.plexus.models.ratings.Ratings
import tech.techlore.plexus.models.ratings.RatingsRoot
import tech.techlore.plexus.models.scores.DgScoreRoot
import tech.techlore.plexus.models.scores.MgScoreRoot

interface ApiService {
    
    @GET("apps")
    fun getApps(): Call<MainRoot>
    
    @GET("apps/{packageName}/scores/none")
    fun getDgScore(@Path("packageName") packageName: String): Call<DgScoreRoot>
    
    @GET("apps/{packageName}/scores/micro_g")
    fun getMgScore(@Path("packageName") packageName: String): Call<MgScoreRoot>
    
    @GET("apps/{packageName}/ratings")
    fun getRatings(@Path("packageName") packageName: String): Call<RatingsRoot>
    
    /*@POST("apps/{packageName}/ratings")
    @Headers("Content-Type: application/json")
    fun updateRating(@Path("packageName") packageName: String, @Body ratings: Ratings): Call<Ratings>*/
    
}