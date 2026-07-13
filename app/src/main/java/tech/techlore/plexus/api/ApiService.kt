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
import io.ktor.client.request.delete
import io.ktor.client.request.forms.submitForm
import io.ktor.client.request.get
import io.ktor.client.request.header
import io.ktor.client.request.parameter
import io.ktor.client.request.post
import io.ktor.client.request.setBody
import io.ktor.client.request.url
import io.ktor.client.statement.HttpResponse
import io.ktor.http.ContentType
import io.ktor.http.contentType
import io.ktor.http.parameters
import tech.techlore.plexus.models.get.apps.GetAppsRoot
import tech.techlore.plexus.models.get.apps.GetSingleAppRoot
import tech.techlore.plexus.models.get.ratings.RatingsRoot
import tech.techlore.plexus.models.get.device.RegisterDeviceResponse
import tech.techlore.plexus.models.get.device.VerifyDeviceResponseRoot
import tech.techlore.plexus.models.post.app.PostAppRoot
import tech.techlore.plexus.models.post.device.RegisterDevice
import tech.techlore.plexus.models.post.device.VerifyDevice
import tech.techlore.plexus.models.post.rating.PostMyRatingDeleteToken
import tech.techlore.plexus.models.post.rating.PostMyRatingRoot
import tech.techlore.plexus.utils.HttpUtils.Companion.checkStatus
import kotlin.coroutines.cancellation.CancellationException

class ApiService(private val okHttpClient: HttpClient) {
    
    private companion object {
        private const val API_BASE_URL = "https://plexus.techlore.tech/api/v1"
    }
    
    suspend fun getAppsWithScores(pageNumber: Int, lastUpdated: String?): GetAppsRoot {
        return okHttpClient.get {
            url("${API_BASE_URL}/apps")
            contentType(ContentType.Application.Json)
            parameter("scores", true)
            parameter("limit", 200)
            parameter("page", pageNumber)
            parameter("last_updated", lastUpdated)
        }.checkStatus().body()
    }
    
    suspend fun getSingleAppWithScores(packageName: String): GetSingleAppRoot {
        return okHttpClient.get {
            url("${API_BASE_URL}/apps/${packageName}")
            contentType(ContentType.Application.Json)
            parameter("scores", true)
        }.checkStatus().body()
    }
    
    suspend fun getRatings(packageName: String, pageNumber: Int): RatingsRoot {
        return okHttpClient.get {
            url("${API_BASE_URL}/apps/${packageName}/ratings")
            contentType(ContentType.Application.Json)
            parameter("limit", 200)
            parameter("page", pageNumber)
        }.checkStatus().body()
    }
    
    suspend fun registerDevice(registerDevice: RegisterDevice): RegisterDeviceResponse {
        return okHttpClient.post {
            url("${API_BASE_URL}/devices/register")
            contentType(ContentType.Application.Json)
            setBody(registerDevice)
        }.checkStatus().body()
    }
    
    suspend fun verifyDevice(verifyDevice: VerifyDevice): VerifyDeviceResponseRoot {
        return okHttpClient.post {
            url("${API_BASE_URL}/devices/verify")
            contentType(ContentType.Application.Json)
            setBody(verifyDevice)
        }.checkStatus().body()
    }
    
    suspend fun renewDevice(authToken: String): VerifyDeviceResponseRoot {
        return okHttpClient.post {
            url("${API_BASE_URL}/devices/renew")
            contentType(ContentType.Application.Json)
            header("Authorization", "Bearer $authToken")
        }.checkStatus().body()
    }
    
    suspend fun postApp(authToken: String, postAppRoot: PostAppRoot): HttpResponse {
        return okHttpClient.post {
            url("${API_BASE_URL}/apps")
            contentType(ContentType.Application.Json)
            header("Authorization", "Bearer $authToken")
            setBody(postAppRoot)
        }
    }
    
    suspend fun postMyRating(authToken: String,
                             packageName: String,
                             postMyRatingRoot: PostMyRatingRoot): HttpResponse {
        return okHttpClient.post {
            url("${API_BASE_URL}/apps/${packageName}/ratings")
            contentType(ContentType.Application.Json)
            header("Authorization", "Bearer $authToken")
            setBody(postMyRatingRoot)
        }
    }
    
    suspend fun deleteMyRating(authToken: String,
                               packageName: String,
                               ratingId: String,
                               postMyRatingDeleteToken: PostMyRatingDeleteToken): HttpResponse {
        return okHttpClient.delete {
            url("${API_BASE_URL}/apps/${packageName}/ratings/${ratingId}")
            contentType(ContentType.Application.Json)
            header("Authorization", "Bearer $authToken")
            setBody(postMyRatingDeleteToken)
        }
    }
    
    suspend fun translateRatingNote(note: String, targetLang: String): HttpResponse {
        val urlList =
            listOf(
                "https://translate.fediverse.radio/translate",
                "https://translate.f-hub.org/translate"
            )
        
        urlList.forEach {
            try {
                return okHttpClient.submitForm(
                    url = it,
                    formParameters = parameters {
                        append("q", note)
                        append("source", "auto")
                        append("target", targetLang)
                        append("format", "text")
                        append("alternatives", "0")
                    }
                )
            }
            catch (e: CancellationException) {
                // This prevents app crashing in the following scenario:
                // Translate bottom sheet displayed > translating still in progress >
                // user taps outside the bottom sheet > coroutine is canceled > app crashes
                throw e
            }
            catch (_: Exception) { }
        }
        
        throw Exception("All translation URLs failed.")
    }
    
}