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

package tech.techlore.plexus.api

import kotlinx.serialization.json.Json
import okhttp3.Dispatcher
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.OkHttpClient
import retrofit2.Retrofit
import retrofit2.converter.kotlinx.serialization.asConverterFactory
import java.util.concurrent.TimeUnit

class ApiManager {
    
    companion object {
        
        private const val API_BASE_URL = "https://plexus.techlore.tech/api/v1/"
        
        private val okHttpClient =
            OkHttpClient.Builder()
                .dispatcher(
                    Dispatcher().apply {
                        maxRequests = 8 // Max parallel network requests (default is 64)
                    }
                )
                .connectTimeout(30, TimeUnit.SECONDS) // Default is 10 seconds
                .readTimeout(30, TimeUnit.SECONDS) // Default is 10 seconds
                .writeTimeout(30, TimeUnit.SECONDS) // Default is 10 seconds
                .build()
        
        private val json = Json { ignoreUnknownKeys = true }
        
        fun apiBuilder(): ApiService {
            val retrofit = Retrofit.Builder()
                .baseUrl(API_BASE_URL)
                .client(okHttpClient)
                .addConverterFactory(json.asConverterFactory("application/json".toMediaType()))
                .build()
            
            return retrofit.create(ApiService::class.java)
        }
    }
}
