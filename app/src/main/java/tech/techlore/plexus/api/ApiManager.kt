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

import io.ktor.client.HttpClient
import io.ktor.client.engine.okhttp.OkHttp
import io.ktor.client.plugins.contentnegotiation.ContentNegotiation
import io.ktor.serialization.kotlinx.json.json
import kotlinx.serialization.json.Json
import okhttp3.CertificatePinner
import okhttp3.ConnectionSpec
import okhttp3.Dispatcher
import org.koin.core.component.KoinComponent
import org.koin.core.component.get
import java.util.concurrent.TimeUnit

class ApiManager {
    
    companion object : KoinComponent {
        
        private val okHttpClient =
            HttpClient(OkHttp) {
                engine {
                    config {
                        dispatcher(
                            Dispatcher().apply {
                                maxRequests = 8 // Max parallel network requests (default is 64)
                            }
                        )
                        connectTimeout(30, TimeUnit.SECONDS) // Default is 10 seconds
                        readTimeout(30, TimeUnit.SECONDS) // Default is 10 seconds
                        writeTimeout(30, TimeUnit.SECONDS) // Default is 10 seconds
                        followRedirects(true)
                        connectionSpecs(listOf(ConnectionSpec.RESTRICTED_TLS, ConnectionSpec.MODERN_TLS))
                        certificatePinner(
                            CertificatePinner.Builder()
                                .add("plexus.techlore.tech", "sha256/RacvZ7FBPeVMqaHMkRcNJ32cR8Qxz5KN3glkNrKu0no=")
                                .add("f-droid.org", "sha256/DngAL+l1YaQ1nusSDt3FBa7EMKj5F/c7W3MDDnIttOE=")
                                .add("play-lh.googleusercontent.com", "sha256/KaXtxbltoxW0VkjNk20CuThvpbEfG0VoUEUOC7YC4t8=")
                                .add("translate.fedilab.app", "sha256/OPZfpSwcAEajLsmxhF6/gq2lkUgENlt/iQa1SEeHZSE=")
                                .build()
                        )
                    }
                }
                install(ContentNegotiation) {
                    json(get<Json>())
                }
            }
        
        fun apiBuilder(): ApiService {
            return ApiService(okHttpClient)
        }
    }
}
