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

package tech.techlore.plexus.repositories.api

import io.ktor.client.statement.HttpResponse
import tech.techlore.plexus.api.ApiService
import tech.techlore.plexus.models.get.apps.GetAppsRoot
import tech.techlore.plexus.models.get.apps.GetSingleAppRoot
import tech.techlore.plexus.models.get.ratings.RatingsRoot
import tech.techlore.plexus.models.get.device.RegisterDeviceResponse
import tech.techlore.plexus.models.get.device.VerifyDeviceResponseRoot
import tech.techlore.plexus.models.post.app.PostAppRoot
import tech.techlore.plexus.models.post.device.RegisterDevice
import tech.techlore.plexus.models.post.device.VerifyDevice
import tech.techlore.plexus.models.post.rating.PostRatingRoot

class ApiRepository(private val apiService: ApiService) {
    
    suspend fun getAppsWithScores(pageNumber: Int, lastUpdated: String?): GetAppsRoot {
        return apiService.getAppsWithScores(pageNumber, lastUpdated)
    }
    
    suspend fun getSingleAppWithScores(packageName: String): GetSingleAppRoot {
        return apiService.getSingleAppWithScores(packageName)
    }
    
    suspend fun getRatings(packageName: String, pageNumber: Int): RatingsRoot {
        return apiService.getRatings(packageName, pageNumber)
    }
    
    suspend fun registerDevice(registerDevice: RegisterDevice): RegisterDeviceResponse {
        return apiService.registerDevice(registerDevice)
    }
    
    suspend fun verifyDevice(verifyDevice: VerifyDevice): VerifyDeviceResponseRoot {
        return apiService.verifyDevice(verifyDevice)
    }
    
    suspend fun renewDevice(authToken: String): VerifyDeviceResponseRoot {
        return apiService.renewDevice(authToken)
    }
    
    suspend fun postApp(authToken: String, postAppRoot: PostAppRoot): HttpResponse {
        return apiService.postApp(authToken, postAppRoot)
    }
    
    suspend fun postRating(authToken: String,
                           packageName: String,
                           postRatingRoot: PostRatingRoot): HttpResponse {
        return apiService.postRating(authToken,
                                     packageName,
                                     postRatingRoot)
    }
    
}