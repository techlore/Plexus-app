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

import com.fasterxml.jackson.databind.ObjectMapper
import com.fasterxml.jackson.module.kotlin.registerKotlinModule
import okhttp3.Dispatcher
import okhttp3.OkHttpClient
import retrofit2.Retrofit
import retrofit2.converter.jackson.JacksonConverterFactory

class ApiManager {
    
    companion object {
        
        private const val API_BASE_URL = "https://plexus.fly.dev/api/v1/"
        
        private val okHttpClient =
            OkHttpClient.Builder()
                .dispatcher(
                    Dispatcher().apply {
                        maxRequests = 15 // Max number of concurrent requests (default is 64)
                    }
                )
                .build()
        
        
        fun apiBuilder(): ApiService {
            val retrofit = Retrofit.Builder()
                .baseUrl(API_BASE_URL)
                .client(okHttpClient)
                .addConverterFactory(JacksonConverterFactory.create(ObjectMapper().registerKotlinModule()))
                .build()
            
            return retrofit.create(ApiService::class.java)
        }
    }
}