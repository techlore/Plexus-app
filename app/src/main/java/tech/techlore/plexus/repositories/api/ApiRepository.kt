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
import tech.techlore.plexus.api.ApiManager
import tech.techlore.plexus.api.ApiService
import tech.techlore.plexus.models.main.Root
import tech.techlore.plexus.models.send.Application

class ApiRepository(private val apiService: ApiService) {
    
    fun getApplications(): Call<Root> {
        return apiService.getApplications()
    }
    
    fun sendApplication(application: Application): Call<ResponseBody> {
        return apiService.sendApplication(application)
    }
    
}