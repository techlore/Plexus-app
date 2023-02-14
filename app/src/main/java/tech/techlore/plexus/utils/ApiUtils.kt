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

package tech.techlore.plexus.utils

import okhttp3.ResponseBody
import retrofit2.Call
import retrofit2.Retrofit
import retrofit2.converter.jackson.JacksonConverterFactory
import retrofit2.http.Body
import retrofit2.http.GET
import retrofit2.http.Headers
import retrofit2.http.POST
import tech.techlore.plexus.models.send.Application
import tech.techlore.plexus.models.main.Root

class ApiUtils {
    
    companion object {
    
        private const val API_BASE_URL = "https://plexus.fly.dev/api/v1/"
    
        interface Api {
            @GET("applications")
            fun getApplications(): Call<Root>
    
            @POST("applications")
            @Headers("Content-Type: application/json")
            fun sendApplication(@Body application: Application): Call<ResponseBody>
        }
        
        fun createService(): Api {
            val retrofit = Retrofit.Builder()
                .baseUrl(API_BASE_URL)
                .addConverterFactory(JacksonConverterFactory.create())
                .build()
        
            return retrofit.create(Api::class.java)
        }
        
    }
}