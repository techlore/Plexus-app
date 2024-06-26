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

package tech.techlore.plexus.converters.get

import androidx.room.TypeConverter
import com.fasterxml.jackson.module.kotlin.jacksonObjectMapper
import com.fasterxml.jackson.module.kotlin.readValue
import tech.techlore.plexus.models.get.responses.DeviceToken

object DeviceTokenConverter {
    
    private val objectMapper = jacksonObjectMapper()
    
    @TypeConverter
    fun fromJsonToDeviceToken(json: String): DeviceToken {
        return objectMapper.readValue(json)
    }
    
    @TypeConverter
    fun fromDeviceTokenToJson(deviceToken: DeviceToken): String {
        return objectMapper.writeValueAsString(deviceToken)
    }
}