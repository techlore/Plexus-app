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
import tech.techlore.plexus.models.get.scores.Score

object ScoreConverter {
    
    private val objectMapper = jacksonObjectMapper()
    
    @TypeConverter
    fun fromJsonToScore(json: String): Score {
        return objectMapper.readValue(json)
    }
    
    @TypeConverter
    fun fromScoreToJson(score: Score): String {
        return objectMapper.writeValueAsString(score)
    }
    
}