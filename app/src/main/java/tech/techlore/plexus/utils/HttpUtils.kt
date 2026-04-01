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

package tech.techlore.plexus.utils

import io.ktor.client.statement.HttpResponse
import io.ktor.http.isSuccess
import tech.techlore.plexus.exception.ApiException

class HttpUtils {
    
    companion object {
        
        fun HttpResponse.checkStatus(): HttpResponse {
            if (!status.isSuccess()) {
                throw ApiException("\n\nHTTP ${status.value} - ${getHttpStatusMessage(status.value)}")
            }
            return this
        }
        
        private fun getHttpStatusMessage(statusCode: Int): String {
            return when (statusCode) {
                400 -> "Bad Request"
                401 -> "Unauthorized"
                403 -> "Forbidden"
                404 -> "Not Found"
                500 -> "Internal Server Error"
                502 -> "Bad Gateway"
                503 -> "Service Unavailable"
                else -> "HTTP Error"
            }
        }
        
    }
    
}