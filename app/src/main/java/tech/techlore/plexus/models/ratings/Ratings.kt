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

package tech.techlore.plexus.models.ratings

import androidx.room.Embedded
import androidx.room.TypeConverters
import com.fasterxml.jackson.annotation.JsonIgnore
import com.fasterxml.jackson.annotation.JsonProperty
import tech.techlore.plexus.converters.RatingsScoreConverter

//@Entity(tableName = "ratings_table")
@TypeConverters(RatingsScoreConverter::class)
data class Ratings(
    
    @JsonProperty("id")
    var id: String? = null,

    @JsonProperty("app_package")
    @JsonIgnore
    var packageName: String? = null,
    
    @JsonProperty("app_build_number")
    var buildNumber: Int = 0,
    
    @JsonProperty("app_version")
    var version: String? = null,
    
    @JsonProperty("google_lib")
    var googleLib: String? = null,
    
    @JsonProperty("score")
    @Embedded
    var ratingsScore: RatingsScore? = null,
    
    @JsonProperty("notes")
    var note: String? = null

)