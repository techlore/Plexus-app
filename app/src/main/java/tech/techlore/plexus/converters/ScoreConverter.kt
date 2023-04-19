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

package tech.techlore.plexus.converters

import androidx.room.TypeConverter
import com.fasterxml.jackson.core.type.TypeReference
import com.fasterxml.jackson.databind.ObjectMapper
import tech.techlore.plexus.models.ratings.Ratings
import tech.techlore.plexus.models.scores.DgScore
import tech.techlore.plexus.models.scores.MgScore

object ScoreConverter {
    
    private val objectMapper = ObjectMapper()
    
    @TypeConverter
    fun fromDgScore(json: String): DgScore {
        return objectMapper.readValue(json, DgScore::class.java)
    }
    
    @TypeConverter
    fun toDgScore(dgScore: DgScore): String {
        return objectMapper.writeValueAsString(dgScore)
    }
    
    @TypeConverter
    fun fromMgScore(json: String): MgScore {
        return objectMapper.readValue(json, MgScore::class.java)
    }
    
    @TypeConverter
    fun toMgScore(mgScore: MgScore): String {
        return objectMapper.writeValueAsString(mgScore)
    }
}