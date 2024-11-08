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
import io.ktor.client.call.body
import io.ktor.client.request.get
import io.ktor.client.request.header
import io.ktor.client.request.parameter
import io.ktor.client.request.post
import io.ktor.client.request.setBody
import io.ktor.client.request.url
import io.ktor.client.statement.HttpResponse
import io.ktor.http.ContentType
import io.ktor.http.contentType
import tech.techlore.plexus.models.get.apps.GetAppsRoot
import tech.techlore.plexus.models.get.apps.GetSingleAppRoot
import tech.techlore.plexus.models.get.ratings.RatingsRoot
import tech.techlore.plexus.models.get.device.RegisterDeviceResponse
import tech.techlore.plexus.models.get.device.VerifyDeviceResponseRoot
import tech.techlore.plexus.models.post.app.PostAppRoot
import tech.techlore.plexus.models.post.device.RegisterDevice
import tech.techlore.plexus.models.post.device.VerifyDevice
import tech.techlore.plexus.models.post.rating.PostRatingRoot

class ApiService(private val okHttpClient: HttpClient) {
    
    private companion object {
        private const val API_BASE_URL = "https://plexus.techlore.tech/api/v1"
    }
    
    suspend fun getAppsWithScores(pageNumber: Int, lastUpdated: String?): GetAppsRoot {
        return okHttpClient.get {
            url("${API_BASE_URL}/apps")
            contentType(ContentType.Application.Json)
            parameter("scores", true)
            parameter("limit", 150)
            parameter("page", pageNumber)
            parameter("last_updated", lastUpdated)
        }.body()
    }
    
    suspend fun getSingleAppWithScores(packageName: String): GetSingleAppRoot {
        return okHttpClient.get {
            url("${API_BASE_URL}/apps/${packageName}")
            contentType(ContentType.Application.Json)
            parameter("scores", true)
        }.body()
    }
    
    suspend fun getRatings(packageName: String, pageNumber: Int): RatingsRoot {
        return okHttpClient.get {
            url("${API_BASE_URL}/apps/${packageName}/ratings")
            contentType(ContentType.Application.Json)
            parameter("limit", 150)
            parameter("page", pageNumber)
        }.body()
    }
    
    suspend fun registerDevice(registerDevice: RegisterDevice): RegisterDeviceResponse {
        return okHttpClient.post {
            url("${API_BASE_URL}/devices/register")
            contentType(ContentType.Application.Json)
            setBody(registerDevice)
        }.body()
    }
    
    suspend fun verifyDevice(verifyDevice: VerifyDevice): VerifyDeviceResponseRoot {
        return okHttpClient.post {
            url("${API_BASE_URL}/devices/verify")
            contentType(ContentType.Application.Json)
            setBody(verifyDevice)
        }.body()
    }
    
    suspend fun renewDevice(authToken: String): VerifyDeviceResponseRoot {
        return okHttpClient.post {
            url("${API_BASE_URL}/devices/renew")
            contentType(ContentType.Application.Json)
            header("Authorization", "Bearer $authToken")
        }.body()
    }
    
    suspend fun postApp(authToken: String, postAppRoot: PostAppRoot): HttpResponse {
        return okHttpClient.post {
            url("${API_BASE_URL}/apps")
            contentType(ContentType.Application.Json)
            header("Authorization", "Bearer $authToken")
            setBody(postAppRoot)
        }
    }
    
    suspend fun postRating(authToken: String,
                           packageName: String,
                           postRatingRoot: PostRatingRoot): HttpResponse {
        return okHttpClient.post {
            url("${API_BASE_URL}/apps/${packageName}/ratings")
            contentType(ContentType.Application.Json)
            header("Authorization", "Bearer $authToken")
            setBody(postRatingRoot)
        }
    }
    
}