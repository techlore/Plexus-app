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

package tech.techlore.plexus.models.get.main

import androidx.room.Embedded
import androidx.room.Entity
import androidx.room.PrimaryKey
import androidx.room.TypeConverters
import com.fasterxml.jackson.annotation.JsonProperty
import tech.techlore.plexus.converters.get.RatingsConverter
import tech.techlore.plexus.converters.get.ScoreConverter
import tech.techlore.plexus.models.get.ratings.Rating
import tech.techlore.plexus.models.get.scores.DgScore
import tech.techlore.plexus.models.get.scores.MgScore

@Entity(tableName = "main_table")
@TypeConverters(value = [ScoreConverter::class, RatingsConverter::class])
data class MainData(
    
    @JsonProperty("name")
    var name: String = "",
    
    @PrimaryKey @JsonProperty("package")
    var packageName: String = "",
    
    @Embedded
    var dgScore: DgScore = DgScore(dgScore = 0.0f, totalDgRatings = 0),
    
    @Embedded
    var mgScore: MgScore = MgScore(mgScore = 0.0f, totalMgRatings = 0),
    
    var ratingsList: ArrayList<Rating> = ArrayList(),
    
    var installedVersion: String = "",
    
    var installedBuild: Int = 0,
    
    var installedFrom: String = "",
    
    var isInPlexusData: Boolean = true,
    
    var isInstalled: Boolean = false,
    
    var isFav: Boolean = false

)